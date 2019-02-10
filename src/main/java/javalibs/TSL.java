package javalibs;


import java.io.*;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, spg63@drexel.edu
 * @author Andrew W.E. McDonald
 * @since 6/6/15
 * License: MIT License
 */

@SuppressWarnings({"unused", "WeakerAccess", "SpellCheckingInspection"})
public class TSL extends Thread{
    // NOTE: Using an enum here would be ideal, but enums in java don't appear to
    // correspond to int values which means they can't be added to strings. So, without
    // wasting an hour figuring out the "right" way to do this, I'm just going to define
    // some ints.
    private static final Integer INFO       = 0;
    private static final Integer WARN       = 1;
    private static final Integer ERROR      = 2;
    private static final Integer EXCEPTION  = 3;
    private static final Integer TRACE      = 4;
    private static final Integer DEBUG      = 5;

    private static volatile TSL _instance;
    private static String reWriteLogPath = "logs" + File.separator + "tslog.log";
    public static boolean LOG_TRACE         = true;
    public static boolean LOG_DEBUG         = true;
    public static boolean LOG_INFO          = true;
    public static boolean LOG_WARN          = true;
    public static boolean ALLOW_REQUIRE     = true;
    public static boolean LOG_TO_CONSOLE    = true;
    public static boolean REWRITE_LOG_FILE  = false;

    private String SHUTDOWN_REQ;
    private volatile boolean shuttingDown, loggerTerminated;
    private BlockingQueue<Object> itemsToLog = new ArrayBlockingQueue<>(1000000);
    @SuppressWarnings("FieldCanBeLocal")
    private LocalDateTime ldt;
    private String dt;
    private Out out;
    private FileUtils futils;

    private TSL(){
        this.SHUTDOWN_REQ = "SHUTDOWN";
        this.shuttingDown = false;
        this.loggerTerminated = false;
        this.out = Out.get();
        this.futils = FileUtils.get();
        LocalDateTime dt = LocalDateTime.now();
        this.dt = dt.toString().replace("T", "_").replace(":","_");

        // Start the logger
        start();
    }

