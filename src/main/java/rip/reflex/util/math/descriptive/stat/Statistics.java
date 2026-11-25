package rip.reflex.util.math.descriptive.stat;

import rip.reflex.util.math.Maths;

public class Statistics {
   private final double mean;
   private final double median;
   private final double variance;
   private final double skewness;
   private final double kurtosis;
   private final double ldev;
   private final double hdev;
   private final double adev;
   private final double lowest;
   private final double highest;
   private final double balance;

   public Statistics(double[] data) throws ArithmeticException {
      this.mean = Maths.mean(data);
      this.median = Maths.median(data);
      this.variance = Maths.variance(data);
      this.skewness = Maths.skewness(data);
      this.kurtosis = Maths.kurtosis(data);
      this.ldev = Maths.ldev(data);
      this.hdev = Maths.hdev(data);
      this.adev = Maths.adev(data);
      this.lowest = Maths.min(data);
      this.highest = Maths.max(data);
      this.balance = this.adev / this.ldev;
   }

   public double getMean() {
      return this.mean;
   }

   public double getMedian() {
      return this.median;
   }

   public double getVariance() {
      return this.variance;
   }

   public double getSkewness() {
      return this.skewness;
   }

   public double getKurtosis() {
      return this.kurtosis;
   }

   public double getLdev() {
      return this.ldev;
   }

   public double getHdev() {
      return this.hdev;
   }

   public double getAdev() {
      return this.adev;
   }

   public double getLowest() {
      return this.lowest;
   }

   public double getHighest() {
      return this.highest;
   }

   public double getBalance() {
      return this.balance;
   }
}
