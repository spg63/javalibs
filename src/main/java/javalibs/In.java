package javalibs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/6/15
 */
@SuppressWarnings("unused")
public class In{
    private static volatile In _instance;
    private Out out = Out.get();
    private TSL log = TSL.get();


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
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = "-1";
        try{
            line = in.readLine();
            while(line == null || line.length() == 0){
                out.writeln("Please enter valid input of at least 1 char");
                line = in.readLine();
            }
        }
        catch(IOException e){
            log.err("readStr() -- IOException");
        }
        return line;
    }
    public String readStr(String msg){
        out.write(msg);
        return readStr();
    }

    public int readInt(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = "-1";
        try{
            line = in.readLine();
            while(line == null || line.length() == 0 || (!Validation.Int(line))){
                out.writeln("Enter a valid int");
                line = in.readLine();
            }
        }
        catch(IOException e){
            log.err("readInt() -- IOException");
        }
        //noinspection ConstantConditions
        return Integer.parseInt(line);
    }
    public int readInt(String msg){
        out.write(msg);
        return readInt();
    }

    public double readDouble(){
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = "-1";
        try{
            line = in.readLine();
            while(line == null || line.length() == 0 || (!Validation.Double(line))){
                out.writeln("Enter a valid int");
                line = in.readLine();
            }
        }
        catch(IOException e){
            log.err("readDouble() -- IOException");
        }
        //noinspection ConstantConditions
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
