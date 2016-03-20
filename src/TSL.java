import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @author Andrew W.E. McDonald
 * @since 6/6/15
 */
@SuppressWarnings("unused")
public class TSL extends Thread{
    private static volatile TSL _instance;

    private String SHUTDOWN_REQ = null;
    private volatile boolean shuttingDown, loggerTerminated;
    private BlockingQueue<String> itemsToLog = new ArrayBlockingQueue<>(10000);
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
        this.futils = FileUtils.getInstance();
        LocalDateTime dt = LocalDateTime.now();
        this.dt = dt.toString().replace("T", "_").replace(":","_");

        // Start the logger
        start();
    }

    public static TSL getInstance(){
        if(_instance == null){
            synchronized(TSL.class){
                if(_instance == null){
                    _instance = new TSL();
                }
            }
        }
        return _instance;
    }

    @Override
    public void run(){
        try{
            String item;
            while(!(item = itemsToLog.take()).equals(SHUTDOWN_REQ)){
                futils.checkAndCreateDir("logs");
                try(PrintWriter out = new PrintWriter(
                                        new BufferedWriter(
                                          new FileWriter("logs/tslog_"+dt+".txt", true)))){
                    out.println(item);
                }
                catch(IOException e){
                    out.writeln_err("*** ThreadSafeLogger failed ***");
                }
            }
        }
        catch(InterruptedException e){
            out.writeln_err("ThreadSafeLogger -- I was interrupted");
        }
        finally{
            loggerTerminated = true;
        }
    }

    public void log(String str){
        if(shuttingDown || loggerTerminated)
            return;
        try{
            ldt = LocalDateTime.now();
            int hour = ldt.getHour();
            int min = ldt.getMinute();
            int sec = ldt.getSecond();
            int milli = (ldt.getNano())/(1000000);

            itemsToLog.put("("+hour+":"+min+":"+sec+"."+milli+") > "+str);
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
            throw new RuntimeException("ThreadSafeLogger.log() -- Unexpected interruption");
        }
    }

    public void shutDown() throws InterruptedException{
        shuttingDown = true;
        itemsToLog.put(SHUTDOWN_REQ);
    }
















}
