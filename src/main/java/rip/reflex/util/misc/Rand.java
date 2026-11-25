package rip.reflex.util.misc;

import java.util.SplittableRandom;

public class Rand {
   private static final SplittableRandom random = new SplittableRandom();

   public static float invalidFloat() {
      switch (random.nextInt(3)) {
         case 0:
            return Float.NaN;
         case 1:
            return Float.POSITIVE_INFINITY;
         case 2:
            return Float.NEGATIVE_INFINITY;
         default:
            throw new UnknownError("nextInt(3) generated number which is out of range[0;2]?!");
      }
   }
}
