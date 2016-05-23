package naming;

import storage.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one file or directory in the file tree.
 *
 * The root node (first node)will be created when we construct the FileTree
 * object. Then, all other non-root nodes should be created via the addNode()
 * method (In order to maintain the consistence easily).
 *
 * A list of Command is used here to record all storage servers which hold this
 * file or directory. To keep a consistent state, all non-root nodes should
 * have at least one storage server in its list. (Say, when you add a new node,
 * remember also to add the related storage server.)
 */
public class FileNode {

    private final String name;

    private final boolean isFile;

    private final FileNode parent;

    // storage servers that have this file
    private final List<Command> commandList;

    private final List<FileNode> children;
    
    private List<FileNode> copies;
    
    private FileNode copyancestor;
    
    private Integer visittime;

    /**
     * Constructor should never be called out of FileNode or FileTree
     *
     * @param name file/directory name
     * @param isFile true if is a file, false if is a directory
     * @param parent parent node. All non-root node should have a parent
     */
    protected FileNode(String name, boolean isFile, FileNode parent) {
        this.name = name;
        this.isFile = isFile;
        this.parent = parent;
        this.commandList = new ArrayList<>();
        this.visittime=0;
        if (!isFile) {
            this.children = new ArrayList<>();
        } else {
            this.children = null;
        }
             
        this.copyancestor=null;
        
        copies=new ArrayList<>();
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
        if (this.isFile) throw new IllegalAccessError();

        for (FileNode node : children) {
            if (node.getName().equals(childName)) {
                return node;
            }
        }
        return null;
    }

    public boolean hasChild(String childName) {
        if (this.isFile) throw new IllegalAccessError();

        return getChild(childName) != null;
    }

    /**
     * After calling this, remember also to call addComand()
     *
     * @param childName name of child node to be added
     * @param isFile is file or dorectory
     * @return  the node added. If there is already a child with this name,
     *          null is returned.
     */
    public FileNode addChild(String childName, boolean isFile) {
        if (this.isFile) throw new IllegalAccessError();

        if (hasChild(childName)) {
            return null;
        }
        FileNode child = new FileNode(childName, isFile, this);
        children.add(child);
        return child;
    }

    public void deleteChild(String childName) {
        if (this.isFile) throw new IllegalAccessError();

        FileNode child = getChild(childName);
        if (child != null) children.remove(child);
    }

    public List<FileNode> children() {
        if (this.isFile) throw new IllegalAccessError();

        return children;
    }

    public String[] listChildren() {
        if (this.isFile) throw new IllegalAccessError();

        String[] res = new String[children.size()];
        for (int i=0; i<res.length; i++) {
            res[i] = children.get(i).getName();
        }
        return res;
    }

    public int childrenSize() {
        if (this.isFile) throw new IllegalAccessError();

        return children.size();
    }

    /**
     * Return all descendant file nodes (may include it self)
     * @return  Array of file nodes
     */
    public List<FileNode> descendantFileNodes() {
        List<FileNode> res = new ArrayList<>();
        if (this.isFile) {
            res.add(this);
        } else {
            for (FileNode node : children) {
                res.addAll(node.descendantFileNodes());
            }
        }
        return res;
    }

    /*
     *  Storage related methods
     */

    public int commandSize() {
        if (!this.isFile) throw new IllegalAccessError();

        return commandList.size();
    }

    public void addCommand(Command command) {
        if (!this.isFile) throw new IllegalAccessError();

        if (!commandList.contains(command)) {
            commandList.add(command);
        }
    }

    public Command getCommand() {
        if (!this.isFile) throw new IllegalAccessError();

        // Just Pick the first one here
        if (commandList.isEmpty()) return null;
        else return commandList.get(0);
    }
    

    public void deleteStorage(Command command) {
        if (!this.isFile) throw new IllegalAccessError();

        commandList.remove(command);
    }

    public List<Command> commands() {
        if (!this.isFile) throw new IllegalAccessError();

        return commandList;
    }
    
    public Integer getVisittime(){
    	return visittime;
    	
    }
    public void addVisittime(){
    	visittime+=1;
    }
    
    public void clearVisittime(){
    	visittime=0;
    }
}
