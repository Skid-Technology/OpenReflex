package rip.reflex.util.performance;

import java.util.Arrays;
import rip.reflex.util.math.Maths;

public enum LagStatus {
   NOT_ESTIMATED("not estimated", -1, 0, 0.0),
   HASTY("hasty", 1, 8, 1.25),
   OK("ok", 9, 26, 1.0),
   MINOR_LAG("minor lag", 27, 39, 0.8),
   NOTICEABLE_LAG("noticeable lag", 40, 57, 0.69),
   VIVID_LAG("vivid lag", 58, 78, 0.37),
   EXTREME_LAG("extreme lag", 79, Integer.MAX_VALUE, 0.2);

   private final String fancyName;
   private final int minLagPoints;
   private final int maxLagPoints;
   private final double violationsWeight;

   private LagStatus(String fancyName, int minLagPoints, int maxLagPoints, double violationsWeight) {
      this.fancyName = fancyName;
      this.minLagPoints = minLagPoints;
      this.maxLagPoints = maxLagPoints;
      this.violationsWeight = violationsWeight;
   }

   public double getViolationsWeight() {
      return this.violationsWeight;
   }

   public int correctVl(int vl) {
      return vl > 1 ? Maths.floor((double)vl * this.violationsWeight) : vl;
   }

   public String fancyName() {
      return this.fancyName;
   }

   public boolean isLowerThan(LagStatus other) {
      return this.ordinal() < other.ordinal();
   }

   public boolean hasMininum(LagStatus other) {
      return this.ordinal() >= other.ordinal();
   }

   public static LagStatus get(int lagPoints) {
      return Maths.matchIn(lagPoints, 360, 390)
         ? OK
         : Arrays.stream(values()).filter(v -> Maths.matchIn(lagPoints, v.minLagPoints, v.maxLagPoints)).findFirst().orElse(NOT_ESTIMATED);
   }
}
