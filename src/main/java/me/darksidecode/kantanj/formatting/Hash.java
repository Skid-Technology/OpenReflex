package me.darksidecode.kantanj.formatting;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import javax.xml.bind.DatatypeConverter;

public enum Hash {
   MD5("MD5"),
   SHA1("SHA1"),
   SHA256("SHA-256"),
   SHA512("SHA-512");

   private String name;

   private Hash(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public byte[] checksum(File input) {
      try (InputStream in = new FileInputStream(input)) {
         MessageDigest digest = MessageDigest.getInstance(this.name);
         byte[] block = new byte[4096];

         int length;
         while ((length = in.read(block)) > 0) {
            digest.update(block, 0, length);
         }

         return digest.digest();
      } catch (Exception ex) {
         throw new RuntimeException("failed to get checksum of " + input.getAbsolutePath(), ex);
      }
   }

   public String checksumString(File input) {
      return DatatypeConverter.printHexBinary(this.checksum(input));
   }

   public byte[] checksum(byte[] input) {
      try {
         MessageDigest digest = MessageDigest.getInstance(this.name);
         digest.update(input);
         return digest.digest();
      } catch (Exception e) {
         throw new RuntimeException("failed to get checksum of " + input.length + " bytes", e);
      }
   }

   public String checksumString(byte[] input) {
      return DatatypeConverter.printHexBinary(this.checksum(input));
   }

   public byte[] checksum(String input, Charset encoding) {
      return this.checksum(input.getBytes(encoding));
   }

   public String checksumString(String input, Charset encoding) {
      return DatatypeConverter.printHexBinary(this.checksum(input, encoding));
   }
}
