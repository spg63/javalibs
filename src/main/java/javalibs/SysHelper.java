package javalibs;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2019 Sean Grimes. All rights reserved.
 * License: MIT License
 *
 * An important note:
 *  Much of the information here is slow to gather, relative to CPU time. Determining
 *  the number of physical processors, as a point of reference, is a 4-6 millisecond
 *  operation on my system. This could vary wildly based on OS and hardware. These are
 *  not cheap operations to perform.
 */
public class SysHelper {
    private static volatile SysHelper _instance;

    // Oshi objects
    private SystemInfo si = null;
    private OperatingSystem os = null;
    private HardwareAbstractionLayer hal = null;
    private OperatingSystem.OSVersionInfo osFullInfo = null;
    private boolean osInfoAvailable = false;
    private boolean oshiOkay = true;

    public String BAD_NETWORK_ATEMPT = "TIMEOUT";
    private TSL log;
    private Logic logic;

    private SysHelper(){
        this.log = TSL.get();
        this.logic = Logic.get();
        this.si = new SystemInfo();
        this.logic.require(this.si != null);

        this.os = si.getOperatingSystem();
        this.hal = si.getHardware();

        // If Oshi fails for any reason we'll try running in a fallback mode with
        // limited information available
        if(this.si == null || this.os == null || this.hal == null) {
            this.oshiOkay = false;
            log.warn("Ohsi failed to initialize properly, SysHelper in fallback mode");
        }
        else{
            log.trace("Ohsi initialized properly");
        }


        if(this.oshiOkay) {
            this.osFullInfo = this.os.getVersionInfo();
            if (this.osFullInfo != null) {
                this.osInfoAvailable = true;
                log.trace("Ohsi full OS information available");
            }
            else
                log.warn("Oshi unable to initialize full OS information");
        }
    }

    public static SysHelper get(){
        if(_instance == null){
            synchronized (SysHelper.class){
                _instance = new SysHelper();
            }
        }
        return _instance;
    }

    /**
     * Get the operating system name
     * @return OS name, as String
     */
    public String osName() { return os.getFamily(); }

    /**
     * Get the specific build information for the OS. Will fallback to System.getProperty
     * with "os.name" key if full os information is not available
     * @return OS build information, as String
     */
    public String osVersionFullInfo() {
        if(osInfoAvailable)
            return this.osFullInfo.toString();
        return sysPropertyImlp("os.name");
    }

    /**
     * Gets the OS build number where available
     * @return The build number or an empty string
     */
    public String osBuildNumber() {
        if(osInfoAvailable)
            return os.getVersionInfo().getBuildNumber();
        log.warn("Unable to obtain OS build number");
        return "";
    }

    /**
     * Gets the OS code name where available
     * @return The code name or an empty string
     */
    public String osCodeName() {
        if(osInfoAvailable)
            return os.getVersionInfo().getCodeName();
        log.warn("Unable to obtain OS code name");
        return "";
    }

    /**
     * Gets the OS version where available
     * @return The os version of an empty string
     */
    public String osVer() {
        if(osInfoAvailable)
            return os.getVersionInfo().getVersion();
        log.warn("Unable to obtain OS version");
        return "";
    }

    /**
     * Attempts to determine the number of physical processor cores
     * @return The total physical cores if available, else an approximation
     */
    public int getPhysicalCPUCoreCount() {
        if(oshiOkay)
            return this.hal.getProcessor().getPhysicalProcessorCount();
        // Most systems, with hyperthreading, run with 2 logical cores per physical
        // core. This is not uniform, some SPARC CPUs run more than 2 logical cores.
        // This is an attempt based on the hardware *I* use.
        log.warn("Attempting to approximate physical CPU core count");
        return Runtime.getRuntime().availableProcessors() / 2;

    }

    /**
     * Attemps to return the total number of logical CPUs available.
     * @return
     */
    public int getReportedCPUCoreCount() {
        if(oshiOkay)
            return this.hal.getProcessor().getLogicalProcessorCount();
        log.warn("Unable to guarantee results of getReportedCPUCoreCount");
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Attempts to determine if hyperthreading is enabled
     * @return True if it seems like hyperthreading is enable, else false.
     */
    public boolean hasHyperThreading() {
        return getReportedCPUCoreCount() > getPhysicalCPUCoreCount();
    }

    /**
     * Attempts to determine how long the machine has been up for
     * @return Uptime in seconds if available, else -1
     */
    public long uptimeSeconds() {
        if(!oshiOkay) {
            log.warn("Unable to determine system uptime");
            return -1;
        }

        long uptime = this.os.getSystemUptime();
        if(uptime > 0)
            return uptime;

        log.warn("System uptime reported value is incorrect");
        return -1;
    }

    /**
     * Get a good looking representation of the total system uptime
     * @return Uptime in the x day, x hour, x minute, x second format
     */
    public String prettyUptime() {
        long uptime = uptimeSeconds();
        if(uptime == -1) return "Uptime unavailable";

        long days = TimeUnit.SECONDS.toDays(uptime);
        uptime -= TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(uptime);
        uptime -= TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(uptime);
        uptime -= TimeUnit.MINUTES.toSeconds(minutes);

        String day = days > 1 ? " days, " : " day, ";
        String hour = hours > 1 ? " hours, " : " hour, ";
        String minute = minutes > 1 ? " minutes, " : " minute, ";
        String second = uptime > 1 ? " seconds" : " second";

        String pretty = days + day + hours + hour + minutes + minute + uptime + second;
        return pretty;
    }

    /**
     * Determine if running as sudo / Administrator
     * @return True if elevated permissions else false
     */
    public boolean elevated() {
        if(oshiOkay)
            return this.os.isElevated();
        log.warn("Unable to determine elevated status");
        return false;
    }

    public String test() {
        return this.os.getManufacturer();
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
            log.exception(e);
        }
        finally {
            if(in != null) {
                try {
                    in.close();
                }
                catch(IOException e){
                    log.exception(e);
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
            return "JVM max memory reported as unlimited";
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
        https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-
        readable-format-in-java
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
