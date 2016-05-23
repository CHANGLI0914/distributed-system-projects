package naming;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import common.Path;
import rmi.RMIException;
import rmi.Skeleton;
import storage.Command;
import storage.Storage;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{

    private FileTree fileTree;

    private FileNode fileNode;

    private Hashtable<Command, Storage> serverTable;

    private Skeleton<Registration> regSkeleton;

    private Skeleton<Service> serviceSkeleton;

    //List of locks that connect a path to a lock
    private volatile ConcurrentHashMap<Path, ReadWriteLock> lockList;
    //Replication Counter
    private volatile ConcurrentHashMap<Path, Integer> replicationCounter;
    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        fileTree = FileTree.getTree();
        fileNode = fileTree.getRoot();
        serverTable = new Hashtable<>();

        InetSocketAddress regAddress =
                new InetSocketAddress(NamingStubs.REGISTRATION_PORT);
        regSkeleton = new Skeleton<>(Registration.class, this, regAddress);

        InetSocketAddress serviceAddress =
                new InetSocketAddress(NamingStubs.SERVICE_PORT);
        serviceSkeleton = new Skeleton<>(Service.class, this, serviceAddress);

        lockList = new ConcurrentHashMap<Path, ReadWriteLock>();
        replicationCounter = new ConcurrentHashMap<Path, Integer>();
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
        regSkeleton.start();
        serviceSkeleton.start();
    }

    /** Stops the naming server.

        <p>
        This method commands both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
        regSkeleton.stop();
        serviceSkeleton.stop();
        fileTree.deleteAllNodes();
        stopped(null);  // TODO: I think this is not correct
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
        /**
         * TODO: Not sure where and how should we call this method.
         */
    }

    public class ReadWriteLock {
        private int readNum = 0;
        private int writeNum = 0;
        private int writeRequest = 0;

        
        public synchronized void lockRead() throws InterruptedException {
          while (!readAccess()) {
            wait();
          }
          readNum ++;
        }

        private boolean readAccess() {
          if (writeNum > 0 || writeRequest > 0) {
            return false;
          } else {
            return true;
          }
        }

        public synchronized void lockWrite() throws InterruptedException {
          writeRequest ++;

          while (!writeAccess()) {
            wait();
          }
          writeRequest --;
          writeNum ++;
        }

        private boolean writeAccess() {
          if (readNum > 0 || writeNum > 0) {
            return false;
          } else {
            return true;
          }
        }
        
        private synchronized void unlockRead() {
          readNum --;
          // to wake up all waiting threads at the same time.
          notifyAll();
        }

        private synchronized void unlockWrite() {
          writeNum --;
          notifyAll();
        }
        public int getReadNum(){
        	return readNum;
        }
        
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException, RMIException
    {
        if (path == null) {
          throw new NullPointerException("path is null!");
        }

        if (!isValidPath(path)) {
            throw new FileNotFoundException("The path is not valid.");
        }

        // find all the parents of current path
        List<Path> pathList = pathParents(path);
        //check if these parents have a ReadWriteLock. If not, assign one to them
        for (int i = 0; i < pathList.size(); i++) {
          if (lockList.get(pathList.get(i)) == null) {
            lockList.put(pathList.get(i), new ReadWriteLock());
          }
        }
        //sort the list of parents
        //Collections: enable to work with groups of objects
        Collections.sort(pathList);

        for (int i = 0; i < pathList.size(); i ++) {
          if (exclusive && i == pathList.size() - 1)
          {
            try {
              lockList.get(pathList.get(i)).lockWrite();
              
              FileNode filenode=fileTree.findNode(pathList.get(i));
              if(filenode.isFile()){
            	  filenode.clearVisittime();
            	  Integer commandnum=filenode.commandSize();
            	  if(commandnum>1){
            		  removefile(pathList.get(i));
            	  }
              } 
            }
            	catch (InterruptedException e) {
              return;
            }
          } else {
            try {
              lockList.get(pathList.get(i)).lockRead();
              
              //if is file, maybe replication
              FileNode filenode=fileTree.findNode(pathList.get(i));
              if(filenode.isFile()){
            	  Integer num=filenode.getVisittime();
            	  if(num>=20){
            		  filenode.clearVisittime();
            		  replicatefile(pathList.get(i));
            	  }
                  filenode.addVisittime();
              }
 
            } catch (InterruptedException e) {
              return;
            }
          }
          }
    }
    
    public void replicatefile(Path path) throws InterruptedException, FileNotFoundException{
    	
    	FileNode filenode=fileTree.findNode(path);
    	List<Command> unavailableStorage = filenode.commands();
        List<Command> availableStorage = new ArrayList<>();
        for (Command c :serverTable.keySet() ) {
            if (!unavailableStorage.contains(c)) {
            	availableStorage.add(c);
            }
        }
        if(availableStorage.size()>0){
        	try{
        	Command nextServer=availableStorage.get(0);
        	nextServer.copy(path, serverTable.get(unavailableStorage.get(0)));
        	filenode.addCommand(nextServer);
        	}
        	catch(Exception e){
        		e.printStackTrace();
        	}

        }     	
        
    }
    
    public void removefile(Path path) throws RMIException{
    	FileNode filenode=fileTree.findNode(path);
    	List<Command> availableStorage = new ArrayList<>(filenode.commands());

        availableStorage.remove(0); 
        System.out.println("avai:"+availableStorage.size());
        System.out.println("still	"+filenode.commandSize());
        for (Command c : availableStorage) {
            c.delete(path);
            filenode.deleteStorage(c);
        }
        System.out.println(filenode.commandSize());

    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
      if (path == null) {
        throw new NullPointerException("path is null!");
      }
      if (!isValidPath(path)) {
          throw new IllegalArgumentException("The path is not valid.");
      }

      List<Path> pathList = pathParents(path);

      for (int i = 0; i < pathList.size(); i ++) {
        if (lockList.get(pathList.get(i)) == null) {
          lockList.put(pathList.get(i), new ReadWriteLock());
        }
      }

      Collections.sort(pathList);
      Collections.reverse(pathList);

      for (int i = 0; i < pathList.size(); i++) {
        if (exclusive && i == 0) {
            lockList.get(pathList.get(i)).unlockWrite();

        } else {
            lockList.get(pathList.get(i)).unlockRead();
            
            // if a file has been read more than 20 times, duplicate it.

        }
      }
    }


    private List<Path> pathParents(Path path) {
      ArrayList<Path> pathlist = new ArrayList<Path>();
      pathlist.add(path);

      while (true) {
        try {
          pathlist.add(path.parent());
          path = path.parent();
        } catch (IllegalArgumentException e) {
          break;
        }
    }
      return pathlist;
    }

    private boolean isValidPath(Path file) {
      //FileNode current = fileTree.getRoot();
      FileNode current = fileNode;
      for (String s : file) {
        current = current.getChild(s);
        if (current == null) {
          return false;
        }
      }
      return true;
    }
    

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
        FileNode node = fileTree.findNode(path);
        if (node == null) {
            throw new FileNotFoundException();
        }
        return node.isDirectory();
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException, RMIException
    {
        lock (directory, false);

        FileNode node = fileTree.findNode(directory);
        if (node == null || !node.isDirectory()) {
            throw new FileNotFoundException();
        }

        unlock(directory, false);

        return node.listChildren();
    }

    private Command pickOneCommand() {
        return serverTable.keys().nextElement();
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        if (serverTable.isEmpty()) {
            throw new IllegalStateException();
        }
        if (file.isRoot()) {
            return false;
        }

        FileNode node = fileTree.findNode(file.parent());
        if (node == null || !node.isDirectory()) {
            throw new FileNotFoundException();
        }
        if (node.hasChild(file.last())) {
            return false;
        }

        Command storageServer = pickOneCommand();
        if(storageServer.create(file)) {
            FileNode fileCreated = node.addChild(file.last(), true);
            fileCreated.addCommand(storageServer);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        if (serverTable.isEmpty()) {
            throw new IllegalStateException();
        }
        if (directory.isRoot()) {
            return false;
        }

        FileNode node = fileTree.findNode(directory.parent());
        if (node == null || !node.isDirectory()) {
            throw new FileNotFoundException();
        }

        if (node.hasChild(directory.last())) {
            return false;
        } else {
            node.addChild(directory.last(), false);
            return true;
        }
/*
        try {
            /**
             * Code here is a little bit ugly. When we need to create a new
             * directory, we first create a temporary file under this
             * directory and then delete it.
             *
             * The reason to do this is that the Command interface onlt
             * permits us to create files instread of directories. (NOT SURE
             * WHETHER I UNDERSTAND IT CORRECTLY)
             *
             * Additionally, if any RMIEXceptions are thrown during this
             * process, an inconsistency might happen.
             */
  /*          Path childPath = new Path(directory, "tmp.txt");
            if (node.getCommand().create(childPath)) {
                node.getCommand().delete(childPath);
                FileNode child = node.addChild(directory.last(), false);
                child.addCommand(node.getCommand());
                return true;
            } else {
                return false;
            }
        } catch (RMIException re) { return false; }
    */
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException, RMIException
    {
    	System.out.println("delete here");
        if (path.isRoot()) return false;

        FileNode node = fileTree.findNode(path);
        if (node == null) {
            throw new FileNotFoundException();
        }

        /**
         * Just delete all descendant files, as we assume the storage server
         * would delete all empty directories automatically.
         */
        //lock (path, true);

        try {
            for (FileNode file : node.descendantFileNodes()) {

                while (file.commandSize() != 0) {
                    Command command = file.commands().get(0);
                    if (!command.delete(path)) {
                        return false;
                    }
                    node.deleteStorage(command);
                }

                file.getParent().deleteChild(file.getName());
            }
        } catch (RMIException re) { return false; }

        node.getParent().deleteChild(node.getName());

        //unlock (path, true);
        return true;
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        FileNode node = fileTree.findNode(file);
        if (node == null || node.isDirectory()) {
            throw new FileNotFoundException();
        }
        return serverTable.get(node.getCommand());
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        if (client_stub == null || command_stub == null || files == null) {
            throw new NullPointerException();
        }
        if (serverTable.containsKey(command_stub)) {
            throw new IllegalStateException();
        }
        serverTable.put(command_stub, client_stub);

        /**
         * Parameter files here should not contain any directory. But in some
         * test cases they do contain some directories? Excuse me?
         */
        List<Path> fileExisted = new ArrayList<>();
        for (Path path : files) {
            FileNode node = fileTree.findNode(path);
            if (node == null) {
                fileTree.addNode(path, command_stub);
            } else {
                if (!node.isRoot()) {
                    fileExisted.add(path);
                }
            }
        }
        return fileExisted.toArray(new Path[0]);
    }
}
