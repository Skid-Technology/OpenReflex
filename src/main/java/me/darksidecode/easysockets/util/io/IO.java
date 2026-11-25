package me.darksidecode.easysockets.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class IO {
   public static byte[] ser(Object obj) {
      if (obj == null) {
         return new byte[0];
      } else {
         try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            byte[] bytes = baos.toByteArray();
            baos.close();
            return bytes;
         } catch (Exception ex) {
            throw new RuntimeException("failed to serialize " + obj.getClass().getName(), ex);
         }
      }
   }

   public static Object deser(byte[] bytes) {
      if (bytes != null && bytes.length != 0) {
         try {
            ByteArrayInputStream bain = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bain);
            Object obj = ois.readObject();
            bain.close();
            ois.close();
            return obj;
         } catch (Exception e) {
            throw new RuntimeException("failed to deserialize " + bytes.length + " bytes", e);
         }
      } else {
         return null;
      }
   }
}
