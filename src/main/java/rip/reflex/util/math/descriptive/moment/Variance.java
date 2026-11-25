package rip.reflex.util.math.descriptive.moment;

import java.io.Serializable;
import java.util.stream.IntStream;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.AbstractStorelessUnivariateStatistic;
import rip.reflex.util.math.descriptive.WeightedEvaluation;

public class Variance extends AbstractStorelessUnivariateStatistic implements Serializable, WeightedEvaluation {
   protected SecondMoment moment = null;
   protected boolean incMoment = true;
   private boolean isBiasCorrected = true;

   public Variance() {
      this.moment = new SecondMoment();
   }

   public Variance(SecondMoment m2) {
      this.incMoment = false;
      this.moment = m2;
   }

   public Variance(boolean isBiasCorrected) {
      this.moment = new SecondMoment();
      this.isBiasCorrected = isBiasCorrected;
   }

   public Variance(boolean isBiasCorrected, SecondMoment m2) {
      this.incMoment = false;
      this.moment = m2;
      this.isBiasCorrected = isBiasCorrected;
   }

   public Variance(Variance original) throws NullPointerException {
      copy(original, this);
   }

   public static void copy(Variance source, Variance dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      dest.setData(source.getDataRef());
      dest.moment = source.moment.copy();
      dest.isBiasCorrected = source.isBiasCorrected;
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
      if (this.moment.n == 0L) {
         return Double.NaN;
      } else if (this.moment.n == 1L) {
         return 0.0;
      } else {
         return this.isBiasCorrected ? this.moment.m2 / ((double)this.moment.n - 1.0) : this.moment.m2 / (double)this.moment.n;
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
   public double evaluate(double[] values) throws IllegalArgumentException {
      if (values == null) {
         throw new NullPointerException("");
      } else {
         return this.evaluate(values, 0, values.length);
      }
   }

   @Override
   public double evaluate(double[] values, int begin, int length) throws IllegalArgumentException {
      double var = Double.NaN;
      if (this.test(values, begin, length)) {
         this.clear();
         if (length == 1) {
            var = 0.0;
         } else if (length > 1) {
            Mean mean = new Mean();
            double m = mean.evaluate(values, begin, length);
            var = this.evaluate(values, m, begin, length);
         }
      }

      return var;
   }

   @Override
   public double evaluate(double[] values, double[] weights, int begin, int length) throws IllegalArgumentException {
      double var = Double.NaN;
      if (this.test(values, weights, begin, length)) {
         this.clear();
         if (length == 1) {
            var = 0.0;
         } else if (length > 1) {
            Mean mean = new Mean();
            double m = mean.evaluate(values, weights, begin, length);
            var = this.evaluate(values, weights, m, begin, length);
         }
      }

      return var;
   }

   @Override
   public double evaluate(double[] values, double[] weights) throws IllegalArgumentException {
      return this.evaluate(values, weights, 0, values.length);
   }

   public double evaluate(double[] values, double mean, int begin, int length) throws IllegalArgumentException {
      double var = Double.NaN;
      if (this.test(values, begin, length)) {
         if (length == 1) {
            var = 0.0;
         } else if (length > 1) {
            double accum = 0.0;
            double accum2 = 0.0;

            for (int i = begin; i < begin + length; i++) {
               double dev = values[i] - mean;
               accum += dev * dev;
               accum2 += dev;
            }

            double len = (double)length;
            if (this.isBiasCorrected) {
               var = (accum - accum2 * accum2 / len) / (len - 1.0);
            } else {
               var = (accum - accum2 * accum2 / len) / len;
            }
         }
      }

      return var;
   }

   public double evaluate(double[] values, double mean) throws IllegalArgumentException {
      return this.evaluate(values, mean, 0, values.length);
   }

   public double evaluate(double[] values, double[] weights, double mean, int begin, int length) throws IllegalArgumentException {
      double var = Double.NaN;
      if (this.test(values, weights, begin, length)) {
         if (length == 1) {
            var = 0.0;
         } else if (length > 1) {
            double accum = 0.0;
            double accum2 = 0.0;

            for (int i = begin; i < begin + length; i++) {
               double dev = values[i] - mean;
               accum += weights[i] * dev * dev;
               accum2 += weights[i] * dev;
            }

            double sumWts = IntStream.range(begin, begin + length).mapToDouble(i -> weights[i]).sum();
            if (this.isBiasCorrected) {
               var = (accum - accum2 * accum2 / sumWts) / (sumWts - 1.0);
            } else {
               var = (accum - accum2 * accum2 / sumWts) / sumWts;
            }
         }
      }

      return var;
   }

   public double evaluate(double[] values, double[] weights, double mean) throws IllegalArgumentException {
      return this.evaluate(values, weights, mean, 0, values.length);
   }

   public boolean isBiasCorrected() {
      return this.isBiasCorrected;
   }

   public void setBiasCorrected(boolean biasCorrected) {
      this.isBiasCorrected = biasCorrected;
   }

   public Variance copy() {
      Variance result = new Variance();
      copy(this, result);
      return result;
   }
}
