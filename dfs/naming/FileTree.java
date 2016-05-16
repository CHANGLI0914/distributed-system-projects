package naming;

import common.Path;
import storage.Command;

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

    public FileNode addNode(Path path, Command command) {
        FileNode node = root;
        Iterator<String> pathIterator = path.iterator();
        while (pathIterator.hasNext()) {
            String pathComponent = pathIterator.next();
            if (!node.isDirectory()) {
                throw new Error("Conflict here.");
            }
            if (!node.hasChild(pathComponent)) {
                if (pathIterator.hasNext()) {
                    node = node.addChild(pathComponent, false);
                    node.addCommand(command);
                } else {
                    node = node.addChild(pathComponent, true);
                    node.addCommand(command);
                }
            } else {
                node = node.getChild(pathComponent);
            }
        }
        return node;
    }
}
