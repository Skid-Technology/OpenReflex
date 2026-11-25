package me.darksidecode.e2eep4j;

import me.darksidecode.e2eep4j.util.Validate;

public class E2EKey {
   private byte[] iv;
   private byte[] aes;
   private byte[] base;
   private Object[] coverage;

   public E2EKey(byte[] iv, byte[] aes, byte[] base) {
      this(iv, aes, base, null);
   }

   public E2EKey(byte[] iv, byte[] aes, byte[] base, Object[] coverage) {
      this.iv = Validate.ivLength(
         Validate.notEmpty(Validate.notNull(iv, "Init vector cannot be null"), "Init vector cannot be empty"), "Illegal input vector length"
      );
      this.aes = Validate.aesLength(Validate.notEmpty(Validate.notNull(aes, "AES cannot be null"), "AES cannot be empty"), "Illegal AES length");
      this.base = Validate.baseLength(Validate.notEmpty(Validate.notNull(base, "Base cannot be null"), "Base cannot be empty"), "Illegal base length");
      this.coverage = coverage == null ? null : Validate.cvgLength(Validate.notEmpty(coverage, "Coverage cannot be empty"), "Illegal coverage length");
   }

   public byte[] getIV() {
      byte[] r = (byte[])Validate.notEmpty(this.iv, "Input vector was already used").clone();
      this.iv = new byte[0];
      return r;
   }

   public byte[] getAES() {
      byte[] r = (byte[])Validate.notEmpty(this.aes, "AES was already used").clone();
      this.aes = new byte[0];
      return r;
   }

   public byte[] getBase() {
      byte[] r = (byte[])Validate.notEmpty(this.base, "Base was already used").clone();
      this.base = new byte[0];
      return r;
   }

   public Object[] getCoverage() {
      Object[] r = Validate.notEmpty(Validate.notNull(this.coverage, "Coverage is not set"), "Coverage was already used").clone();
      this.coverage = new Object[0];
      return r;
   }

   public boolean shallCoverage() {
      return this.coverage != null;
   }

   public boolean isUsed() {
      return this.iv.length == 0 || this.aes.length == 0 || this.base.length == 0 || this.coverage != null && this.coverage.length == 0;
   }

   @Override
   public boolean equals(Object obj) {
      throw new E2EException("E2E keys cannot be compared");
   }

   @Override
   public int hashCode() {
      throw new E2EException("E2E keys cannot be hashed");
   }

   @Override
   public String toString() {
      throw new E2EException("E2E keys cannot be converted to a string");
   }

   @Override
   public Object clone() {
      throw new E2EException("E2E keys cannot be cloned");
   }
}
