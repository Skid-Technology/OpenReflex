package me.darksidecode.e2eep4j.api;

import java.io.File;
import java.nio.file.Files;
import me.darksidecode.e2eep4j.E2EException;
import me.darksidecode.e2eep4j.E2EKey;
import me.darksidecode.e2eep4j.enc.CryptoSession;
import me.darksidecode.e2eep4j.enc.DecryptionSession;
import me.darksidecode.e2eep4j.enc.EncryptionSession;
import me.darksidecode.e2eep4j.util.Convert;
import me.darksidecode.e2eep4j.util.Generate;

public class E2EEP4J {
   public static byte[] encryptBytes(byte[] input, E2EKey key) {
      return cryptBytes(new EncryptionSession(input, key));
   }

   public static byte[] decryptBytes(byte[] input, E2EKey key) {
      return cryptBytes(new DecryptionSession(input, key));
   }

   public static byte[] cryptBytes(CryptoSession session) {
      return session.run().result();
   }

   public static E2EKey generateKey() {
      return Generate.e2eKey();
   }

   public static E2EKey generateKey(Object[] coverage) {
      return Generate.e2eKey(coverage);
   }

   public static void safeKeyToFile(E2EKey key, File f) {
      try {
         Files.write(f.toPath(), Convert.e2eKey2Bytes(key));
      } catch (Exception ex) {
         throw new E2EException("Failed to save key to file", ex);
      }
   }

   public static E2EKey readKeyFromFile(File f) {
      try {
         return Convert.bytes2e2ekey(Files.readAllBytes(f.toPath()));
      } catch (Exception e) {
         throw new E2EException("Failed to read key from file", e);
      }
   }
}
