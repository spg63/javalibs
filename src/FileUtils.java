import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Sean Grimes, spg63@cs.drexel.edu
 * @since 6/5/15
 */
public class FileUtils{
    private static volatile FileUtils _instance;

    private FileUtils(){
    }

    public static FileUtils getInstance(){
        if(_instance == null){
            synchronized(FileUtils.class){
                if(_instance == null){
                    _instance = new FileUtils();
                }
            }
        }
        return _instance;
    }

    public String getWorkingDir(){
        Path WD = Paths.get("");
        return WD.toAbsolutePath().toString();
    }
    public void checkAndCreateDir(String dirName){
        String path = getWorkingDir();
        File tmp = new File(path + File.separator + dirName + File.separator);
        // Hello lolcode
        if(!tmp.exists()){
            boolean cai_haz_filz = tmp.mkdirs();
            if(cai_haz_filz && StateVariables_sg.debug_lib){
                System.out.println("Wez maded sum new dir(z), cai haz sum milks now?");
            }
        }
    }



}
