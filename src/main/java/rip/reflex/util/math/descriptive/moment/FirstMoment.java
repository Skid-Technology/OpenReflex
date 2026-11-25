package rip.reflex.util.math.descriptive.moment;

import java.io.Serializable;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.AbstractStorelessUnivariateStatistic;

public class FirstMoment extends AbstractStorelessUnivariateStatistic implements Serializable {
   protected long n;
   protected double m1;
   protected double dev;
   protected double nDev;

   public FirstMoment() {
      this.n = 0L;
      this.m1 = Double.NaN;
      this.dev = Double.NaN;
      this.nDev = Double.NaN;
   }

   public FirstMoment(FirstMoment original) throws NullPointerException {
      copy(original, this);
   }

   public static void copy(FirstMoment source, FirstMoment dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.m1 = source.m1;
      dest.dev = source.dev;
      dest.nDev = source.nDev;
   }

   @Override
   public void increment(double d) {
      if (this.n == 0L) {
         this.m1 = 0.0;
      }

      this.n++;
      double n0 = (double)this.n;
      this.dev = d - this.m1;
      this.nDev = this.dev / n0;
      this.m1 = this.m1 + this.nDev;
   }

   @Override
   public void clear() {
      this.m1 = Double.NaN;
      this.n = 0L;
      this.dev = Double.NaN;
      this.nDev = Double.NaN;
   }

   @Override
   public double getResult() {
      return this.m1;
   }

   @Override
   public long getN() {
      return this.n;
   }

   public FirstMoment copy() {
      FirstMoment result = new FirstMoment();
      copy(this, result);
      return result;
   }
}
