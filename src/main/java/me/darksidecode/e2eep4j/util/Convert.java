package me.darksidecode.e2eep4j.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import me.darksidecode.e2eep4j.E2EException;
import me.darksidecode.e2eep4j.E2EKey;

public class Convert {
   public static byte[] e2eKey2Bytes(E2EKey k) {
      try {
         byte[] iv = k.getIV();
         byte[] aes = k.getAES();
         byte[] base = k.getBase();
         Object[] cvg = k.shallCoverage() ? k.getCoverage() : new Object[0];
         Object[] r = new Object[iv.length + aes.length + base.length + cvg.length];
         int idx = 0;

         for (byte n : iv) {
            r[idx++] = n;
         }

         for (byte n : aes) {
            r[idx++] = n;
         }

         for (byte n : base) {
            r[idx++] = n;
         }

         for (Object n : cvg) {
            r[idx++] = n;
         }

         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(r);
         oos.flush();
         oos.close();
         baos.close();
         return baos.toByteArray();
      } catch (Exception ex) {
         throw new E2EException("E2EKey conversion failed", ex);
      }
   }

   public static E2EKey bytes2e2ekey(byte[] bytes) {
      try {
         ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bis);
         Object[] r = (Object[])ois.readObject();
         int idx = 0;
         byte[] iv = new byte[16];
         byte[] aes = new byte[16];
         byte[] base = new byte[32];
         Object[] cvg = new Object[r.length - 64];

         for (int i = 0; i < iv.length; i++) {
            iv[i] = (Byte)r[idx++];
         }

         for (int i = 0; i < aes.length; i++) {
            aes[i] = (Byte)r[idx++];
         }

         for (int i = 0; i < base.length; i++) {
            base[i] = (Byte)r[idx++];
         }

         for (int i = 0; i < cvg.length; i++) {
            cvg[i] = r[idx++];
         }

         return new E2EKey(iv, aes, base, cvg.length == 0 ? null : cvg);
      } catch (Exception e) {
         throw new E2EException("Bytes conversion failed", e);
      }
   }
}
