package se.natusoft.osgi.aps.test.tools;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Scans a directory returning a list of File object for each file in the directory and subdirectories.
 */
public class DirScanner {
    //
    // Private Members
    //

    private List<String> entries = new LinkedList<>();
    private int rootDirLength = 0;

    //
    // Constructors
    //

    /**
     * Creates a new DirScanner instance and does the complete scan.
     *
     * @param root The directory to start scanning at.
     */
    public DirScanner(File root) {
        scanDir(root);
    }

    //
    // Methods
    //

    /**
     * Scans the provided root dir for all files recursively.
     *
     * @param dir The dir to scan.
     */
    private void scanDir(File dir) {
        if (dir == null) throw new IllegalArgumentException("dir cannot be null!");

        if (this.rootDirLength == 0) {
            this.rootDirLength = dir.getAbsolutePath().length();
        }

        //noinspection ConstantConditions
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                String path = file.getAbsolutePath().substring(this.rootDirLength);
                this.entries.add(path);
            }
            else if (file.isDirectory()) {
                scanDir(file);
            }
        }
    }

    /**
     * Returns the result of the scan. All file paths are relative to the root!
     */
    public List<String> getEntries() {
        return this.entries;
    }

    /**
     * Returns the entries as a stream of strings containing root relative paths.
     */
    public Stream<String> stream() {
        return StreamSupport.stream(Spliterators.spliterator(
                this.entries.iterator(), this.entries.size(),
                Spliterator.ORDERED | Spliterator.DISTINCT |
                        Spliterator.IMMUTABLE | Spliterator.NONNULL), /* parallel */ false);
    }
}
