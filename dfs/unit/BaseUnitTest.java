package unit;

import test.Test;
import test.TestFailed;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lichkkkk on 5/13/16.
 */
public abstract class BaseUnitTest extends Test {

    protected final String test_path = "/tmp/dfs_test";
    protected File testDir = new File(test_path);
    protected File[] files = new File[5];

    @Override
    protected void initialize() throws TestFailed {
        if (!testDir.mkdir()) {
            throw new TestFailed("Can't create test directory.");
        }
        try {
            for (int i = 0; i < 5; i++) {
                files[i] = new File(test_path + "/test-" + i + ".txt");
                files[i].createNewFile();
                FileOutputStream os = new FileOutputStream(files[i]);
                os.write(System.getProperty("user.name").getBytes());
            }
        } catch (IOException e) {
            throw new TestFailed("Exception occured when creating files.", e);
        }
    }

    @Override
    protected void clean() throws TestFailed {
        for (File file : files) file.delete();
        if (!testDir.delete()) {
            throw new TestFailed("Can't remove test directory.");
        }
    }
}
