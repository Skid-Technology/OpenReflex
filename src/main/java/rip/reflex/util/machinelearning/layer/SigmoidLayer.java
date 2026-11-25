package rip.reflex.util.machinelearning.layer;

import java.util.ArrayList;
import java.util.HashMap;
import rip.reflex.util.machinelearning.Classifier;
import rip.reflex.util.machinelearning.ComputeError;
import rip.reflex.util.machinelearning.Dataset;

public class SigmoidLayer extends Layer {
   private final HashMap<Dataset, Float> probabilityChances = new HashMap<>();
   private final HashMap<Dataset, Integer> weights;
   private final ArrayList<Dataset> hidden = new ArrayList<>();

   public SigmoidLayer(HashMap<Dataset, Integer> weights) {
      this.weights = weights;
   }

   @Override
   public void computeOutput() {
      ArrayList<Dataset> learnable = new ArrayList<>();
      learnable.addAll(this.weights.keySet());
      this.learn(learnable);
      if (this.hidden.size() < 3) {
         Classifier.crash("computing sigmoid output", new ComputeError("learnable size is too small (" + this.hidden.size() + "), expected at least 3"));
      } else {
         this.probabilityChances.clear();

         for (Dataset ds : this.hidden) {
            this.probabilityChances.put(ds, 0.0F);
         }

         float w0 = (float)this.weights.get(this.hidden.get(0)).intValue();
         float w1 = (float)this.weights.get(this.hidden.get(1)).intValue();
         float w2 = (float)this.weights.get(this.hidden.get(2)).intValue();
         float sig = Math.abs(w0 + w1 + w2);
         float x0 = w0 * 100.0F / sig;
         float x1 = w1 * 100.0F / sig;
         float x2 = w2 * 100.0F / sig;
         this.probabilityChances.put(this.byWeight((int)w0), x0);
         this.probabilityChances.put(this.byWeight((int)w1), x1);
         this.probabilityChances.put(this.byWeight((int)w2), x2);
      }
   }

   @Override
   public void learn(ArrayList<Dataset> lessonSet) {
      lessonSet.stream().filter(e -> !this.hidden.contains(e)).forEach(this.hidden::add);
   }

   private Dataset byWeight(int weight) {
      return this.hidden.stream().filter(ds -> this.weights.get(ds) == weight).findFirst().orElse(null);
   }

   public float getProbabilityChance(Dataset ds) {
      return this.probabilityChances.getOrDefault(ds, 0.0F);
   }

   public float[] probabilityChances() {
      ArrayList<Float> values = new ArrayList<>();
      values.addAll(this.probabilityChances.values());
      float[] vec = new float[3];
      int idx = 0;

      for (float f : values) {
         vec[idx++] = f;
      }

      return vec;
   }

   public boolean isKnown(Dataset ds) {
      return this.hidden.contains(ds) || this.probabilityChances.containsKey(ds);
   }

   public void clear() {
      this.probabilityChances.clear();
      this.weights.clear();
      this.hidden.clear();
   }
}
