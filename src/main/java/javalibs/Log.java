package javalibs;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/1/15
 */
public class Log{
    private static volatile Log _instance;
    private String logFile;
    private String errLogFile;
    private StringBuilder buf;
    private StringBuilder errBuf;

    private Log(){
        this.buf = new StringBuilder(8192);
        this.errBuf = new StringBuilder(1024);
        this.logFile = "log.txt";
        this.errLogFile = "errLog.txt";
    }

    public static Log get(){
        if(_instance == null){
            synchronized(Log.class){
                if(_instance == null){
                    _instance = new Log();
                }
            }
        }
        return _instance;
    }

    public void setLog(String file){
        if(file.length() > 0){
            this.logFile = file;
        }
        else{
            System.err.println("*** javalibs.Log file name must be greater than 0 chars ***");
            System.err.println("*** Logs will be stored in \"log.txt\" ***");
        }
    }
    public void setErrLog(String file){
        if(file.length() > 0){
            this.errLogFile = file;
        }
        else{
            System.err.println("*** errLog file name must be greater that 0 chars ***");
            System.err.println("*** errLogs will be stored in \"errLog.txt\" ***");
        }
    }
    public void log(Object s){
        buf.append("\n");
        if(StateVariables_sg.log_time_stamps){
            buf.append(LocalDateTime.now().toString());
            buf.append(": ");
            buf.append(s);
        }
        else{
            buf.append(s);
        }
        if(buf.length() >= 8192){
            flushBuf();
        }
    }
    public void logErr(Object s){
        errBuf.append("\n");
        if(StateVariables_sg.err_time_stamps){
            errBuf.append(LocalDateTime.now().toString());
            errBuf.append(": ");
            errBuf.append(s);
        }
        else{
            errBuf.append(s);
        }
        if(errBuf.length() >= 1024){
            flushErrBuf();
        }
    }
    public void flushBuf(){
        writeLogToFile(buf);
        buf = new StringBuilder(8192);
    }
    public void flushErrBuf(){
        writeErrToFile(errBuf);
        errBuf = new StringBuilder(1024);
    }

    private void writeLogToFile(Object msg){
        if(StateVariables_sg.debug_lib)
            System.out.println("We're writing log to file");
        try(PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(logFile, true)))){
            out.println(msg);
        }
        catch(IOException e){
            System.out.println("*** writeLogToFile failed ***");
        }
    }
    private void writeErrToFile(Object msg){
        if(StateVariables_sg.debug_lib)
            System.out.println("We're writing errLog to file");
        try(PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new FileWriter(errLogFile, true)))){
            out.println(msg);
        }
        catch(IOException e){
            System.out.println("*** writeErrToFile failed ***");
        }
    }
}
