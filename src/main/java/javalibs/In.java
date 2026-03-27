package javalibs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 6/6/15
 */
@SuppressWarnings("unused")
public class In{
    private static volatile In _instance;
    private Out out = Out.get();
    private TSL log = TSL.get();
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    private In(){ }

    public static In get(){
        if(_instance == null){
            synchronized(In.class){
                if(_instance == null){
                    _instance = new In();
                }
            }
        }
        return _instance;
    }

    public String readStr(){
        String line = "-1";
        try{
            line = reader.readLine();
            while(line == null || line.isEmpty()){
                out.writeln("Please enter valid input of at least 1 char");
                line = reader.readLine();
            }
        }
        catch(IOException e){
            log.exception(e);
            throw new UncheckedIOException(e);
        }
        return line;
    }

    public String readStr(String msg){
        out.write(msg);
        return readStr();
    }

    public int readInt(){
        String line = "-1";
        try{
            line = reader.readLine();
            while(line == null || line.isEmpty() || (!Validation.Int(line))){
                out.writeln("Enter a valid int");
                line = reader.readLine();
            }
        }
        catch(IOException e){
            log.exception(e);
            throw new UncheckedIOException(e);
        }
return Integer.parseInt(line);
    }

    public int readInt(String msg){
        out.write(msg);
        return readInt();
    }

    public double readDouble(){
        String line = "-1";
        try{
            line = reader.readLine();
            while(line == null || line.isEmpty() || (!Validation.Double(line))){
                out.writeln("Enter a valid double");
                line = reader.readLine();
            }
        }
        catch(IOException e){
            log.exception(e);
            throw new UncheckedIOException(e);
        }
return Double.parseDouble(line);
    }

    public double readDouble(String msg){
        out.write(msg);
        return readDouble();
    }

    public int intRange(int num, int start, int end){
        while(num < start || num > end){
            num = readInt("Enter a valid number between "+start+" and "+end+": ");
        }
        return num;
    }

    public int arrayLimits(int start, int end){
        int num = readInt();
        while(num < start || num > end){
            out.writeln("Number must be between "+start+" and "+end+ "(inclusive)");
            num = readInt("Enter again: ");
        }
        return num;
    }
}
