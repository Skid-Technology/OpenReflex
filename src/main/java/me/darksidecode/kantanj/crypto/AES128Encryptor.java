package me.darksidecode.kantanj.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES128Encryptor implements Encryptor {
   private static final String ALGO = "AES/CBC/PKCS5PADDING";
   private final byte[] key;
   private final byte[] iv;

   public AES128Encryptor(byte[] key, byte[] iv) {
      if (key == null || key.length != 16) {
         throw new IllegalArgumentException("Invalid key. Required length: 16");
      } else if (iv != null && iv.length == 16) {
         this.key = key;
         this.iv = iv;
      } else {
         throw new IllegalArgumentException("Invalid init vector. Required length: 16");
      }
   }

   @Override
   public byte[] encrypt(byte[] input) {
      try {
         IvParameterSpec iv = new IvParameterSpec(this.iv);
         SecretKeySpec skc = new SecretKeySpec(this.key, "AES");
         Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
         c.init(1, skc, iv);
         return c.doFinal(input);
      } catch (Exception ex) {
         throw new RuntimeException("failed to encrypt " + input.length + " bytes with AES (128)", ex);
      }
   }

   @Override
   public byte[] decrypt(byte[] input) {
      try {
         IvParameterSpec iv = new IvParameterSpec(this.iv);
         SecretKeySpec skeySpec = new SecretKeySpec(this.key, "AES");
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
         cipher.init(2, skeySpec, iv);
         return cipher.doFinal(input);
      } catch (Exception e) {
         throw new RuntimeException("failed to decrypt " + input.length + " bytes with AES (128)", e);
      }
   }
}
