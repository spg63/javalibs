/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/1/15
 */
public class Out implements java.io.Serializable{
    private static final long serialVersionUID = 80085L;
    private static volatile Out _instance;

    private Out(){
    }

    public static Out get(){
        if(_instance == null){
            synchronized(Out.class){
                if(_instance == null){
                    _instance = new Out();
                }
            }
        }
        return _instance;
    }

    public void writeln(Object msg){
        System.out.println(msg);
    }
    public void write(Object msg){
        System.out.print(msg);
    }
    public void writef(String format, Object... args){
        System.out.print(String.format(format, args));
    }
    public void writeln_err(Object msg){
        System.err.println(msg);
    }
    public void write_err(Object msg){
        System.err.print(msg);
    }







}
