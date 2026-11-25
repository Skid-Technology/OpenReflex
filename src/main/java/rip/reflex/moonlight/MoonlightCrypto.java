package rip.reflex.moonlight;

import java.nio.charset.StandardCharsets;
import me.darksidecode.kantanj.crypto.AES256Encryptor;
import me.darksidecode.kantanj.crypto.Encryptor;
import org.apache.commons.codec.binary.Base64;

public final class MoonlightCrypto {
   private static Encryptor encryptor;

   private MoonlightCrypto() {
   }

   public static void setEncryptor(String keyString, String base64Salt) {
      char[] key = keyString.toCharArray();
      byte[] salt = Base64.decodeBase64(base64Salt);
      encryptor = new AES256Encryptor(key, salt);
   }

   public static String encryptString(String s) {
      if (encryptor == null) {
         throw new IllegalStateException("encryptor is not set");
      } else {
         byte[] utf8Bytes = s.getBytes(StandardCharsets.UTF_8);
         byte[] encrypted = encryptor.encrypt(utf8Bytes);
         return Base64.encodeBase64String(encrypted);
      }
   }

   public static String decryptString(String s) {
      if (encryptor == null) {
         throw new IllegalStateException("encryptor is not set");
      } else {
         byte[] encrypted = Base64.decodeBase64(s);
         byte[] utf8Bytes = encryptor.decrypt(encrypted);
         return new String(utf8Bytes, StandardCharsets.UTF_8);
      }
   }
}
