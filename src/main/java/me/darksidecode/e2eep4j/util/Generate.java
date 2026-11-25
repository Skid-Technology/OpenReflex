package me.darksidecode.e2eep4j.util;

import java.security.SecureRandom;
import me.darksidecode.e2eep4j.E2EKey;

public class Generate {
   private static final SecureRandom r = new SecureRandom();

   public static E2EKey e2eKey() {
      return e2eKey(null);
   }

   public static E2EKey e2eKey(Object[] cvg) {
      return new E2EKey(iv(), aes(), base(), cvg);
   }

   public static byte[] iv() {
      return byteStream(16);
   }

   public static byte[] aes() {
      return byteStream(16);
   }

   public static byte[] base() {
      return byteStream(32);
   }

   public static byte[] byteStream(int len) {
      byte[] bytes = new byte[Validate.multipleOf(len, 4, "Length must be a multiple of 4")];
      r.nextBytes(bytes);
      return (byte[])bytes.clone();
   }
}
