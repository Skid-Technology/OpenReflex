package rip.reflex.util.math.descriptive;

public interface WeightedEvaluation {
   double evaluate(double[] values, double[] weights) throws IllegalArgumentException;

   double evaluate(double[] values, double[] weights, int begin, int length) throws IllegalArgumentException;
}
