package rip.reflex.util.math.descriptive.moment;

import java.io.Serializable;
import java.util.stream.IntStream;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.AbstractStorelessUnivariateStatistic;

public class Kurtosis extends AbstractStorelessUnivariateStatistic implements Serializable {
   protected FourthMoment moment;
   protected boolean incMoment;

   public Kurtosis() {
      this.incMoment = true;
      this.moment = new FourthMoment();
   }

   public Kurtosis(FourthMoment m4) {
      this.incMoment = false;
      this.moment = m4;
   }

   public Kurtosis(Kurtosis original) throws NullPointerException {
      copy(original, this);
   }

   public static void copy(Kurtosis source, Kurtosis dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      dest.setData(source.getDataRef());
      dest.moment = source.moment.copy();
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
      double kurtosis = Double.NaN;
      if (this.moment.getN() > 3L) {
         double variance = this.moment.m2 / (double)(this.moment.n - 1L);
         if (this.moment.n > 3L && !(variance < 1.0E-19)) {
            double n = (double)this.moment.n;
            kurtosis = (n * (n + 1.0) * this.moment.getResult() - 3.0 * this.moment.m2 * this.moment.m2 * (n - 1.0))
               / ((n - 1.0) * (n - 2.0) * (n - 3.0) * variance * variance);
         } else {
            kurtosis = 0.0;
         }
      }

      return kurtosis;
   }

   @Override
   public void clear() {
      if (this.incMoment) {
         this.moment.clear();
      }
   }

   @Override
   public long getN() {
      return this.moment.getN();
   }

   @Override
   public double evaluate(double[] values, int begin, int length) throws IllegalArgumentException {
      double kurt = Double.NaN;
      if (this.test(values, begin, length) && length > 3) {
         Variance variance = new Variance();
         variance.incrementAll(values, begin, length);
         double mean = variance.moment.m1;
         double stdDev = Math.sqrt(variance.getResult());
         double accum3 = IntStream.range(begin, begin + length).mapToDouble(i -> Math.pow(values[i] - mean, 4.0)).sum();
         accum3 /= Math.pow(stdDev, 4.0);
         double n0 = (double)length;
         double coefficientOne = n0 * (n0 + 1.0) / ((n0 - 1.0) * (n0 - 2.0) * (n0 - 3.0));
         double termTwo = 3.0 * Math.pow(n0 - 1.0, 2.0) / ((n0 - 2.0) * (n0 - 3.0));
         kurt = coefficientOne * accum3 - termTwo;
      }

      return kurt;
   }

   public Kurtosis copy() {
      Kurtosis result = new Kurtosis();
      copy(this, result);
      return result;
   }
}
