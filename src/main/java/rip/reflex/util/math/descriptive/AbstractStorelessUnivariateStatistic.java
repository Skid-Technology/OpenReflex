package rip.reflex.util.math.descriptive;

import rip.reflex.util.math.Maths;

public abstract class AbstractStorelessUnivariateStatistic extends AbstractUnivariateStatistic implements StorelessUnivariateStatistic {
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
      if (this.test(values, begin, length)) {
         this.clear();
         this.incrementAll(values, begin, length);
      }

      return this.getResult();
   }

   @Override
   public abstract StorelessUnivariateStatistic copy();

   @Override
   public abstract void clear();

   @Override
   public abstract double getResult();

   @Override
   public abstract void increment(double value);

   @Override
   public void incrementAll(double[] values) throws IllegalArgumentException {
      if (values == null) {
         throw new NullPointerException("");
      } else {
         this.incrementAll(values, 0, values.length);
      }
   }

   @Override
   public void incrementAll(double[] values, int begin, int length) throws IllegalArgumentException {
      if (this.test(values, begin, length)) {
         int k = begin + length;

         for (int i = begin; i < k; i++) {
            this.increment(values[i]);
         }
      }
   }

   @Override
   public boolean equals(Object object) {
      return object == this;
   }

   @Override
   public int hashCode() {
      return 31 * (31 + Maths.hash(this.getResult())) + Maths.hash((double)this.getN());
   }
}
