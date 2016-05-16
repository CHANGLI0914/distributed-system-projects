package naming;

import java.io.*;
import java.net.*;
import java.util.*;

import rmi.*;
import common.*;
import storage.*;

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

    private Hashtable<Command, Storage> serverTable;

    private Skeleton<Registration> regSkeleton;

    private Skeleton<Service> serviceSkeleton;

    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
        fileTree = FileTree.getTree();
        serverTable = new Hashtable<>();

        InetSocketAddress regAddress =
                new InetSocketAddress(NamingStubs.REGISTRATION_PORT);
        regSkeleton = new Skeleton<>(Registration.class, this, regAddress);

        InetSocketAddress serviceAddress =
                new InetSocketAddress(NamingStubs.SERVICE_PORT);
        serviceSkeleton = new Skeleton<>(Service.class, this, serviceAddress);
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
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
        throw new UnsupportedOperationException("not implemented");
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
    public String[] list(Path directory) throws FileNotFoundException
    {
        FileNode node = fileTree.findNode(directory);
        if (node == null || !node.isDirectory()) {
            throw new FileNotFoundException();
        }
        return node.listChildren();
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
        if (serverTable.isEmpty()) {
            throw new IllegalStateException();
        }
        FileNode node = fileTree.findNode(file.parent());
        if (node == null || !node.isDirectory()) {
            throw new FileNotFoundException();
        }
        return node.getCommand().create(file);
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
        if (serverTable.isEmpty()) {
            throw new IllegalStateException();
        }
        FileNode node = fileTree.findNode(directory.parent());
        if (node == null || !node.isDirectory()) {
            throw new FileNotFoundException();
        }

        try {
            Path childPath = new Path(directory, "tmp.txt");
            if (node.getCommand().create(childPath)) {
                node.getCommand().delete(childPath);
                return true;
            } else {
                return false;
            }
        } catch (RMIException re) { return false; }
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
        FileNode node = fileTree.findNode(path);
        if (node == null) {
            throw new FileNotFoundException();
        }
        try {
            for (Command command : node.commands()) {
                if (!command.delete(path)) {
                    return false;
                }
            }
        } catch (RMIException re) { return false; }
        return true;
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        FileNode node = fileTree.findNode(file);
        if (node == null) {
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

        List<Path> fileExisted = new ArrayList<>();
        for (Path path : files) {
            FileNode node = fileTree.findNode(path);
            if (node == null) {
                fileTree.addNode(path);
            } else if (node.isDirectory()) {
                throw new Error("Files in registration contains directory.");
            } else {
                fileExisted.add(path);
            }
        }
        return fileExisted.toArray(new Path[0]);
    }
}
