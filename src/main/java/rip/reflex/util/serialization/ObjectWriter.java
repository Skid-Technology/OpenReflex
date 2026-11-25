package rip.reflex.util.serialization;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class ObjectWriter {
   public static <T extends Serializable> byte[] write(Class<T> c, T obj) {
      try {
         ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
         ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
         objOut.writeObject(c.cast(obj));
         byte[] data = byteOut.toByteArray();
         byteOut.close();
         objOut.close();
         return data;
      } catch (IOException ex) {
         throw new RuntimeException(ex);
      }
   }

   public static <T extends Serializable> void write(Class<T> c, T obj, OutputStream out) {
      try {
         out.write(write(c, obj));
         out.close();
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public static <T extends Serializable> void write(Class<T> c, T obj, File file) {
      try {
         write(c, obj, new FileOutputStream(file));
      } catch (IOException err) {
         throw new RuntimeException(err);
      }
   }
}
