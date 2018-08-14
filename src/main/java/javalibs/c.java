package javalibs; /**
 *
 * Getting around some boilerplate
 *
 * @author Sean Grimes, spg63@cs.drexel.edu
 *
 */

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;


public final class c implements java.io.Serializable{
	private static final long serialVersionUID = 32L;
	private static final boolean debug_lib = false;
	private static StringBuilder buf = new StringBuilder();
	private static StringBuilder errBuf = new StringBuilder();
	private static String logFile = "logger.txt";
	private static String errLogFile = "errLog.txt";
	
	/* Debug Bools */
	// Debugging code in Driver class / main
    public static boolean debug_1 = true;
	
	// Debugging code in intermediate levels
	public static boolean debug_2 = true;
	
	// Debugging code in whatever is 'low' level
	public static boolean debug_3 = true;
	
	// Send debug statements to logger if true
	public static boolean preferLog = true;

    // Get time stamps with all logger messages
    public static boolean log_time_stamps = false;

    // Get time stamps with errLog only
    public static boolean errLog_time_stamps = false;

    // Get debug time stamps with only "debug1/2/3" calls
    public static boolean debug_time_stamps = true;

    // Extra output
    public static boolean verbose = true;
	
	
	/* Set logFile and errLogFile */
	public static void setLog(String file){
		if(file.length() > 0){
			logFile = file;
		}
		else{
			writeln_err(" *** javalibs.Log file name must be greater than 0 chars ***");
			writeln_err(" *** logs will be stored in \"logger.txt\" *** ");
		}
	}
	public static void setErrLog(String file){
		if(file.length() > 0){
			errLogFile = file;
		}
		else{
			writeln_err(" *** errLog file name must be greater than 0 chars ***");
			writeln_err(" *** errlogs will be stored in \"logger.txt\" *** ");
		}
	}

    // Don't Instantiate
	private c(){}


