package rip.reflex.util.math.descriptive;

public interface UnivariateStatistic {
   double evaluate(double[] values) throws IllegalArgumentException;

   double evaluate(double[] values, int begin, int length) throws IllegalArgumentException;

   UnivariateStatistic copy();
}
