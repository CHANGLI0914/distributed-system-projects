package naming;

import common.Path;
import storage.Command;

import java.util.Iterator;

/**
 * Represents the directory tree of this file system.
 *
 * Each file system should only have one such tree, so we use singleton
 * pattern here, which means you need to use the static method getTree() to
 * get the tree object and it always return the same one to you.
 *
 *
 */
public class FileTree {

    private static FileTree tree;

    private FileNode root;

    private FileTree() {
        root = new FileNode("", false, null);
    }

    /**
     * Return the unique FileTree object of this file system. Use singleton
     * pattern here.
     *
     * @return the FileTree object.
     */
    public static FileTree getTree() {
        if (tree == null) {
            tree = new FileTree();
        }
        return tree;
    }

    public FileNode getRoot() {
        return root;
    }

    public void deleteAllNodes() {
        root = new FileNode("", false, null);
    }

    /**
     * Find the fileNode in the tree which represented by the path. If not
     * found , return null.
     *
     * @param path the path want to search.
     * @return the corresponded fileNode, or null if not found.
     */
    public FileNode findNode(Path path) {
        FileNode node = root;
        for (String pathComponent : path) {
            node = node.getChild(pathComponent);
            if (node == null) { break; }
        }
        return node;
    }

    /**
     * Add a new file (NOT A DIRECTORY) to this file tree. Used in the
     * registration process. All missing directories along the path will be
     * created.
     *
     * An error will be thrown when conflicts detected. E.g. we already have
     * a file (NOT DIRECTORY) in the file tree with path "/dir/file", but we
     * want to add another file with path "/dir/file/file2".
     *
     * @param path the path of the file to be added
     * @param command the storage server holds this file
     * @return the added node
     */
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
