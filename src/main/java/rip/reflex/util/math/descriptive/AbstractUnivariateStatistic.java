package rip.reflex.util.math.descriptive;

public abstract class AbstractUnivariateStatistic implements UnivariateStatistic {
   private double[] storedData;

   public void setData(double[] values) {
      this.storedData = values == null ? null : (double[])values.clone();
   }

   public double[] getData() {
      return this.storedData == null ? null : (double[])this.storedData.clone();
   }

   protected double[] getDataRef() {
      return this.storedData;
   }

   public void setData(double[] values, int begin, int length) throws IllegalArgumentException {
      if (values == null) {
         throw new NullPointerException("");
      } else if (begin >= 0 && length >= 0) {
         if (begin + length > values.length) {
            throw new IllegalArgumentException("number too large");
         } else {
            this.storedData = new double[length];
            System.arraycopy(values, begin, this.storedData, 0, length);
         }
      } else {
         throw new IllegalArgumentException("argument most be positive");
      }
   }

   public double evaluate() throws IllegalArgumentException {
      return this.evaluate(this.storedData);
   }

   @Override
   public double evaluate(double[] values) throws IllegalArgumentException {
      this.test(values, 0, 0);
      return this.evaluate(values, 0, values.length);
   }

   @Override
   public abstract double evaluate(double[] values, int begin, int length) throws IllegalArgumentException;

   @Override
   public abstract UnivariateStatistic copy();

   protected boolean test(double[] values, int begin, int length) throws IllegalArgumentException {
      return values != null && begin >= 0 && length >= 0 && begin <= length && values.length != 0;
   }

   protected boolean test(double[] values, int begin, int length, boolean allowEmpty) throws IllegalArgumentException {
      return values != null && begin >= 0 && length >= 0 && begin <= length && (!allowEmpty || values.length != 0);
   }

   protected boolean test(double[] values, double[] weights, int begin, int length) throws IllegalArgumentException {
      return values != null && weights != null && begin >= 0 && length >= 0 && begin <= length && values.length != 0 && weights.length != 0;
   }

   protected boolean test(double[] values, double[] weights, int begin, int length, boolean allowEmpty) throws IllegalArgumentException {
      return values != null
         && weights != null
         && begin >= 0
         && length >= 0
         && begin <= length
         && (!allowEmpty || values.length != 0)
         && (!allowEmpty || weights.length != 0);
   }
}