	/* 
	 * Logging Facilities
	 * NOTE: Without setting the shutdown hook, you must manually call
	 * flushBuf() to write "buf" to the file "logger.txt" before shutdown
	 * NOTE: The shutdown hook, like all Java shutdown hooks, will not
	 * execute code for some abnormal exits. 
	 * 	- Unless your program is called init, w/ ID=1, you can't catch -9
	 */
	public static void setShutDownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log("\n********** END OF RUN **********");
				logErr("\n********** END OF RUN **********");
				flushBuf();
				flushErrBuf();
				if (debug_lib)
					writeln("\nBuffer Flushed\n");
			}
		});
	}
	public static void log(Object s){
		buf.append("\n");
		if(log_time_stamps) {
			buf.append(LocalDateTime.now().toString());
			buf.append(": ");
			buf.append(s);
		}
        else
            buf.append(s);
		if(buf.length() >= 8192)
			flushBuf();
	}
	public static void logErr(Object s){
		errBuf.append("\n");
        if(errLog_time_stamps) {
			buf.append(LocalDateTime.now().toString());
			buf.append(": ");
			buf.append(s);
		}
        else
		    errBuf.append(s);
		if(errBuf.length() >= 1024){
			flushErrBuf();
		}
	}
	public static void flushBuf(){
		logMsg(buf);
		buf = new StringBuilder();
	}
	public static void flushErrBuf(){
		logErrMsg(errBuf);
		errBuf = new StringBuilder();
	}
	public static void getNewLogFile(){
		try{
			File f = new File(logFile);
			if(f.delete()){
				if(debug_lib){
                    writeln("logFile Deleted");
                }
			}
			else{
				if(debug_lib)
					writeln_err("logFile NOT Deleted");
			}
			if(!(f.createNewFile())) {
				writeln_err(logFile + " could not be created");
				writeln_err("Execution will continue, logs may not be saved");
			}
		}
		catch(Exception e){
			if(debug_lib) {
				writeln_err("Exception removing logFile");
				e.printStackTrace();
			}
			else{
				writeln_err("Exception in getNewLogFile, continuing");
			}
		}
	}
	public static void getNewErrLogFile(){
		try {
			File f = new File(errLogFile);
			if (f.delete()) {
				if (debug_lib)
					writeln("errLogFile Deleted");
			} else {
				if (debug_lib)
					writeln_err("errLogFile NOT Deleted");
			}
			if (!(f.createNewFile())){
				writeln_err(errLogFile + " could not be created.");
				writeln_err("Execution will continue, errLogs may not be saved");
			}
		}
		catch(Exception e){
			if(debug_lib) {
				writeln_err("Exception removing errLogFile");
				e.printStackTrace();
			}
			else{
				writeln_err("Exception in getNewErrLogFile, continuing");
			}
		}
	}
	
	
	/*
	 * Console Input / javalibs.Out
	 */
	public static void writeln(Object msg){
		System.out.println(msg);
	}
	public static void write(Object msg){
		System.out.print(msg);
	}
	public static void writef(String format, Object... args){
		System.out.print(String.format(format, args));
	}
	public static void writeln_err(Object msg){
		System.err.println(msg);
	}
	public static void write_err(Object msg){
		System.err.print(msg);
	}

	/*
	 * NOTE: debug1/2/3 methods will only print when associated debug booleans
	 * 		 found at the top of this class are true.
	 * Reason: No more if(debug) statements scattered throughout the code
	 *
	 * NOTE: when preferLog is true the debug messages will be sent to logger.txt
	 * 		 When preferLog is false, debug messages will be printed to console
	 */
	public static void debug1(Object msg){
		if(debug_1){
			if(preferLog) {
                if (debug_time_stamps)
                    log("* " + LocalDateTime.now().toString() + ": " + msg);
                else
                    log("*: " + msg);
            }
			else{
				if(debug_time_stamps)
					writeln("* " + LocalDateTime.now().toString() + ": " + msg);
				else
					writeln("*: " + msg);
			}
		}
	}
	public static void debug1(String funName, Object msg){
		if(debug_1){
			if(preferLog) {
                if(debug_time_stamps)
                    log("* " + LocalDateTime.now().toString() + ": " + funName + " - " + msg);
                else
                    log("*: " + funName + ": " + msg);
            }
			else{
				if(debug_time_stamps)
					writeln("* " + LocalDateTime.now().toString() + ": " + funName + " - " + msg);
				else
					writeln("*: " + funName + ": " + msg);
			}
		}
	}
	public static void debug2(Object msg){
		if(debug_2){
			if(preferLog) {
                if(debug_time_stamps)
                    log("** " + LocalDateTime.now().toString() + ": " + msg);
                else
                    log("**: " + msg);
            }
			else{
				if(debug_time_stamps)
					writeln("** " + LocalDateTime.now().toString() + ": " + msg);
				else
					writeln("**: " + msg);
			}
		}
	}
	public static void debug2(String funName, Object msg){
		if(debug_2){
			if(preferLog) {
                if(debug_time_stamps)
                    log("** " + LocalDateTime.now().toString() + ": " + funName + " - " + msg);
                else
                    log("**: " + funName + ": " + msg);
            }
			else{
				if(debug_time_stamps)
					writeln("** " + LocalDateTime.now().toString() + ": " + funName+ " - " + msg);
				else
					writeln("**: " + funName + ": " + msg);
			}
		}
	}
	public static void debug3(Object msg){
		if(debug_3){
			if(preferLog) {
                if(debug_time_stamps)
                    log("*** " + LocalDateTime.now().toString() + ": " + msg);
                else
                    log("***: " + msg);
            }
			else{
				if(debug_time_stamps)
					writeln("*** " + LocalDateTime.now().toString() + ": " + msg);
				else
					writeln("***: " + msg);
			}
		}
	}
	public static void debug3(String funName, Object msg){
		if(debug_3){
			if(preferLog) {
                if(debug_time_stamps)
                    log("*** " + LocalDateTime.now().toString() + ": " + funName + " - " + msg);
                else
                    log("***: " + funName + ": " + msg);
            }
			else{
				if(debug_time_stamps)
					writeln("*** " + LocalDateTime.now().toString() + ": " + funName + " - " + msg);
				else
					writeln("***: " + funName + ": " + msg);
			}
		}
	}

	/*
	 * File output for logging
	 * NOTE: No String or char[] buffer/builder, opens file and writes to file
	 * 		 This will be very slow in loops, generally better to use
	 * 		 log() from above with the shutDownHook in place in your main()
	 */
	public static void logMsg(Object msg){
		if(debug_lib)
			writeln("We're in the thread");
		try(PrintWriter out = new PrintWriter(
								new BufferedWriter(
								  new FileWriter(logFile, true)))){
			out.println(msg);
		}
		catch(IOException e){
			writeln_err("*** logMsg failed ***");
		}
	}
	public static void logErrMsg(Object msg){
		if(debug_lib)
			writeln("We're in logErrMsg thread");
		try(PrintWriter out = new PrintWriter(
								new BufferedWriter(
								  new FileWriter(errLogFile, true)))){
			out.println(msg);
		}
		catch(IOException e){
			writeln_err("*** logErrMsg failed ***");
		}
	}

	/*
	 * General Utilities functions that I've needed over time
	 */
	public static String getWorkingDir() {
		Path WD = Paths.get("");
		return WD.toAbsolutePath().toString();
	}
	public static void checkAndCreateDir(String dirName){
		String path = getWorkingDir();
		File tmp = new File(path + File.separator + dirName + File.separator);
		// Hello lolcode
		if(!tmp.exists()){
			boolean cai_haz_filz = tmp.mkdirs();
			//noinspection PointlessBooleanExpression,ConstantConditions
			if(cai_haz_filz && debug_lib)
				writeln("Wez maded sum new dir(z), cai haz sum milks now?");
		}
	}
} // Class 'javalibs.c'






















