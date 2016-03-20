import java.time.LocalDateTime;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/5/15
 */
@SuppressWarnings("unused")
public class DebugUtils{
    private static volatile DebugUtils _instance;
    private boolean debug_to_log;
    private boolean debug_time_stamps;
    private boolean debug;

    private Log logger = null;
    private Out out = null;

    private DebugUtils(){
        this.logger = Log.get();
        this.out = Out.get();
        this.debug_to_log = false;
        this.debug_time_stamps = false;
        this.debug = StateVariables_sg.enable_debug;
    }

    public static DebugUtils getInstance(){
        if(_instance == null){
            synchronized(DebugUtils.class){
                if(_instance == null){
                    _instance = new DebugUtils();
                }
            }
        }
        return _instance;
    }

    /**
     * Sends debug1 / debug2 statements to "log.txt" instead of console
     */
    public void debug_to_file(){
        this.debug_to_log = true;
    }

    /**
     * Enable time stamps for debugging statements
     */
    public void debug_time_stamps(){
        this.debug_time_stamps = true;
    }

    /**
     * debug1 is intended for higher level classes / main
     * if StateVariables.enable_debug1 is false, this will not print / log
     */
    public void debug(Object msg){
        if(debug){
            if(debug_to_log){
                if(debug_time_stamps)
                    logger.log("* "+LocalDateTime.now().toString()+": "+msg);
                else
                    logger.log("*: "+msg);
            }
            else{
                if(debug_time_stamps)
                    out.writeln("* "+LocalDateTime.now().toString()+": "+msg);
                else
                    out.writeln("*: "+msg);
            }
        }
    }

    /**
     * Intended for the user to pass the calling function name as well as a msg
     */
    public void debug(Object msg, boolean print_caller_name){
        // Abort and call above debug1
        if(!print_caller_name)
            debug(msg);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stackTrace[2]; // Get caller name
        String caller_name = e.getMethodName();

        if(debug){
            if(debug_to_log){
                if(debug_time_stamps)
                    logger.log("* "+LocalDateTime.now().toString()+": "+caller_name+" - "+msg);
                else
                    logger.log("*: "+caller_name+" - "+msg);
            }
            else{
                if(debug_time_stamps)
                    out.writeln("* "+LocalDateTime.now().toString()+": "+caller_name+" - "+msg);
                else
                    out.writeln("*: "+caller_name+" - "+msg);
            }
        }
    }
}
