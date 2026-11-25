package rip.reflex.util.machinelearning.layer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import rip.reflex.util.machinelearning.Dataset;

public class OutputLayer extends Layer {
   private final ArrayList<Dataset> hidden = new ArrayList<>();
   private final SigmoidLayer sigmoid;
   private final Dataset[] winners = new Dataset[3];

   public OutputLayer(HashMap<Dataset, Integer> weights, SigmoidLayer sigmoid) {
      this.sigmoid = sigmoid;
   }

   @Override
   public void computeOutput() {
      float[] mostProbable = this.highest3(this.sigmoid.probabilityChances());

      for (int idx = 0; idx < mostProbable.length; idx++) {
         this.winners[idx] = this.byValue(mostProbable[idx]);
      }
   }

   @Override
   public void learn(ArrayList<Dataset> lessonSet) {
      lessonSet.stream().filter(e -> !this.hidden.contains(e)).forEach(this.hidden::add);
   }

   private float[] highest3(float[] arrOrig) {
      Arrays.sort(arrOrig);
      float[] arr = new float[3];

      for (int i = 0; i < 3; i++) {
         arr[i] = arrOrig[arrOrig.length - (i + 1)];
      }

      return arr;
   }

   private Dataset byValue(float f) {
      for (Dataset ds : this.hidden) {
         if (this.sigmoid.isKnown(ds) && this.sigmoid.getProbabilityChance(ds) == f) {
            return ds;
         }
      }

      return null;
   }

   public SigmoidLayer getSigmoid() {
      return this.sigmoid;
   }

   public Dataset[] getWinners() {
      return this.winners;
   }

   public void clear() {
      this.hidden.clear();
   }
}
