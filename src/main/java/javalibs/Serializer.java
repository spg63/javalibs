package javalibs;

import java.io.*;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @version 1.0
 *
 * Creation: 05/29/15
 * Edit: 05/29/15
 */
public class Serializer{
    private static volatile Serializer _instance;

    private Serializer(){}

    public static Serializer getInstance(){
        if(_instance == null){
            synchronized(Serializer.class){
                if(_instance == null){
                    _instance = new Serializer();
                }
            }
        }
        return _instance;
    }

    /**
     * Note: Users must cast the returned object to the type that is being read
     * @param file_path --> Relative path to the object
     * @return --> The deserialized object, needs casting
     */
    public Object load(String file_path){
        Object obj = null;
        try{
            FileInputStream fin = new FileInputStream(file_path);
            ObjectInputStream in = new ObjectInputStream(fin);
            obj = in.readObject();
            in.close();
            fin.close();

            if(obj != null && c.verbose){
                c.writeln(file_path + " successfully loaded");
            }
            else if(obj == null && c.verbose){
                c.writeln_err(file_path + " *NOT* loaded");
            }
        }
        catch(FileNotFoundException e){
            if(c.verbose){
                c.writeln_err("Couldn't find " + file_path);
            }
            c.writeln_err("** Load Unsuccessful **");
        }
        catch(Exception e){
            if(c.verbose){
                c.writeln_err("Exception occurred in Load for " + file_path);
            }
            c.writeln_err("** Load Unsuccessful **");
        }
        return obj;
    }

    /**
     *
     * @param obj --> The object getting serialized
     * @param file_path --> Relative path to store the object
     */
    public void save(Object obj, String file_path){
        try{
            // Create the appropriate dirs if they don't yet exist
            @SuppressWarnings("unused") File f = new File(file_path);
            FileOutputStream fout = new FileOutputStream(file_path);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(obj);
            out.close();
        }
        catch(Exception e){
            if(c.verbose){
                c.writeln_err("Couldn't save " + file_path);
            }
            c.writeln_err("** Save Unsuccessful **");
        }
    }
}
