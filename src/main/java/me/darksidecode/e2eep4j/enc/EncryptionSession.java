package me.darksidecode.e2eep4j.enc;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import me.darksidecode.e2eep4j.E2EException;
import me.darksidecode.e2eep4j.E2EKey;
import me.darksidecode.e2eep4j.util.Validate;
import org.apache.commons.codec.binary.Base64;

public class EncryptionSession implements CryptoSession {
   private byte[] input;
   private byte[] result;
   private E2EKey k;

   public EncryptionSession(byte[] input, E2EKey k) {
      this.input = Validate.notEmpty(Validate.notNull(input, "Input cannot be null"), "Input cannot be empty");
      this.k = Validate.notNull(k, "Key cannot be null");
   }

   @Override
   public CryptoSession run() {
      try {
         IvParameterSpec iv = new IvParameterSpec(this.k.getIV());
         SecretKeySpec skc = new SecretKeySpec(this.k.getAES(), "AES");
         Cipher c = Cipher.getInstance("AES/CBC/PKCS5PADDING");
         c.init(1, skc, iv);
         this.result = Base64.encodeBase64(c.doFinal(this.input));
         return this;
      } catch (Exception ex) {
         throw new E2EException("Failed to encrypt " + this.input.length + " bytes", ex);
      }
   }

   @Override
   public CryptoSession end() {
      this.input = this.result = new byte[0];
      this.k = null;
      return this;
   }

   @Override
   public byte[] result() {
      if (Validate.notNull(this.result, "The session was never ran").length != 0 && this.k != null) {
         byte[] r = (byte[])this.result.clone();
         this.end();
         return r;
      } else {
         throw new E2EException("Result was already used");
      }
   }
}
