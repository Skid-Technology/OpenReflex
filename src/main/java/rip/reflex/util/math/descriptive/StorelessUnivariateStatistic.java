package rip.reflex.util.math.descriptive;

public interface StorelessUnivariateStatistic extends UnivariateStatistic {
   void increment(double value);

   void incrementAll(double[] values) throws IllegalArgumentException;

   void incrementAll(double[] values, int begin, int length) throws IllegalArgumentException;

   double getResult();

   long getN();

   void clear();

   StorelessUnivariateStatistic copy();
}
