package unit;

import common.Path;
import storage.StorageServer;
import test.TestFailed;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by lichkkkk on 5/13/16.
 */
public class StorageServerTest extends BaseUnitTest {

    protected StorageServer server;
    protected Path filePath;
    protected File fileFile;
    protected String str = "1234567890";

    @Override
    protected void initialize() throws TestFailed {
        super.initialize();
        server = new StorageServer(testDir);
        filePath = new Path("/test.txt");
        fileFile = new File(testDir, "/test.txt");
        if(fileFile.exists()) {
            throw new TestFailed("test file already existed");
        }
    }

    @Override
    protected void perform() throws TestFailed {
        checkCreate();
        checkWrite();
        checkRead();
        checkSize();
        checkDelete();
    }

    private void checkCreate() throws TestFailed {
        if (!server.create(filePath)) {
            throw new TestFailed("Cannot create file.");
        }

        if (!fileFile.exists()) {
            throw new TestFailed("Created file Not Found");
        }

        if (server.create(filePath)) {
            throw new TestFailed("Can create existed file.");
        }
    }

    private void checkWrite() throws TestFailed {
        try {

            server.write(filePath, 0, str.getBytes());
            server.write(filePath, fileFile.length(), str.getBytes());

            FileInputStream is = new FileInputStream(fileFile);
            byte[] buf = new byte[str.getBytes().length];

            is.read(buf);
            if (!Arrays.equals(str.getBytes(), buf)) {
                throw new TestFailed("String written not match.");
            }
            is.read(buf);
            if (!Arrays.equals(str.getBytes(), buf)) {
                throw new TestFailed("String written not match.");
            }
            is.close();
        } catch (IOException e) {
            throw new TestFailed("IOException when writing", e);
        }
    }

    private void checkRead() throws TestFailed {
        try {
            byte[] buf = server.read(filePath, 0, str.getBytes().length);
            if (!Arrays.equals(str.getBytes(), buf)) {
                throw new TestFailed("String read not match.");
            }
            buf = server.read(filePath, buf.length, buf.length);
            if (!Arrays.equals(str.getBytes(), buf)) {
                throw new TestFailed("String read not match.");
            }
        } catch (IOException e) {
            throw new TestFailed("filed when check reading", e);
        }
    }

    private void checkSize() throws TestFailed {
        try {
            if (fileFile.length() != server.size(filePath)) {
                throw new TestFailed("File size not cirrect.");
            }
        } catch (IOException e) {
            throw new TestFailed("IOException when read size.", e);
        }
    }

    private void checkDelete() throws TestFailed {
        server.delete(filePath);
        if (fileFile.exists()) {
            throw new TestFailed("File still exists after deleting");
        }
    }

    private void checkCopy() throws TestFailed {

    }

    @Override
    protected void clean() throws TestFailed {
        if (fileFile.exists()) {
            fileFile.delete();
        }
        super.clean();
    }
}
