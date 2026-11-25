package me.darksidecode.kantanj.crypto;

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Encryptor implements Encryptor {
   public static final int DEFAULT_ITERATIONS = 65536;
   private static final String KEYGEN_ALGO = "PBKDF2WithHmacSHA256";
   private static final String CRYPT_ALGO = "AES/CBC/PKCS5PADDING";
   private final char[] key;
   private final byte[] salt;
   private final int iterations;

   public AES256Encryptor(char[] key, byte[] salt) {
      this(key, salt, 65536);
   }

   public AES256Encryptor(char[] key, byte[] salt, int iterations) {
      if (key != null && key.length != 0) {
         if (salt == null || salt.length != 8) {
            throw new IllegalArgumentException("Invalid salt. Required length: 8");
         } else if (iterations < 2) {
            throw new IllegalArgumentException("Too few iterations. Expected at least 2");
         } else {
            this.key = (char[])key.clone();
            this.salt = salt;
            this.iterations = iterations;
         }
      } else {
         throw new IllegalArgumentException("key cannot be null or empty");
      }
   }

   private SecretKey getSecret() throws InvalidKeySpecException, NoSuchAlgorithmException {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      KeySpec spec = new PBEKeySpec(this.key, this.salt, this.iterations, 256);
      SecretKey secret = factory.generateSecret(spec);
      return new SecretKeySpec(secret.getEncoded(), "AES");
   }

   @Override
   public byte[] encrypt(byte[] input) {
      try {
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
         cipher.init(1, this.getSecret());
         AlgorithmParameters params = cipher.getParameters();
         byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
         byte[] cipherData = cipher.doFinal(input);
         return new AES256Encryptor.CipherObj(cipherData, iv).getBytes();
      } catch (Exception ex) {
         throw new RuntimeException("failed to encrypt " + input.length + " bytes with AES (256)", ex);
      }
   }

   @Override
   public byte[] decrypt(byte[] input) {
      try {
         AES256Encryptor.CipherObj cObj = AES256Encryptor.CipherObj.from(input);
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(2, this.getSecret(), new IvParameterSpec(cObj.iv));
         return cipher.doFinal(cObj.cipherData);
      } catch (Exception e) {
         throw new RuntimeException("failed to decrypt " + input.length + " bytes with AES (256)", e);
      }
   }

   private static class CipherObj {
      private final byte[] cipherData;
      private final byte[] iv;

      private CipherObj(byte[] cipherData, byte[] iv) {
         if (cipherData == null || cipherData.length == 0 || cipherData.length % 16 != 0) {
            throw new RuntimeException("invalid cipherdata");
         } else if (iv != null && iv.length == 16) {
            this.cipherData = cipherData;
            this.iv = iv;
         } else {
            throw new RuntimeException("invalid init vector");
         }
      }

      private byte[] getBytes() {
         byte[] b = new byte[16 + this.cipherData.length];
         System.arraycopy(this.iv, 0, b, 0, 16);
         System.arraycopy(this.cipherData, 0, b, 16, this.cipherData.length);
         return b;
      }

      private static AES256Encryptor.CipherObj from(byte[] b) {
         byte[] iv = new byte[16];
         byte[] cipherData = new byte[b.length - 16];
         System.arraycopy(b, 0, iv, 0, 16);
         System.arraycopy(b, 16, cipherData, 0, cipherData.length);
         return new AES256Encryptor.CipherObj(cipherData, iv);
      }
   }
}
