package rip.reflex.util.math.descriptive.moment;

import java.io.Serializable;
import java.util.stream.IntStream;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.AbstractStorelessUnivariateStatistic;
import rip.reflex.util.math.descriptive.WeightedEvaluation;
import rip.reflex.util.math.descriptive.summary.Sum;

public class Mean extends AbstractStorelessUnivariateStatistic implements Serializable, WeightedEvaluation {
   protected FirstMoment moment;
   protected boolean incMoment;

   public Mean() {
      this.incMoment = true;
      this.moment = new FirstMoment();
   }

   public Mean(FirstMoment m1) {
      this.moment = m1;
      this.incMoment = false;
   }

   public Mean(Mean original) throws NullPointerException {
      copy(original, this);
   }

   public static void copy(Mean source, Mean dest) throws NullPointerException {
      Maths.checkNotNull(source);
      Maths.checkNotNull(dest);
      dest.setData(source.getDataRef());
      dest.incMoment = source.incMoment;
      dest.moment = source.moment.copy();
   }

   @Override
   public void increment(double d) {
      if (this.incMoment) {
         this.moment.increment(d);
      }
   }

   @Override
   public void clear() {
      if (this.incMoment) {
         this.moment.clear();
      }
   }

   @Override
   public double getResult() {
      return this.moment.m1;
   }

   @Override
   public long getN() {
      return this.moment.getN();
   }

   @Override
   public double evaluate(double[] values, int begin, int length) throws IllegalArgumentException {
      if (this.test(values, begin, length)) {
         Sum sum = new Sum();
         double sampleSize = (double)length;
         double xbar = sum.evaluate(values, begin, length) / sampleSize;
         double correction = IntStream.range(begin, begin + length).mapToDouble(i -> values[i] - xbar).sum();
         return xbar + correction / sampleSize;
      } else {
         return Double.NaN;
      }
   }

   @Override
   public double evaluate(double[] values, double[] weights, int begin, int length) throws IllegalArgumentException {
      if (this.test(values, weights, begin, length)) {
         Sum sum = new Sum();
         double sumw = sum.evaluate(weights, begin, length);
         double xbarw = sum.evaluate(values, weights, begin, length) / sumw;
         double correction = IntStream.range(begin, begin + length).mapToDouble(i -> weights[i] * (values[i] - xbarw)).sum();
         return xbarw + correction / sumw;
      } else {
         return Double.NaN;
      }
   }

   @Override
   public double evaluate(double[] values, double[] weights) throws IllegalArgumentException {
      return this.evaluate(values, weights, 0, values.length);
   }

   public Mean copy() {
      Mean result = new Mean();
      copy(this, result);
      return result;
   }
}
