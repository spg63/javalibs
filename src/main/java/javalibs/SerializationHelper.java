package javalibs;

import java.io.*;

public class SerializationHelper {
    /**
     * This is a generic deserialization method, it only needs to know the type of
     * object it is deserializing. It will work for *anything* that can be deserialized.
     * @param type The object type, passed as, for example, Object.class
     * @param path The path to the file to be deserialized
     * @param <T> A necessary generic qualifier, it is implicitly passed
     * @return The deserialized object
     * NOTE: This would be called like so:
     * SerializationHelper.deserialize(Object.class, path) where "Object.class" is
     * whatever object type you're deserializing, e.g. "Car.class" to deserialize the Car
     */
    public static <T> T deserialize(Class<T> type, String path){
        // Make sure the path exists and is a file
        File tst = new File(path);
        TSL.get().require(tst.exists() && tst.isFile());

        // Create a generic Object that will hold the deserialized object
        T deserializedObject = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);

            // Read it with the proper cast
            deserializedObject = type.cast(ois.readObject());
        }
        // IOException or ClassNotFoundException, either way handled the same
        catch(IOException | ClassNotFoundException e){
            TSL.get().exception(e);
            TSL.get().autoLog("Deserialization failure");
        }
        finally{
            if(fis != null){
                try {
                    fis.close();
                }
                catch(IOException e){
                    TSL.get().exception(e);
                    TSL.get().autoLog("FileInputStream failed to close");
                }
            }
            if(ois != null){
                try{
                    ois.close();
                }
                catch(IOException e){
                    TSL.get().exception(e);
                    TSL.get().autoLog("ObjectInputStream failed to close");
                }
            }
        }
        return deserializedObject;
    }

    /**
     * Serialize a class to disk using generics and passed class attributes.
     * @param type The object type, passed as, for example, Object.class
     * @param obj The object to be saved, will be up-cast to the proper type before saving
     * @param dirPath The path to the directory to store the object
     * @param fileName The name of the file to serialize the object to
     * @param <T> A necessary generic qualifier, implicitly passed
     * @return The full path to the serialized object, stored on disk
     */
    public static <T> String serialize(Class<T> type, Object obj, String dirPath,
                                       String fileName){
        // Make sure the directory exists
        FileUtils.get().checkAndCreateDir(dirPath);
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        String fullPath = dirPath + fileName;
        try{
            fos = new FileOutputStream(fullPath);
            oos = new ObjectOutputStream(fos);

            oos.writeObject(type.cast(obj));

            oos.close();
            fos.close();
        }
        catch(IOException e){
            TSL.get().exception(e);
        }
        finally{
            if(oos != null) {
                try {
                    oos.close();
                }
                catch(IOException ex){
                    TSL.get().exception(ex);
                    TSL.get().autoLog("ObjectOutputStream failed to close");
                }
            }
            if(fos != null) {
                try{
                    fos.close();
                }
                catch(IOException ex){
                    TSL.get().exception(ex);
                    TSL.get().autoLog("FileOutputStream failed to close");
                }
            }
        }
        return fullPath;
    }
}
