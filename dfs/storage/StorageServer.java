package storage;

import java.io.*;
import java.net.*;
import java.util.Iterator;

import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{

    private final File root;
    private final Skeleton<Storage> storageSkeleton;
    private final Skeleton<Command> cmdSkeleton;

    /** Creates a storage server, given a directory on the local filesystem, and
        ports to use for the client and command interfaces.

        <p>
        The ports may have to be specified if the storage server is running
        behind a firewall, and specific ports are open.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @param client_port Port to use for the client interface, or zero if the
                           system should decide the port.
        @param command_port Port to use for the command interface, or zero if
                            the system should decide the port.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root, int client_port, int command_port)
    {
        if (root == null) throw new NullPointerException();

        this.root = root;

        if (client_port != 0) {
            storageSkeleton = new Skeleton<>(
                    Storage.class, this, new InetSocketAddress(client_port));
        } else {
            storageSkeleton = new Skeleton<>(Storage.class, this);
        }

        if (command_port != 0) {
            cmdSkeleton = new Skeleton<>(
                    Command.class, this, new InetSocketAddress(command_port));
        } else {
            cmdSkeleton = new Skeleton<>(Command.class, this);
        }
    }

    /** Creats a storage server, given a directory on the local filesystem.

        <p>
        This constructor is equivalent to
        <code>StorageServer(root, 0, 0)</code>. The system picks the ports on
        which the interfaces are made available.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
     */
    public StorageServer(File root)
    {
        this(root, 0, 0);
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        cmdSkeleton.start();
        storageSkeleton.start();


        // Be careful. Path.list() may be changed later.
        Path[] fileExisted = Path.list(root);

        Path[] fileRedundant = naming_server.register(
                Stub.create(Storage.class, storageSkeleton, hostname),
                Stub.create(Command.class, cmdSkeleton, hostname),
                fileExisted);

        for (Path path : fileRedundant) {
            delete(path);
        }
        deleteEmptyDirectory(root);
    }

    /**
     * Helper function to scan the whole local storage and delete all empty
     * directories (except for the root).
     *
     * @param dir The root to start scan.
     */
    private void deleteEmptyDirectory(File dir) {

        if (!dir.isDirectory()) return;

        for (File child : dir.listFiles()) {
            deleteEmptyDirectory(child);
        }

        if (dir.list().length == 0 && !dir.equals(root)) {
            dir.delete();
        }
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        cmdSkeleton.stop();
        storageSkeleton.stop();
        stopped(null);      // TODO: May not correct to call it here
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
        // TODO: Not sure where and how should we call this
    }

    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
        File f = file.toFile(root);
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException();
        }
        return f.length();
    }

    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
        File f = file.toFile(root);
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException();
        }
        if (offset < 0 || length < 0 || offset+length > f.length()) {
            throw new IndexOutOfBoundsException();
        }

        byte[] buf = new byte[length];
        FileInputStream is = new FileInputStream(f);
        is.skip(offset);
        is.read(buf);
        is.close();

        return buf;
    }

    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
        File f = file.toFile(root);
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException();
        }
        if (offset < 0) {
            throw new IndexOutOfBoundsException();
        }

        /**
         * The second parameter of the below constructor means write in an
         * "append mode" or not. If in the append mode, the position offset
         * would be useless, but if not in the append mode, with an offset,
         * the previous content of the file would be polluted?
         */
        FileOutputStream os = new FileOutputStream(f, false);
        os.getChannel().position(offset);
        os.write(data);
        os.flush();
        os.close();
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
        File f = file.toFile(root);
        if (f.equals(root)) return false;

        File dir = root;

        Iterator<String> pathIterator = file.iterator();
        while (pathIterator.hasNext()) {
            File subDir = new File(dir, pathIterator.next());
            if (pathIterator.hasNext()) {
                // create directories along the path
                if (!subDir.exists()) {
                    if (subDir.mkdir()) {
                        dir = subDir;
                    } else {
                        return false;
                    }
                } else if (!subDir.isDirectory()) {
                    return false;
                } else {
                    dir = subDir;
                }
            } else {
                // create the file (the last component of the path)
                try {
                    return subDir.createNewFile();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    return false;
                }
            }
        }
        throw new Error("Should never reach here");
    }

    @Override
    public synchronized boolean delete(Path path)
    {
        File f = path.toFile(root);
        if (f.equals(root)) return false;

        boolean success;
        if (!f.isDirectory()) {
            success = f.delete();
        } else {
            success = deleteDirectory(f);
        }

        // Always delete empty directories
        if (success) {
            File parent = f.getParentFile();
            while (parent.isDirectory() && !parent.equals(root)
                    && parent.listFiles().length == 0) {
                parent.delete();
                parent = parent.getParentFile();
            }
        }
        return success;
    }

    /**
     * Helper function to delete a directory recursively.
     * @param dir   Directory to be deleted.
     * @return      True if success, false otherwise.
     */
    private boolean deleteDirectory(File dir) {
        for (File child : dir.listFiles()) {
            if (child.isFile()) {
                if (!child.delete()) return false;
            } else {
                if (!deleteDirectory(child)) return false;
            }
        }
        return dir.delete();
    }

    @Override
    public synchronized boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
    {
        if (file == null || server == null)
            throw new NullPointerException();

        // delete the file if it exists
        File f = file.toFile(root);
        long fsize = server.size(file);
        if (f.exists()) {
          delete (file);
        }
        // create the file and make copy
        create(file);
        byte[] btoCopy;
        long offset = 0;
        while (offset < fsize) {
          int readby = (int) Math.min(Integer.MAX_VALUE, fsize - offset);
          btoCopy = server.read (file, offset, readby);
          write (file, offset, btoCopy);
          offset  += readby;
        }
        return true;
    }
}
