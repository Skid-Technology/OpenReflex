package rip.reflex.util.crypto;

import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
   public static final byte[] key = new byte[]{37, -45, -95, 73, 120, -86, 86, -97, -76, 89, -27, -61, 41, 10, -41, 55};

   public static byte[] encrypt(byte[] data) throws Exception {
      return getCipher(1).doFinal(data);
   }

   public static byte[] decrypt(byte[] data) throws Exception {
      return getCipher(2).doFinal(data);
   }

   private static Key generateKey() {
      return new SecretKeySpec(key, "AES");
   }

   public static Cipher getCipher(int mode) {
      try {
         Cipher c = Cipher.getInstance("AES");
         c.init(mode, generateKey());
         return c;
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }
   }
}
