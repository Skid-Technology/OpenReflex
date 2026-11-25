package rip.reflex.util.machinelearning;

import java.util.ArrayList;

public class Classification {
   private final int badPatterns;
   private final double confidence;
   private final ArrayList<Dataset> topMatched;
   private final int cheatPoints;
   private final String cheatAttributes;

   public Classification(int badPatterns, double confidence, ArrayList<Dataset> topMatched, int cheatPoints, String cheatAttributes) {
      this.badPatterns = badPatterns;
      this.confidence = confidence;
      this.topMatched = badPatterns > 0 ? topMatched : null;
      this.cheatPoints = cheatPoints;
      this.cheatAttributes = cheatAttributes;
   }

   public double confidence() {
      return this.confidence;
   }

   public int countBadPatterns() {
      return this.badPatterns;
   }

   public ArrayList<Dataset> topMatched() {
      return this.topMatched;
   }

   public Dataset bestMatched() {
      return this.topMatched.isEmpty() ? null : this.topMatched.get(0);
   }

   public int getCheatPoints() {
      return this.cheatPoints;
   }

   public String getCheatAttributes() {
      return this.cheatAttributes;
   }
}
