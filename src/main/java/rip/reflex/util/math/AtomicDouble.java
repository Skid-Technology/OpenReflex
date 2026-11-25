package rip.reflex.util.math;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class AtomicDouble extends Number implements Serializable {
   private static final long serialVersionUID = 4468035942200377527L;
   private final AtomicLong bits;

   public AtomicDouble() {
      this(0.0);
   }

   public AtomicDouble(double initialValue) {
      this.bits = new AtomicLong(Double.doubleToLongBits(initialValue));
   }

   public final boolean compareAndSet(double expect, double update) {
      return this.bits.compareAndSet(Double.doubleToLongBits(expect), Double.doubleToLongBits(update));
   }

   public final void set(double newValue) {
      this.bits.set(Double.doubleToLongBits(newValue));
   }

   public final double get() {
      return Double.longBitsToDouble(this.bits.get());
   }

   public final double getAndSet(double newValue) {
      return Double.longBitsToDouble(this.bits.getAndSet(Double.doubleToLongBits(newValue)));
   }

   public final boolean weakCompareAndSet(double expect, double update) {
      return this.bits.weakCompareAndSet(Double.doubleToLongBits(expect), Double.doubleToLongBits(update));
   }

   @Override
   public double doubleValue() {
      return this.get();
   }

   @Override
   public int intValue() {
      return (int)this.get();
   }

   @Override
   public long longValue() {
      return (long)this.get();
   }

   @Override
   public float floatValue() {
      return (float)this.get();
   }
}
