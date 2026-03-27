package javalibs;

import java.nio.file.*;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 5/11/15
 */
public class DirWatcher{
    private String dirPath;
    private volatile boolean logHash;
    private volatile boolean printHash;
    private String hashSaveFile;
    private final FileHasher fileHasher = FileHasher.get();
    private final TSL log = TSL.get();

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        DirWatcher that = (DirWatcher) o;

        if(!dirPath.equals(that.dirPath)) return false;
        return hashSaveFile.equals(that.hashSaveFile);
    }

    @Override
    public int hashCode(){
        int result = dirPath.hashCode();
        result = 31 * result + hashSaveFile.hashCode();
        return result;
    }

    public DirWatcher(){
        this.logHash = true;
        this.printHash = true;
    }

    /**
     * Allow the user to stop the hashing without killing the instance
     * Can be restarted by calling watchAndLogHash or watchAndPrintHash
     */
    public void killLogWatcher(){
        this.logHash = false;
    }

    public void killPrintWatcher(){
        this.printHash = false;
    }

    /**
     * Watches the directory specified at instantiation
     * Will hash any new or modified files
     * Log format: filename: hash string
     * Will not delete old hashes for modified files (yet...)
     */
    public void watchAndLogHash(String path, String saveFile){
        this.dirPath = path;
        this.hashSaveFile = saveFile;
        this.logHash = true;
        new Thread(() -> {
            try(WatchService watcher = FileSystems.getDefault().newWatchService()){
                Paths.get(dirPath).register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                while(logHash){
                    String hashStr = watchDir(watcher);
                    if(hashStr != null){
                        log.info(hashStr);
                    }
                }
            }
            catch(Exception e){
                log.exception(e);
            }
            log.info("Log thread killed");
        }).start();
    }

    public void watchAndLogHash(String path){
        this.watchAndLogHash(path, "file_hashes.txt");
    }

    /**
     * Watches the directory specified at instantiation
     * Will hash any new or modified files
     * Print format: filename: hash string
     * Will not delete old hashes for modified files (yet...)
     */
    public void watchAndPrintHash(String path){
        this.dirPath = path;
        this.printHash = true;
        new Thread(() -> {
            try(WatchService watcher = FileSystems.getDefault().newWatchService()){
                Paths.get(dirPath).register(watcher,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                while(printHash){
                    String hashStr = watchDir(watcher);
                    if(hashStr != null){
                        Out.get().writeln(hashStr);
                    }
                }
            }
            catch(Exception e){
                log.exception(e);
            }
            log.info("Print thread killed");
        }).start();
    }

    /**
     * Hashes any files that are created or modified in the watched directory.
     * Returns null for:
     *  Attempts to hash a directory
     *  Attempts to hash sym links
     *  Probably some other things
     * @return hash string or null
     */
    private String watchDir(WatchService watcher){
        if(this.dirPath == null) return null;

        try{
            WatchKey watchKey = watcher.take();
            for(WatchEvent<?> event : watchKey.pollEvents()){
                WatchEvent.Kind<?> kind = event.kind();

                // ENTRY_DELETE isn't registered, but lets double check
                if("ENTRY_CREATE".equals(kind.name()) ||
                        "ENTRY_MODIFY".equals(kind.name())){

                    String fileName = event.context().toString();
                    String fullPath = Paths.get(dirPath, fileName).toString();
                    return fileName + ": " + fileHasher.hash(fullPath);
                }
            }
        }
        catch(Exception e){
            log.exception(e);
        }

        return null;
    }
}
