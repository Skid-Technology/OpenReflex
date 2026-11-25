package rip.reflex.util.serialization;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class ObjectReader {
   public static <T extends Serializable> T read(Class<T> c, byte[] data) {
      return read(c, new ByteArrayInputStream(data));
   }

   public static <T extends Serializable> T read(Class<T> c, File file) {
      try {
         return read(c, new FileInputStream(file));
      } catch (FileNotFoundException ignored) {
         throw new RuntimeException();
      }
   }

   public static <T extends Serializable> T read(Class<T> c, InputStream in) {
      try {
         ObjectInputStream objIn = new ObjectInputStream(in);
         T obj = (T)c.cast(objIn.read());
         in.close();
         objIn.close();
         return obj;
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }
}
