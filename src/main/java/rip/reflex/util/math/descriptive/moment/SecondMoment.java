package rip.reflex.util.math.descriptive.moment;

import java.io.Serializable;
import rip.reflex.util.math.Maths;

public class SecondMoment extends FirstMoment implements Serializable {
   protected double m2;

   public SecondMoment() {
      this.m2 = Double.NaN;
   }

   public SecondMoment(SecondMoment original) throws NullPointerException {
      super(original);
      this.m2 = original.m2;
   }

   public static void copy(SecondMoment source, SecondMoment dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      FirstMoment.copy(source, dest);
      dest.m2 = source.m2;
   }

   @Override
   public void increment(double d) {
      if (this.n < 1L) {
         this.m1 = this.m2 = 0.0;
      }

      super.increment(d);
      this.m2 = this.m2 + ((double)this.n - 1.0) * this.dev * this.nDev;
   }

   @Override
   public void clear() {
      super.clear();
      this.m2 = Double.NaN;
   }

   @Override
   public double getResult() {
      return this.m2;
   }

   public SecondMoment copy() {
      SecondMoment result = new SecondMoment();
      copy(this, result);
      return result;
   }
}
