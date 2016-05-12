package common;

import java.io.*;
import java.util.*;
import java.io.Serializable;

/** Distributed filesystem paths.

    <p>
    Objects of type <code>Path</code> are used by all filesystem interfaces.
    Path objects are immutable.

    <p>
    The string representation of paths is a forward-slash-delimeted sequence of
    path components. The root directory is represented as a single forward
    slash.

    <p>
    The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
    not permitted within path components. The forward slash is the delimeter,
    and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Comparable<Path>, Serializable
{
  private static final long serialVersionUID = 42L;
  ArrayList<String> pathComponents;

    /** Creates a new path which represents the root directory. */
    public Path()
    {
        pathComponents = new ArrayList<>();
    }

    /** Creates a new path by appending the given component to an existing path.

        @param path The existing path.
        @param component The new component.
        @throws IllegalArgumentException If <code>component</code> includes the
                                         separator, a colon, or
                                         <code>component</code> is the empty
                                         string.
    */
    public Path(Path path, String component)
    {
        // chekck the IllegalArgumentException
        if (component == null || component.contains(":") || component.contains("/")) {
          throw new IllegalArgumentException ("Invalid! Should not include separator, colon, or is empty");
        }

        pathComponents = new ArrayList<>();
        // add an existing path
        pathComponents.addAll((Collection<String>) path.pathComponents);

        // add new path component
        pathComponents.add(component);
    }

    /** Creates a new path from a path string.

        <p>
        The string is a sequence of components delimited with forward slashes.
        Empty components are dropped. The string must begin with a forward
        slash.

        @param path The path string.
        @throws IllegalArgumentException If the path string does not begin with
                                         a forward slash, or if the path
                                         contains a colon character.
     */
    public Path(String path)
    {
        if (path == null) {
          throw new IllegalArgumentException (" The path cannot be null!");
        }
        if (!path.startsWith("/") || path.contains(":")) {
          throw new IllegalArgumentException ("Invalid! Please begin with a forward slash, and do not contain colon!");
        }

        pathComponents = new ArrayList<>();
        String[] tokens = path.split("/");

      // Add the tockens to the path
      for (String token : tokens) {
          if (!token.isEmpty()) {
              pathComponents.add(token);
          }
      }
      /* we can also use
      StringTokenizer st = new StringTokenizer(path,"/");
      while (st.hasMoreTokens()) {
      pathComponents.add(st.nextToken());
      } */

    }

    public Path(ArrayList<String> pathComponents) {
    	this.pathComponents = pathComponents;
    }

    /** Returns an iterator over the components of the path.

        <p>
        The iterator cannot be used to modify the path object - the
        <code>remove</code> method is not supported.

        @return The iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
        return new PathIterator (pathComponents.iterator());
    }

    private class PathIterator implements Iterator<String> {
      private Iterator<String> iterator;

      public PathIterator (Iterator<String> iterator) {
        this.iterator = iterator;
      }

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public String next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException ("The iterator cannot be used to modify the path object");
      }

    }

    /** Lists the paths of all files in a directory tree on the local
        filesystem.

        @param directory The root directory of the directory tree.
        @return An array of relative paths, one for each file in the directory
                tree.
        @throws FileNotFoundException If the root directory does not exist.
        @throws IllegalArgumentException If <code>directory</code> exists but
                                         does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException
    {
        if (!directory.exists()) {
          throw new FileNotFoundException ("root directory does not exist!");
        }
        if (!directory.isDirectory()) {
          throw new IllegalArgumentException ("directory does not refer to a directory!");
        }

        List<String> files = goList (directory, "");
        int len = files.size();
        Path[] pathes = new Path[len];

        int begin = directory.getName().length() + 1;
        for (int i = 0; i < len; i++) {
          pathes[i] = new Path(files.get(i).substring(begin));
        }
        return pathes;

    }

    public static List<String> goList (File f, String s) {
      List<String> getfile = new ArrayList<>();

      if (f.isFile()) {
        getfile.add (s + "/" + f.getName());
      } else {
        String p = s + "/" + f.getName();
        for (File file : f.listFiles()) {
          getfile.addAll (goList(file, p));
        }
      }
      return getfile;
    }

    /** Determines whether the path represents the root directory.

        @return <code>true</code> if the path does represent the root directory,
                and <code>false</code> if it does not.
     */
    public boolean isRoot()
    {
        return pathComponents.isEmpty();
    }

    /** Returns the path to the parent of this path.
     
        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no parent.
     */
    public Path parent()
    {
        if (this.isRoot()) {
          throw new IllegalArgumentException ("root directory has no parent!");
        }

        ArrayList<String> parentpath = new ArrayList<>();
        parentpath.addAll(pathComponents);

        // remove the last one
        parentpath.remove(pathComponents.size()-1);

        return new Path(parentpath);
    }

    /** Returns the last component in the path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no last
                                         component.
     */
    public String last()
    {
      if (this.isRoot()) {
        throw new IllegalArgumentException ("root directory has no parent!");
      }
      return pathComponents.get(pathComponents.size() - 1);

    }

    /** Determines if the given path is a subpath of this path.

        <p>
        The other path is a subpath of this path if it is a prefix of this path.
        Note that by this definition, each path is a subpath of itself.

        @param other The path to be tested.
        @return <code>true</code> If and only if the other path is a subpath of
                this path.
     */
    public boolean isSubpath(Path other)
    {
        Iterator<String> otherIterator = other.iterator();
        Iterator<String> thisIterator  = this.iterator();
        //LinkedList<String> otherComponents = other.pathComponents;
        // check the length
        if (other.pathComponents.size() > pathComponents.size()) {
          return false;
        }
        // check if equal
        while(otherIterator.hasNext()) {
          if (!otherIterator.next().equals (thisIterator.next())) {
            return false;
          }
        }
        return true;
    }

    /** Converts the path to <code>File</code> object.

        @param root The resulting <code>File</code> object is created relative
                    to this directory.
        @return The <code>File</code> object.
     */
    public File toFile(File root)
    {
        return new File(root.getPath().concat(this.toString()));
    }

    /** Compares this path to another.

        <p>
        An ordering upon <code>Path</code> objects is provided to prevent
        deadlocks between applications that need to lock multiple filesystem
        objects simultaneously. By convention, paths that need to be locked
        simultaneously are locked in increasing order.

        <p>
        Because locking a path requires locking every component along the path,
        the order is not arbitrary. For example, suppose the paths were ordered
        first by length, so that <code>/etc</code> precedes
        <code>/bin/cat</code>, which precedes <code>/etc/dfs/conf.txt</code>.

        <p>
        Now, suppose two users are running two applications, such as two
        instances of <code>cp</code>. One needs to work with <code>/etc</code>
        and <code>/bin/cat</code>, and the other with <code>/bin/cat</code> and
        <code>/etc/dfs/conf.txt</code>.

        <p>
        Then, if both applications follow the convention and lock paths in
        increasing order, the following situation can occur: the first
        application locks <code>/etc</code>. The second application locks
        <code>/bin/cat</code>. The first application tries to lock
        <code>/bin/cat</code> also, but gets blocked because the second
        application holds the lock. Now, the second application tries to lock
        <code>/etc/dfs/conf.txt</code>, and also gets blocked, because it would
        need to acquire the lock for <code>/etc</code> to do so. The two
        applications are now deadlocked.

        @param other The other path.
        @return Zero if the two paths are equal, a negative number if this path
                precedes the other path, or a positive number if this path
                follows the other path.
     */
    @Override
    public int compareTo(Path other)
    {
        if (this.equals (other)) {
          return 0;
        } else if (!this.isSubpath (other)) {
            return 1;
        } else {
            return -1;
        }
    }

    /** Compares two paths for equality.

        <p>
        Two paths are equal if they share all the same components.

        @param other The other path.
        @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other)
    {
      return this.toString().equals(other.toString());
    }

    /** Returns the hash code of the path. */
    @Override
    public int hashCode()
    {
        return this.pathComponents.hashCode();
    }

    /** Converts the path to a string.

        <p>
        The string may later be used as an argument to the
        <code>Path(String)</code> constructor.

        @return The string representation of the path.
     */
    @Override
    public String toString()
    {
        String pathString = "";
        if (this.isRoot()) {
          pathString += "/";
          return pathString;
        }

        for (String pc : pathComponents) {
          pathString += "/" + pc;
        }
        return pathString;
    }
}
