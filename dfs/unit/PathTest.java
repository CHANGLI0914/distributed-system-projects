package unit;

import common.Path;
import test.Test;
import test.TestFailed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by lichkkkk on 5/13/16.
 */
public class PathTest extends Test {

    private final String test_path = System.getProperty("user.dir")+"/dfs_test";
    private File testDir = new File(test_path);
    private File[] files = new File[5];

    @Override
    protected void initialize() throws TestFailed {
        if (!testDir.mkdir()) {
            throw new TestFailed("Can't create test directory.");
        }
        try {
            for (int i = 0; i < 5; i++) {
                files[i] = File.createTempFile("test-"+i+"-", ".txt", testDir);
            }
        } catch (IOException e) {
            throw new TestFailed("Exception occured when creating files.", e);
        }
    }

    @Override
    protected void perform() throws TestFailed{
        checkList();
        checkCompareTo();
    }

    /**
     * Test the list method. The standard result is got from File.listFiles().
     * The order doesn't matter here.
     *
     * @throws TestFailed
     */
    private void checkList() throws TestFailed {

        String listPath = test_path;
        File listDir = new File(listPath);

        // Get list result
        Path[] children;
        try {
            children = Path.list(listDir);
        } catch (FileNotFoundException fe) {
            throw new TestFailed("File not found when list", fe);
        }

        // Construct the standard result set
        HashSet<File> listRes = new HashSet<>();
        for (File file : listDir.listFiles()) {
            listRes.add(file);
        }

        // Check
        for (Path p : children) {
            File f = p.toFile(listDir);
            if (!listRes.contains(f)) {
                throw new TestFailed("List result not complete.");
            }
            listRes.remove(f);
        }
        if (!listRes.isEmpty()) {
            throw new TestFailed("List result contains extra files.");
        }
    }

    /**
     * Test the compareTo method here. Parent should return a negative
     * result, children should return a positive result. Compare to self
     * should return 0;
     *
     * @throws TestFailed
     */
    private void checkCompareTo() throws TestFailed {
        Path path = new Path(test_path);
        if (path.compareTo(path.parent()) <= 0) {
            throw new TestFailed("Path <= Parent");
        }
        if (path.compareTo(new Path(path, "haha")) >= 0) {
            throw new TestFailed("Path >= Children");
        }
        if (path.compareTo(path) != 0) {
            throw new TestFailed("Path != self");
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
