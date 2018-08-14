package javalibs;

import java.nio.file.*;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 5/11/15
 */
public class DirWatcher{
    private String dir_path;
    private volatile boolean log_hash;
    private volatile boolean print_hash;
    private String hash_save_file;
    private FileHasher file_hasher;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        DirWatcher that = (DirWatcher) o;

        if(!dir_path.equals(that.dir_path)) return false;
        if(!hash_save_file.equals(that.hash_save_file)) return false;
        return file_hasher.equals(that.file_hasher);

    }

    @Override
    public int hashCode(){
        int result = dir_path.hashCode();
        result = 31 * result + hash_save_file.hashCode();
        result = 31 * result + file_hasher.hashCode();
        return result;
    }

    public DirWatcher(){
        this.log_hash = true;
        this.print_hash = true;
        this.file_hasher = new FileHasher();
    }

    /**
     * Allow the user to stop the hashing without killing the instance
     * Can be restarted by called watchAndLogHash or watchAndPrintHash
     */
    public void killLogWatcher(){
        this.log_hash = false;
    }
    public void killPrintWatcher(){
        this.print_hash = false;
    }

    /**
     * Watches the directory specified at instantiation
     * Will hash any new or modified files
     * javalibs.Log format: filename: hash string
     * Will not delete old hashes for modified files (yet...)
     */
    // NOTE: Lambda version, see watchAndPrintHash for anon-function version
    public void watchAndLogHash(String path, String save_file){
        // The dir we're watching
        this.dir_path = path;
        // Setup the save_file
        this.hash_save_file = save_file;
        // Reset boolean to true if method called again
        this.log_hash = true;
        new Thread(() -> {
            String hash_str = null;
            while(log_hash){
                hash_str = watchDir();
                if(hash_str != null){
                    c.log(hash_str);
                }
            }
            c.log("javalibs.Log thread killed");
        }).start();
    }

    public void watchAndLogHash(String path){
        this.watchAndLogHash(path, "file_hashes.txt");
    }

    /**
     * Watches the directory specified at instantiation
     * Will hash any new or modified files
     * Print format: filename: hash string
     * Will not delete old hashes for modified file (yet...)
     */
    @SuppressWarnings("Convert2Lambda")
    public void watchAndPrintHash(String path){
        this.dir_path = path;
        this.print_hash = true;
        new Thread(new Runnable(){
            @Override
            public void run(){
                String hash_str = null;
                while(print_hash){
                    hash_str = watchDir();
                    if(hash_str != null) {
                        c.writeln(hash_str);
                    }
                }
                c.log("Print thread killed");
            }
        }).start();
    }

    /**
     * Hashes any files that are created or modified in the watched directory
     * Returns null for:
     *  Attempts to hash directory
     *  Attempts to hash sym links
     *  Probably some other things
     * @return hash string or null
     */
    private String watchDir(){
        Path this_dir;
        if(this.dir_path != null)
            this_dir = Paths.get(dir_path);
        else
            return null;

        try{
            WatchService watcher = FileSystems.getDefault().newWatchService();
            this_dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                                        StandardWatchEventKinds.ENTRY_MODIFY);
            WatchKey watchKey = watcher.take();

            for(WatchEvent<?> event : watchKey.pollEvents()){
                WatchEvent.Kind<?> kind = event.kind();

                // ENTRY.DELETE isn't registered, but lets double check.
                if("ENTRY_CREATE".equals(kind.name()) ||
                        "ENTRY_MODIFY".equals(kind.name())){

                    // Name of the newly created file
                    String file_name = event.context().toString();

                    // Full path to that file
                    String path = this_dir+"/"+file_name;

                    return file_name+": "+this.file_hasher.hash(path);
                }
            }
        }
        catch(Exception e){
            c.writeln_err("WatchService Exception");
            c.logErr("WatchService Exception");
        }

        return null;
    }
}
