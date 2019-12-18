package javalibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class SysHelper {
    private static volatile SysHelper _instance;
    public String BAD_NETWORK_ATEMPT = "TIMEOUT";
    private SysHelper(){ }

    public static SysHelper get(){
        if(_instance == null){
            synchronized (SysHelper.class){
                _instance = new SysHelper();
            }
        }
        return _instance;
    }

    public String osName() { return sysPropertyImlp("os.name"); }

    public String osVer() { return sysPropertyImlp("os.version"); }

    public String availableProcs() {
        return Integer.toString(Runtime.getRuntime().availableProcessors());
    }

    public String userName() { return sysPropertyImlp("user.name"); }

    public String userHome() { return sysPropertyImlp("user.home"); }

    public String userWorking() { return sysPropertyImlp("user.dir"); }

    public String javaVer() { return sysPropertyImlp("java.version"); }


    private String sysPropertyImlp(String key) { return System.getProperty(key); }

    public String getIPAddr() {
        // Do this in a thread!!!
        URL ipChecker = null;
        BufferedReader in = null;
        String ip = null;
        try {
            ipChecker = new URL("http://checkip.amazonzws.com");
            in = new BufferedReader(new InputStreamReader(ipChecker.openStream()));
            ip = in.readLine();
        }
        catch (IOException e) {
            TSL.get().exception(e);
        }
        finally {
            if(in != null) {
                try {
                    in.close();
                }
                catch(IOException e){
                    TSL.get().exception(e);
                }
            }
        }

        if(ip == null){
            return "";
        }

        return ip;
    }

    /**
     * Returns the max available memory as reported by the Runtime
     * Will return in GB / MB / KB or bytes based on the most appropriate unit
     * @return Runtime reported max memory
     */
    public String maxMem() {
        long maxMem = Runtime.getRuntime().maxMemory();
        if(maxMem == Long.MAX_VALUE)
            return "JVM max memory unlimited";
        else
            return appropriateSizeUnit(maxMem);
    }

    /**
     * Returns the total available memory *now* as reported by the Runtime
     * Will return in GB / MB / KB or bytes based on the most appropriate unit
     * @return Runtime reported total memory available *now*
     */
    public String totalMem() {
        return appropriateSizeUnit(Runtime.getRuntime().totalMemory());
    }

    /**
     * Returns the total free memeory *now* as reported by the Runtime
     * Will return in GB / MB / KB or bytes based on the most appropriate unit
     * @return Runtime reported free memory available *now*
     */
    public String freeMemory() {
        return appropriateSizeUnit(Runtime.getRuntime().freeMemory());
    }

    /*
        The most copied piece of Java ever on SO. Came with a bug, originally.
        https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
     */
    private String appropriateSizeUnit(long memValue) {
        String s = memValue < 0 ? "-" : "";
        long b = memValue == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(memValue);
        return b < 1000L ? memValue + " B"
                : b < 999_950L ? String.format("%s%.1f kB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f MB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f GB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f TB", s, b / 1e3)
                : (b /= 1000) < 999_950L ? String.format("%s%.1f PB", s, b / 1e3)
                : String.format("%s%.1f EB", s, b / 1e6);
    }

    public String diskWriteSpeed() {
        return "";
    }

    public String diskReadSpeed() {
        return "";
    }

    /**
     * Attempts to get the size of the root partition
     * NOTE: This is *NOT* robust and I only guarantee it to work on my machine
     * @return The size of the root partition
     */
    public String rootTotalSpace() {
        File[] roots = File.listRoots();
        return appropriateSizeUnit(roots[0].getTotalSpace());
    }

    /**
     * Attempts to get the total free space available on the root partition
     * NOTE: This is *NOT* robust and I only guarantee it to work on my machine
     * @return Free space on root partition
     */
    public String rootFreeSpace(){
        File[] roots = File.listRoots();
        return appropriateSizeUnit(roots[0].getUsableSpace());
    }
}
