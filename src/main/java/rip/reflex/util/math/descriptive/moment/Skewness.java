package rip.reflex.util.math.descriptive.moment;

import java.io.Serializable;
import java.util.stream.IntStream;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.AbstractStorelessUnivariateStatistic;

public class Skewness extends AbstractStorelessUnivariateStatistic implements Serializable {
   protected ThirdMoment moment = null;
   protected boolean incMoment;

   public Skewness() {
      this.incMoment = true;
      this.moment = new ThirdMoment();
   }

   public Skewness(ThirdMoment m3) {
      this.incMoment = false;
      this.moment = m3;
   }

   public Skewness(Skewness original) throws NullPointerException {
      copy(original, this);
   }

   public static void copy(Skewness source, Skewness dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      dest.setData(source.getDataRef());
      dest.moment = new ThirdMoment(source.moment.copy());
      dest.incMoment = source.incMoment;
   }

   @Override
   public void increment(double d) {
      if (this.incMoment) {
         this.moment.increment(d);
      }
   }

   @Override
   public double getResult() {
      if (this.moment.n < 3L) {
         return Double.NaN;
      } else {
         double variance = this.moment.m2 / (double)(this.moment.n - 1L);
         if (variance < 1.0E-19) {
            return 0.0;
         } else {
            double n0 = (double)this.moment.getN();
            return n0 * this.moment.m3 / ((n0 - 1.0) * (n0 - 2.0) * Math.sqrt(variance) * variance);
         }
      }
   }

   @Override
   public long getN() {
      return this.moment.getN();
   }

   @Override
   public void clear() {
      if (this.incMoment) {
         this.moment.clear();
      }
   }

   @Override
   public double evaluate(double[] values, int begin, int length) throws IllegalArgumentException {
      double skew = Double.NaN;
      if (this.test(values, begin, length) && length > 2) {
         Mean mean = new Mean();
         double m = mean.evaluate(values, begin, length);
         double accum = 0.0;
         double accum2 = 0.0;

         for (int i = begin; i < begin + length; i++) {
            double d = values[i] - m;
            accum += d * d;
            accum2 += d;
         }

         double variance = (accum - accum2 * accum2 / (double)length) / (double)(length - 1);
         double accum3 = IntStream.range(begin, begin + length).mapToDouble(i -> values[i] - m).map(dx -> dx * dx * dx).sum();
         accum3 /= variance * Math.sqrt(variance);
         double n0 = (double)length;
         skew = n0 / ((n0 - 1.0) * (n0 - 2.0)) * accum3;
      }

      return skew;
   }

   public Skewness copy() {
      Skewness result = new Skewness();
      copy(this, result);
      return result;
   }
}
