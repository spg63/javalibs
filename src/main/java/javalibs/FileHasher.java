package javalibs;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author Sean Grimes, sean@seanpgrimes.com
 * @since 5/11/15
 */
@SuppressWarnings("unused")
public class FileHasher{
    public String hash(String file_path){
        String hash_str = null;
        try{
            FileInputStream fis = new FileInputStream(file_path);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];

            int num_bytes_read;

            while((num_bytes_read = fis.read(buffer)) > 0){
                md.update(buffer, 0, num_bytes_read);
            }

            byte[] hash = md.digest();

            hash_str = new BigInteger(1, hash).toString(16);
        }
        catch(Exception e){
            TSL.get().exception(e);
        }

        return hash_str;
    }

    public void printHash(String file_path){
        System.out.println(file_path+": "+hash(file_path));
    }

    public void logHash(String file_path){
        TSL.get().info(file_path+": "+hash(file_path));
    }
}
