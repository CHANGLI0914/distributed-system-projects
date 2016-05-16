package naming;

import common.Path;

import java.io.File;
import java.util.Iterator;

/**
 * Created by lichkkkk on 5/13/16.
 */
public class FileTree {

    private static FileTree tree;

    private final FileNode root;

    private FileTree() {
        root = new FileNode("", false, null);
    }

    public static FileTree getTree() {
        if (tree == null) {
            tree = new FileTree();
        }
        return tree;
    }

    public FileNode findNode(Path path) {
        FileNode node = root;
        for (String pathComponent : path) {
            node = node.getChild(pathComponent);
            if (node == null) { break; }
        }
        return node;
    }

    public boolean addNode(Path path) {
        FileNode node = root;
        Iterator<String> pathIterator = path.iterator();
        while (pathIterator.hasNext()) {
            String pathComponent = pathIterator.next();
            if (!node.isDirectory()) {
                return false;
            }
            if (!node.hasChild(pathComponent)) {
                if (pathIterator.hasNext()) {
                    node.addChild(pathComponent, false);
                } else {
                    node.addChild(pathComponent, true);
                }
            }
            node = node.getChild(pathComponent);
        }
        return true;
    }
}
