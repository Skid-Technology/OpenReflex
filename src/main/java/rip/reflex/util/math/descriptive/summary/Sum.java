package rip.reflex.util.math.descriptive.summary;

import java.io.Serializable;
import java.util.stream.IntStream;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.AbstractStorelessUnivariateStatistic;

public class Sum extends AbstractStorelessUnivariateStatistic implements Serializable {
   private long n;
   private double value;

   public Sum() {
      this.n = 0L;
      this.value = 0.0;
   }

   public Sum(Sum original) throws NullPointerException {
      copy(original, this);
   }

   public static void copy(Sum source, Sum dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      dest.setData(source.getDataRef());
      dest.n = source.n;
      dest.value = source.value;
   }

   @Override
   public void increment(double d) {
      this.value += d;
      this.n++;
   }

   @Override
   public double getResult() {
      return this.value;
   }

   @Override
   public long getN() {
      return this.n;
   }

   @Override
   public void clear() {
      this.value = 0.0;
      this.n = 0L;
   }

   @Override
   public double evaluate(double[] values, int begin, int length) throws IllegalArgumentException {
      double sum = Double.NaN;
      if (this.test(values, begin, length, true)) {
         sum = 0.0;
         sum += IntStream.range(begin, begin + length).mapToDouble(i -> values[i]).sum();
      }

      return sum;
   }

   public double evaluate(double[] values, double[] weights, int begin, int length) throws IllegalArgumentException {
      double sum = Double.NaN;
      if (this.test(values, weights, begin, length, true)) {
         sum = 0.0;
         sum += IntStream.range(begin, begin + length).mapToDouble(i -> values[i] * weights[i]).sum();
      }

      return sum;
   }

   public double evaluate(double[] values, double[] weights) throws IllegalArgumentException {
      return this.evaluate(values, weights, 0, values.length);
   }

   public Sum copy() {
      Sum result = new Sum();
      copy(this, result);
      return result;
   }
}
