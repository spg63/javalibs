/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * Created: 06/01/15
 * Edited:  06/05/15
 *
 * Pretty much just a facade that wraps the other singleton classes and allows
 * access to their methods for ease of use
 */
@SuppressWarnings("unused")
public class UserUtils{
    private static volatile UserUtils _instance;

    private Out out = null;
    private In in = null;
    private Log log = null;
    private Serializer ser = null;
    private DebugUtils bug = null;
    private FileUtils file = null;
    private TSL tsl = null;

    /**
     * Some of the infrequently used classes being aggregated will be loaded
     * when a method requiring them is called
     *  - Serializer
     *  - DebugUtils
     *  - FileUtils
     *  - In
     *  - TSL
     */
    private UserUtils(){
        this.out = Out.get();
        this.log = Log.get();
        this.setShutDownHook();
    }

    public static UserUtils getInstance(){
        if(_instance == null){
            synchronized(UserUtils.class){
                if(_instance == null){
                    _instance = new UserUtils();
                }
            }
        }
        return _instance;
    }

    private void setShutDownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                if(tsl != null){
                    try{
                        tsl.shutDown();
                    }
                    catch(InterruptedException e){
                        log.logErr("TSL -- InterruptedException on shutDown in shutDownHook");
                    }
                }
                log.log("\n********** END OF RUN **********");
                log.logErr("\n********** END OF RUN **********");
                log.flushBuf();
                log.flushErrBuf();
                if(StateVariables_sg.debug_lib)
                    writeln("\nBuffer Flushed\n");
            }
        });
    }

    /*
        Console output functions --> Wrapper for Out.java
     */
    public void writeln(Object msg){
        this.out.writeln(msg);
    }
    public void writeln_err(Object msg){
        this.out.writeln_err(msg);
    }
    public void write(Object msg){
        this.out.write(msg);
    }
    public void write_err(Object msg){
        this.out.write_err(msg);
    }
    public void writef(String format, Object... args){
        this.out.writef(format, args);
    }

    /*
        Console input functions
     */
    public String readStr(){
        check_input_null();
        return this.in.readStr();
    }
    public String readStr(String msg){
        check_input_null();
        return this.in.readStr(msg);
    }
    public int readInt(){
        check_input_null();
        return this.in.readInt();
    }
    public int readInt(String msg){
        check_input_null();
        return this.in.readInt(msg);
    }
    public double readDouble(){
        check_input_null();
        return this.in.readDouble();
    }
    public double readDouble(String msg){
        check_input_null();
        return this.in.readDouble(msg);
    }
    public int intRange(int num, int start, int end){
        check_input_null();
        return this.in.intRange(num, start, end);
    }
    public int arrayLimits(int start, int end){
        check_input_null();
        return this.in.arrayLimits(start, end);
    }
    private void check_input_null(){
        if(this.in == null)
            this.in = In.get();
    }


    /*
        Logging functions --> Wrappers for Log.java
        NOTE: No access to flushBuf / flushErrBuf from the facade.
              Users have no reason to call these methods.
     */
    public void setLog(String file){
        this.log.setLog(file);
    }
    public void setErrLog(String file){
        this.log.setErrLog(file);
    }
    public void log(Object s){
        this.log.log(s);
    }
    public void logErr(Object s){
        this.log.logErr(s);
    }

    /*
        Serializer Wrappers
     */
    public Object load(String file_path){
        check_serial_null();
        return this.ser.load(file_path);
    }
    public void save(Object obj, String file_path){
        check_serial_null();
        this.ser.save(obj, file_path);
    }
    private void check_serial_null(){
        if(this.ser == null)
            this.ser = Serializer.getInstance();
    }

    /*
        DebugUtils Wrappers
     */
    public void debug(Object msg){
        check_debug_null();
        this.bug.debug(msg);
    }
    public void debug(Object msg, boolean print_caller_name){
        check_debug_null();
        this.bug.debug(msg, print_caller_name);
    }
    public void debug_to_file(){
        check_debug_null();
        this.bug.debug_to_file();
    }
    public void debug_time_stamps(){
        check_debug_null();
        this.bug.debug_time_stamps();
    }
    private void check_debug_null(){
        if(this.bug == null)
            this.bug = DebugUtils.getInstance();
    }

    /*
        FileUtils Wrappers
     */
    public String getWorkingDir(){
        check_file_null();
        return this.file.getWorkingDir();
    }
    public void checkAndCreateDir(String dirName){
        check_file_null();
        this.file.checkAndCreateDir(dirName);
    }
    private void check_file_null(){
        if(this.file == null)
            this.file = FileUtils.getInstance();
    }

    /*
        TSL Wrappers
     */
    public void tslog(String msg){
        check_tsl_null();
        this.tsl.log(msg);
    }
    public void tslShutDown(){
        check_tsl_null();
        try{
            this.tsl.shutDown();
        }
        catch(InterruptedException e){
            this.log.logErr("ThreadSafeLogger -- InterruptedException on shutDown");
        }
    }
    private void check_tsl_null(){
        if(this.tsl == null)
            this.tsl = TSL.getInstance();
    }

}
