package javalibs;


import java.io.*;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (javalibs.c) 2018 Sean Grimes. All rights reserved.
 * @author Sean Grimes, sean@seanpgrimes.com
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
    private static final int INFO       = 0;
    private static final int WARN       = 1;
    private static final int ERROR      = 2;
    private static final int EXCEPTION  = 3;
    private static final int TRACE      = 4;
    private static final int DEBUG      = 5;
    private static final int RESULTS    = 6;
    private static final int DOLIUS     = 7;
    private static final int SWARM      = 8;

    private static volatile TSL _instance;
    private static String reWriteLogPath = "logs" + File.separator + "tslog.log";
    public static boolean LOG_TRACE             = true;
    public static boolean LOG_DEBUG             = true;
    public static boolean LOG_INFO              = true;
    public static boolean LOG_WARN              = true;
    public static boolean LOG_TO_CONSOLE        = true;
    // Keep trace messages in file but not on console
    public static boolean LOG_TRACE_TO_CONSOLE  = false;
    // Keep debug messages in file but not on console
    public static boolean LOG_DEBUG_TO_CONSOLE  = true;
    public static boolean REWRITE_LOG_FILE      = true;
    public static boolean REWRITE_RESULTS       = false;

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
        this.dt = dt.toString()
                .replace("T", "_")
                .replace(":","_");

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
        PrintWriter logWriter = null;
        PrintWriter resultsWriter = null;
        PrintWriter doliusWriter = null;
        PrintWriter swarmWriter = null;
        PrintWriter inUseWriter = null;
        try{
            String item;
            try{
                if(!REWRITE_LOG_FILE) {
                    logWriter = new PrintWriter(
                            new BufferedWriter(
                                new FileWriter(
                                    "logs/tslogs_" + dt + ".txt", true
                                )
                            )
                    );
                }
                else{
                    logWriter = new PrintWriter(
                            new BufferedWriter(
                                    new FileWriter(reWriteLogPath)));
                }
                if(!REWRITE_RESULTS) {
                    resultsWriter = new PrintWriter(
                            new BufferedWriter(
                                new FileWriter(
                                    "logs/results_" + dt + ".txt", true
                                )
                            )
                    );
                }
                else {
                    resultsWriter = new PrintWriter(
                            new BufferedWriter(
                                    new FileWriter("results/results.txt")));
                }
                doliusWriter = new PrintWriter(
                        new BufferedWriter(
                            new FileWriter(
                                "logs/dolius_" + dt + ".txt", true
                            )
                        )
                );
                swarmWriter = new PrintWriter(
                        new BufferedWriter(
                                new FileWriter(
                                        "logs/swarmRes_" + dt + ".txt", true
                                )
                        )
                );

            }
            catch(IOException e){
                e.printStackTrace();
                out.writeln_err("*** ThreadSafeLogger IOException");
            }

            String label;
            while(!(item = (String)itemsToLog.take()).equals(SHUTDOWN_REQ)){

                int numFlag = Character.getNumericValue(item.charAt(0));

                switch(numFlag){
                    case TRACE: label = "[TRC] ";
                        // Need to deal with skipping console logging if false when
                        // message is a trace message. In these cases set log to console
                        // to false as a temporary measure and reset it at end of function
                                inUseWriter = logWriter;
                                break;
                    case DEBUG: label = "[DBG] ";
                                inUseWriter = logWriter;
                                break;
                    case INFO:  label = "[INF] ";
                                inUseWriter = logWriter;
                                break;
                    case WARN:  label = "[WAR] ";
                                inUseWriter = logWriter;
                                break;
                    case ERROR: label = "[ERR] ";
                                inUseWriter = logWriter;
                                break;
                    case RESULTS: label = "[RES] ";
                                inUseWriter = resultsWriter;
                                break;
                    case DOLIUS: label = "[DOL] ";
                                 inUseWriter = doliusWriter;
                                 break;
                    case SWARM: label = "[SWM] ";
                                inUseWriter = swarmWriter;
                                break;
                    default:
                                label = "[EXP] ";
                                inUseWriter = logWriter;
                }

                // Split the init lable off the string
                String[] splitItem = item.split(" ", 2);

                StringBuilder sb = new StringBuilder();
                sb.append(label);
                sb.append(time_str());
                sb.append(splitItem[1]);
                inUseWriter.println(sb.toString());
                inUseWriter.flush();
                if(LOG_TO_CONSOLE)
                    this.out.writeln(sb.toString());
            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
            this.out.writeln_err("ThreadSafeLogger -- I was interrupted");
        }
        finally{
            loggerTerminated = true;
            if(logWriter != null)
                logWriter.close();
            if(resultsWriter != null)
                resultsWriter.close();
            if(inUseWriter != null)
                inUseWriter.close();
        }
    }

    /**
     * Another horrible addition but I need swarm separate from results for identifying
     */
    public void swarm(Object str){
        if(shuttingDown || loggerTerminated) return;
        try {
            itemsToLog.put(SWARM + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.swarm() -- " +
                    "Unexpected interruption");
        }
    }

    /**
     * This is a horrible addition but I need to get results into a separate file
     */
    public void results(Object str){
        if(shuttingDown || loggerTerminated) return;
        try{
            itemsToLog.put(RESULTS + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.results() -- " +
                    "Unexpected interruption");
        }
    }

    /**
     * More horrible additons
     */
    public void dolius(Object str){
        if(shuttingDown || loggerTerminated) return;
        try{
            itemsToLog.put(DOLIUS + " " + str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.dolius() -- " +
                    "Unexpected interruption");
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
            itemsToLog.put(INFO + " " + str);
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
            itemsToLog.put(WARN + " " + str);
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
            itemsToLog.put(ERROR + " " + str);
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
            itemsToLog.put(TRACE + " " + str);
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
            itemsToLog.put(DEBUG + " " + str);
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
            itemsToLog.put(EXCEPTION + " " + str);
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
        if(shuttingDown || loggerTerminated) return;
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
            Thread.sleep(2000);
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

    public void die(Exception e, String log_message) {
        exception(e);
        err(log_message);
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
    public void autoLog(String log_message){ info(getStackInfo(log_message)); }

    /**
     * See full description above: autoLog call without log message
     */
    public void autoLog(){ autoLog(""); }

    /**
     * See above for full description, substitute INFO for ERR
     */
    public void errFrom(String errMsg){ err(getStackInfo(errMsg)); }

    public void exceptionFromAndDie(Exception e){
        // TODO Implement
    }

    /**
     * Builds the stack information string for autologging
     */
    public String getStackInfo(Object msg){
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
        return "\n\t *** " + callerName + " *** \n" +
                "\t Class name:      " + elementOfInterest.getClassName() + "\n" +
                "\t Function name:   " + elementOfInterest.getMethodName() + "\n" +
                "\t Line number:     " + elementOfInterest.getLineNumber() + "\n" +
                "\t Log message:     " + msg;
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
