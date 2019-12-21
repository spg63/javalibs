package javalibs;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * Copyright 2019 Sean Grimes. All rights reserved.
 * License: MIT License
 *
 * Most functionality is provided by https://github.com/oshi/oshi with some logic to
 * handle situations where oshi output is not available.
 *
 * An important note:
 *  Much of the information here is slow to gather, relative to CPU time. Determining
 *  the number of physical processors, as a point of reference, is a 2-5 millisecond
 *  operation on my system. This could vary wildly based on OS and hardware. These are
 *  not cheap operations to perform - at least the first time. See below.
 *
 * A happy surprise:
 *  Oshi, happily, seems to memoize as many hardware / os related calls as possible.
 *  This is fantastic news. As an example the first call to osName takes 3500
 *  microseconds on my machine while the second call takes 5 microseconds.
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

    public String BAD_NETWORK_ATEMPT = "NETWORK TIMEOUT";
    private TSL log;

    private SysHelper(){
        this.log = TSL.get();
        this.si = new SystemInfo();

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
    public String osName() {
        if(osInfoAvailable)
            return os.getFamily();
        log.info("Oshi osName unavailable, returning System Property os.name");
        return sysPropertyImlp("os.name");
    }

    /**
     * Get the specific build information for the OS. Will fallback to System.getProperty
     * with "os.name" key if full os information is not available
     * @return OS build information, as String
     */
    public String osVersionFullInfo() {
        if(osInfoAvailable)
            return this.osFullInfo.toString();
        log.warn("Oshi osVersionFullInfo unavailable, returning os.name");
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
     * Gets OS manufacturer depending on available information
     * @return The os manufacturer or an empty string
     */
    public String osManufacturer() {
        if(osInfoAvailable)
            return os.getManufacturer();
        log.warn("Unable to obtain manufacturer information");
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
    public int physicalCPUCoreCount() {
        if(oshiOkay)
            return this.hal.getProcessor().getPhysicalProcessorCount();
        // Most systems, with hyperthreading, run with 2 logical cores per physical
        // core. This is not uniform, e.g. SPARC CPUs run more than 2 logical cores.
        // This is an attempt based on the hardware *I* use.
        log.warn("Attempting to approximate physical CPU core count");
        return Runtime.getRuntime().availableProcessors() / 2;
    }

    /**
     * Try to determine the max frequency of the CPU
     * @return The max frequency, else 0
     */
    public long cpuMaxBaseFreq() {
        if(oshiOkay)
            return hal.getProcessor().getMaxFreq();
        log.warn("CPU Max frequency unavailable");
        return 0L;
    }

    /**
     * Attempt to get the current CPU temperature in Celsius
     * @return Return the CPU temp if available, else 0
     */
    public double cpuTemp() {
        if(oshiOkay)
            return hal.getSensors().getCpuTemperature();
        log.warn("CPU temperature unavailable");
        return 0.0;
    }

    /**
     * Attemps to return the total number of logical CPUs available.
     * @return Number of logical processors available
     */
    public int reportedCPUCoreCount() {
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
        return reportedCPUCoreCount() > physicalCPUCoreCount();
    }

    /**
     * Tries to determine the machine's hostname
     * @return The machine's hostname if available else empty string
     */
    public String hostName() {
        if(oshiOkay)
            return os.getNetworkParams().getHostName();
        log.warn("hostname unavailale");
        return "";
    }

    /**
     * Tries to determine the hardware manufacturer
     * @return The manufacturer if available else emoty string
     */
    public String hardwareManufacturer() {
        if(oshiOkay)
            return hal.getComputerSystem().getManufacturer();
        log.warn("No manufacturer information available");
        return "";
    }

    /**
     * Tries to determine the machine's model
     * @return The model if available else empty string
     */
    public String hardwareModel() {
        if(oshiOkay)
            return hal.getComputerSystem().getModel();
        log.warn("No manufacturer information available");
        return "";
    }

    /**
     * Tries to determine the hardware serial number
     * @return The serial number if available else empty string
     */
    public String hardwareSerial() {
        if(oshiOkay)
            return hal.getComputerSystem().getSerialNumber();
        log.warn("No manufacturer information available");
        return "";
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

    /**
     * Returns true if verified AC power, else false. False doesn't guarantee battery
     * power, it does guarantee that the function couldn't say for sure that the
     * machine is running on AC power
     * @return True if on A/C and verified, else false
     */
    public boolean runningOnAC() {
        PowerSource battery = findValidBattery();
        if(battery == null){
            log.warn("Unable to find valid battery. runningOnAC is assuming this is due" +
                    " to the lack of battery, and therefore, the presence of AC power");
            return true;
        }

        // Let Oshi determine if verified A/C power
        return battery.isPowerOnLine();
    }

    /**
     * Returns an estimated runtime on battery as reported by the OS where available.
     * If the system appears to be plugged in the result will be Double.MAX_VALUE. If
     * information is unavailable the result will be -1.0.
     * @return Estimated runtime on battery in seconds
     */
    public double batteryTimeRemainingSeconds() {
        PowerSource battery = findValidBattery();
        if(battery == null){
            log.warn("Unable to find valid battery");
            return -1.0;
        }

        double timeRemaining = battery.getTimeRemainingEstimated();
        // Oshi was able to determine runtime, reporting it to user
        if(timeRemaining > 0.0)
            return timeRemaining;

        // -2.0 indicates unlimited time, i.e. running on A/C
        if(Double.compare(-2.0, timeRemaining) == 0)
            return Double.MAX_VALUE;

        // Oshi couldn't determine runtime
        return -1.0;
    }

    /**
     * Try to determine the current cycle count on the battery, if available
     * @return The current battery cycle count, else -1
     */
    public int batteryCycleCount() {
        PowerSource battery = findValidBattery();
        if(battery == null){
            log.warn("Unable to find valid battery");
            return -1;
        }

        return battery.getCycleCount();
    }

    private PowerSource findValidBattery() {
        if(!oshiOkay) return null;
        PowerSource[] pss = hal.getPowerSources();
        if(pss == null || pss.length <= 0) return null;

        for(PowerSource ps : pss){
            // If any of these are true then oshi almost certainly found a battery
            // power source
            if(ps.isDischarging() || ps.isCharging() || ps.getMaxCapacity() > 0)
                return ps;
        }

        // Couldn't find a valid PS
        return null;
    }

    /**
     * Get the user's username
     * @return The username of the user
     */
    public String userName() { return sysPropertyImlp("user.name"); }

    /**
     * Get the user's home directory
     * @return The path of the user's home directory
     */
    public String userHome() { return sysPropertyImlp("user.home"); }

    /**
     * Get the current working directory
     * @return Path of the current working directory
     */
    public String userWorking() { return sysPropertyImlp("user.dir"); }

    /**
     * Get the version of the running JVM
     * @return JVM version
     */
    public String javaVer() { return sysPropertyImlp("java.version"); }

    private String sysPropertyImlp(String key) { return System.getProperty(key); }

    /**
     * Get the external IP address when available
     * @return The external IP address if available else NETWORK_UNAVAILABLE
     */
    public String externalIPAddr() {
        URL ipChecker = null;
        BufferedReader in = null;
        String ip = null;
        URLConnection urlConn = null;
        try {
            //ipChecker = new URL("http://checkip.amazonzws.com");
            ipChecker = new URL("http://whatismyip.akamai.com/");
            urlConn = ipChecker.openConnection();
            urlConn.setConnectTimeout(2500);
            in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            ip = in.readLine();
        }
        catch (Exception e) {
            log.exception(e);
            return BAD_NETWORK_ATEMPT;
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
            return BAD_NETWORK_ATEMPT;
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
