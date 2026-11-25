package rip.reflex.util.math;

import rip.reflex.util.misc.Validate;

public class SafeLong extends Number {
   private long value;

   public SafeLong() {
      this(0);
   }

   @Override
   public int intValue() {
      return this.getInt();
   }

   @Override
   public long longValue() {
      return this.value;
   }

   @Override
   public float floatValue() {
      return (float)this.value;
   }

   @Override
   public double doubleValue() {
      return (double)this.value;
   }

   public SafeLong(Long value) {
      this.value = Validate.notNull(value, "Long value cannot be null when instantiating a SafeLong");
   }

   public SafeLong(Integer value) {
      this.value = (long)Validate.notNull(value, "Integer value cannot be null when instantiating a SafeLong").intValue();
   }

   public SafeLong set(long value) {
      this.value = value;
      return this;
   }

   public long get() {
      return this.value;
   }

   public int getInt() {
      return this.getInt(this.value > 0L ? Integer.MAX_VALUE : (this.value < 0L ? Integer.MIN_VALUE : 0));
   }

   public int getInt(int alt) {
      try {
         return Math.toIntExact(this.value);
      } catch (ArithmeticException ignored) {
         return alt;
      }
   }

   public SafeLong add(SafeLong l) {
      return this.add(l.get());
   }

   public SafeLong add(long l) {
      if (this.value == Long.MAX_VALUE && Math.abs(l) > 0L) {
         throw new ArithmeticException("Positive long overflow: MAX_VALUE+" + Math.abs(l));
      } else {
         this.value = this.value + Math.abs(l);
         return this;
      }
   }

   public SafeLong addSafely(SafeLong l) {
      return this.addSafely(l.get());
   }

   public SafeLong addSafely(long l) {
      if (this.value == Long.MAX_VALUE && Math.abs(l) > 0L) {
         return this;
      } else {
         this.value = this.value + Math.abs(l);
         return this;
      }
   }

   public SafeLong subtract(SafeLong l) {
      return this.subtract(l.get());
   }

   public SafeLong subtract(long l) {
      this.value = this.value - Math.abs(l);
      return this;
   }

   public SafeLong subtractSafely(SafeLong l) {
      return this.subtractSafely(l.get());
   }

   public SafeLong subtractSafely(long l) {
      if (this.value == Long.MIN_VALUE && Math.abs(l) > 0L) {
         throw new ArithmeticException("Negative long overflow: MIN_VALUE-" + Math.abs(l));
      } else {
         this.value = this.value - Math.abs(l);
         return this;
      }
   }

   public SafeLong multiply(SafeLong l) {
      return this.multiply(l.get());
   }

   public SafeLong multiply(long l) {
      this.value *= l;
      return this;
   }

   public SafeLong divide(SafeLong l) {
      return this.divide(l.get());
   }

   public SafeLong divide(long l) {
      if (this.value % Validate.notZero(l, "Divide by zero") != 0L) {
         throw new ArithmeticException("The value (" + this.value + ") is not a multiple of " + l);
      } else {
         this.value /= l;
         return this;
      }
   }

   public SafeLong divideAndFloor(SafeLong l) {
      return this.divideAndFloor(l.get());
   }

   public SafeLong divideAndFloor(long l) {
      this.value = Maths.divAndFloor(this.value, Validate.notZero(l, "Divide by zero"));
      return this;
   }

   public SafeLong copy() {
      return new SafeLong(this.value);
   }

   @Override
   public String toString() {
      return Long.toString(this.value);
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.value);
   }
}
