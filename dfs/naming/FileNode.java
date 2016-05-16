package naming;

import storage.Command;
import storage.Storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lichkkkk on 5/13/16.
 */
public class FileNode {

    private final String name;

    private final boolean isFile;

    private final FileNode parent;

    private final List<Command> commandList;

    private final List<FileNode> children;

    protected FileNode(String name, boolean isFile, FileNode parent) {
        this.name = name;
        this.isFile = isFile;
        this.parent = parent;
        this.commandList = new ArrayList<>();
        if (!isFile) {
            this.children = new ArrayList<>();
        } else {
            this.children = null;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isDirectory() {
        return !isFile;
    }

    public FileNode getParent() {
        return parent;
    }

    /*
     *  Children related methods.
     */

    public FileNode getChild(String childName) {
        if (isFile) return null;

        for (FileNode node : children) {
            if (node.getName().equals(childName)) {
                return node;
            }
        }
        return null;
    }

    public boolean hasChild(String childName) {
        if (isFile) return false;

        return getChild(childName) != null;
    }

    public FileNode addChild(String childName, boolean isFile) {
        if (isFile || hasChild(childName)) {
            return null;
        }
        FileNode child = new FileNode(childName, isFile, this);
        children.add(child);
        return child;
    }

    public void deleteChild(String childName) {
        if (isFile) return;

        FileNode child = getChild(childName);
        if (child != null) children.remove(child);
    }

    public List<FileNode> children() {
        if (isFile) return null;

        return children;
    }

    public String[] listChildren() {
        if (isFile) return new String[0];

        return children.toArray(new String[0]);
    }

    public int childrenSize() {
        if (isFile) return 0;

        return children.size();
    }

    /*
     *  Storage related methods
     */

    public int commandSize() {
        return commandList.size();
    }

    public void addCommand(Command command) {
        if (!commandList.contains(command)) {
            commandList.add(command);
        }
    }

    public void deleteStorage(Command command) {
        commandList.remove(command);
    }

    // TODO: Just Pick the first one here
    public Command getCommand() {
        if (commandList.isEmpty()) return null;
        else return commandList.get(0);
    }

    public List<Command> commands() {
        return commandList;
    }
}
