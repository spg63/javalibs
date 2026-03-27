package javalibs;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 5/11/15
 */
@SuppressWarnings("unused")
public class FileHasher{
    private static volatile FileHasher _instance;
    private final TSL log = TSL.get();

    private FileHasher(){}

    public static FileHasher get(){
        if(_instance == null){
            synchronized(FileHasher.class){
                if(_instance == null){
                    _instance = new FileHasher();
                }
            }
        }
        return _instance;
    }

    public String hash(String filePath){
        try(FileInputStream fis = new FileInputStream(filePath)){
            // MD5 is guaranteed by the JVM spec, NoSuchAlgorithmException will never throw
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];

            int numBytesRead;
            while((numBytesRead = fis.read(buffer)) > 0){
                md.update(buffer, 0, numBytesRead);
            }

            // %032x ensures leading zeros are preserved in the output string
            return String.format("%032x", new java.math.BigInteger(1, md.digest()));
        }
        catch(IOException e){
            log.exception(e);
            throw new java.io.UncheckedIOException(e);
        }
        catch(NoSuchAlgorithmException e){
            throw new RuntimeException("MD5 not available", e);
        }
    }

    public void printHash(String filePath){
        Out.get().writeln(filePath + ": " + hash(filePath));
    }

    public void logHash(String filePath){
        log.info(filePath + ": " + hash(filePath));
    }
}