    /**
     * Get reference to the logger
     * @return A reference to the logger
     */
    public static TSL get(){
        if(_instance == null){
            synchronized(TSL.class){
                if(_instance == null){
                    _instance = new TSL();
                }
            }
        }
        return _instance;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run(){
        this.futils.checkAndCreateDir("logs");
        PrintWriter pw = null;
        try{
            String item;
            try{
                if(!REWRITE_LOG_FILE) {
                    pw = new PrintWriter(
                            new BufferedWriter(
                                    new FileWriter("logs/tslogs_" + dt + ".txt", true)));
                }
                else{
                    pw = new PrintWriter(
                            new BufferedWriter(
                                    new FileWriter(reWriteLogPath)));
                }
            }
            catch(IOException e){
                out.writeln_err("*** ThreadSafeLogger IOException");
            }

            while(!(item = (String)itemsToLog.take()).equals(SHUTDOWN_REQ)){
                String label;

                if(Character.getNumericValue(item.charAt(0)) == INFO)
                    label = "[INF] ";
                else if(Character.getNumericValue(item.charAt(0)) == TRACE)
                    label = "[TRC] ";
                else if(Character.getNumericValue(item.charAt(0)) == DEBUG)
                    label = "[DBG] ";
                else if(Character.getNumericValue(item.charAt(0)) == WARN)
                    label = "[WAR] ";
                else if(Character.getNumericValue(item.charAt(0)) == ERROR)
                    label = "[ERR] ";
                else
                    label = "[EXP] ";

                // Split the init lable off the string
                String[] splitItem = item.split(" ", 2);

                StringBuilder sb = new StringBuilder();
                sb.append(label);
                sb.append(time_str());
                sb.append(splitItem[1]);
                pw.println(sb.toString());
                pw.flush();
                if(LOG_TO_CONSOLE)
                    this.out.writeln(sb.toString());
            }
        }
        catch(InterruptedException e){
            this.out.writeln_err("ThreadSafeLogger -- I was interrupted");
        }
        finally{
            loggerTerminated = true;
            if(pw != null)
                pw.close();
        }
    }

    /**
     * javalibs.Log info
     * @param str The log message
     */
    public void info(Object str){
        if(!LOG_INFO || shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put(INFO.toString() + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.info() -- " +
                    "Unexpected interruption");
        }
    }

    /**
     * javalibs.Log warnings
     * @param str The log message
     */
    public void warn(Object str){
        if(!LOG_WARN || shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put(WARN.toString() + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.warn() -- " +
                    "Unexpected interruption");
        }
    }

    /**
     * javalibs.Log errors
     * @param str The log message
     */
    public void err(Object str){
        if(shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put(ERROR.toString() + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.err() -- " +
                    "Unexpected interruption");
        }
    }

    /**
     * javalibs.Log trace
     * @param str The log message
     */
    public void trace(Object str){
        if(!LOG_TRACE || shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put(TRACE.toString() + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.trace() -- " +
                    "Unexcepted interruption");
        }
    }

    /**
     * javalibs.Log debug
     * @param str The log message
     */
    public void debug(Object str){
        if(!LOG_DEBUG || shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put(DEBUG.toString() + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.debug() -- " +
                    "Unexpected interruption");
        }
    }

    private void exception(Object str){
        if(shuttingDown || loggerTerminated)
            return;
        try{
            itemsToLog.put(EXCEPTION.toString() + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException(("ThreadSafeLogger.exception() -- " +
                    "Unexpected interruption"));
        }
    }

    /**
     * javalibs.Log an exception
     * @param e The exception to be logged
     */
    public void exception(Exception e){
        if(null == e) return;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String trace = sw.toString();
        exception("\n" + trace);
        pw.close();
    }

    /**
     * Shutdown the logger, thread will sleep for 1000ms to allow proper flushing
     * NOTE: This does not kill the program, it just shutsdown the logger
     */
    public void shutDown() {
        shuttingDown = true;
        try {
            itemsToLog.put(SHUTDOWN_REQ);
            // Force a pause of the main thread to give the logger thread a chance to
            // write all data to the file system
            Thread.sleep(1000);
        }
        catch(InterruptedException e){
            throw new RuntimeException("ThreadSafeLogger.shutDown() -- " +
                    "Unexpected interruption");
        }
    }

    /**
     * Shutdown the logger, sleep for a second to allow the logger to finish flushing to
     * disk then kill the program with exit code
     * NOTE: This traps the calling thread!
     */
    public void die(){
        shuttingDown = true;
        try{
            itemsToLog.put(SHUTDOWN_REQ);
            Thread.sleep(2000);
            System.exit(0);
        }
        catch(InterruptedException e){
            System.exit(6);
        }
    }

    /**
     * Shutdown the logger, adding a final log message onto the queue before killing the
     * program
     * @param log_message The message ot be logged before shutdown
     */
    public void die(String log_message){
        err(log_message);
        die();
    }

    public void die(Exception e){
        exception(e);
        die();
    }

    /**
     * This function will automatically log an INFO message for the calling class name,
     * function name, and line number of the function call. It also accepts an optional
     * log message. Function is useful to see where execution may have stopped, or
     * where a specific area of interest shows up without forcing the user to add line
     * specific or message specific messages to the logger call.
     * NOTE: It's not a particularily fast function to call
     * @param log_message Message to add to the Class name, function name, and line number
     */
    public void autoLog(String log_message){
        info(getStackInfo(log_message));
    }

    /**
     * See full description above: autoLog call without log message
     */
    public void autoLog(){
        autoLog("");
    }

    /**
     * Builds the stack information string for autologging
     */
    private String getStackInfo(Object msg){
        // Get all stack frame for the calling thead
        StackTraceElement[] stackFrames = Thread.currentThread().getStackTrace();

        // Note: Depending on the JVM the frame index could be different. However, if we
        // find the frame immediately after the frame for this function, that *should*
        // give the frame for the calling function.

        int thisFunctionFrameIndex = -1;
        String thisFunctionName = "getStackInfo";
        for(int i = 0; i < stackFrames.length; ++i)
            if(thisFunctionName.equals(stackFrames[i].getMethodName()))
                thisFunctionFrameIndex = i;

        if(thisFunctionFrameIndex == -1){
            err(thisFunctionName + " unable to do anything at all. This sucks.");
            return "";
        }

        // Need to increment by 2 since this is called internally by autolog or require
        int frameOfInterest = thisFunctionFrameIndex + 2;
        if(frameOfInterest >= stackFrames.length){
            err(thisFunctionName + " unable to do anything at all. This sucks.");
            return "";
        }

        int internalCaller = thisFunctionFrameIndex + 1;
        String callerName = "";
        if(internalCaller < stackFrames.length)
            callerName = stackFrames[internalCaller].getMethodName();

        StackTraceElement elementOfInterest = stackFrames[frameOfInterest];
        StringBuilder builder = new StringBuilder();
        builder.append("\n\t *** " + callerName + " *** \n");
        builder.append("\t Class name:      " + elementOfInterest.getClassName() + "\n");
        builder.append("\t Function name:   " + elementOfInterest.getMethodName() + "\n");
        builder.append("\t Line number:     " + elementOfInterest.getLineNumber() + "\n");
        builder.append("\t Log message:     " + msg);
        return builder.toString();
    }

    /**
     * Similar to assertTrue, will kill the program if trueToLive is false, however it
     * kills it from the logger, automatically giving you function, line information.
     * Essentially the info from an exception while also letting the logger die properly
     * @param trueToLive True or false, false dies
     * @param msg Additional log message
     */
    public void require(Boolean trueToLive, Object msg){
        // Require can be skipped if we want to live dangerously, or for, like, production
        if(!ALLOW_REQUIRE || trueToLive) return;
        die(getStackInfo(msg));
    }

    /**
     * Similar to assertTrue, will kill the program if trueToLive is false, however it
     * kills it from the logger, automatically giving you function, line information.
     * Essentially the info from an exception while also letting the logger die properly
     * @param trueToLive
     */
    public void require(Boolean trueToLive){
        // Can't just pass empty string to require above, will add a stack frame
        // element to the stack frame stack
        if(!ALLOW_REQUIRE || trueToLive) return;
        die(getStackInfo(""));
    }

    private String time_str(){
        ldt = LocalDateTime.now();
        int hour = ldt.getHour();
        int min = ldt.getMinute();
        int sec = ldt.getSecond();
        long milli = TimeUnit.NANOSECONDS.toMillis(ldt.getNano());

        String hour_s = hour < 10 ? ("0" + hour) : Integer.toString(hour);
        String min_s = min < 10 ? ("0" + min) : Integer.toString(min);
        String sec_s = sec < 10 ? ("0" + sec) : Integer.toString(sec);
        String mil_s;
        if(milli > 10 && milli < 100)
            mil_s = "0" + milli;
        else if(milli < 10)
            mil_s = "00" + milli;
        else
            mil_s = Long.toString(milli);

        return "("+hour_s+":"+min_s+":"+sec_s+"."+mil_s+") > ";
    }
}
