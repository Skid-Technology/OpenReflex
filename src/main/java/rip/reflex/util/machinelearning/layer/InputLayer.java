package rip.reflex.util.machinelearning.layer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.machinelearning.Dataset;
import rip.reflex.util.machinelearning.KeyInfo;
import rip.reflex.util.math.AtomicDouble;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.stat.Statistics;

public class InputLayer extends Layer {
   private final Dataset in;
   private final ArrayList<Dataset> lessonSet = new ArrayList<>();
   private OutputLayer output;
   private int cheatPoints;
   private String cheatAttributes = "";

   public InputLayer(Dataset in) {
      this.in = in;
   }

   @Override
   public void computeOutput() {
      if (this.in != null && this.lessonSet.size() >= 2) {
         HashMap<Dataset, Double> totalError = new LinkedHashMap<>();
         HashMap<Dataset, Integer> weights = new LinkedHashMap<>();
         KeyInfo info = new KeyInfo();

         for (Dataset ds : this.lessonSet) {
            totalError.put(ds, 0.0);
            weights.put(ds, 0);
         }

         for (int keyCode = 0; keyCode < 2; keyCode++) {
            String key = info.getKey(keyCode);

            for (int idx = 0; idx < this.in.getData().get(key).size(); idx++) {
               double smallestError = 1.0E30;
               double wonVec = 1.0E30;

               for (Dataset vec : this.lessonSet) {
                  double expected = this.in.getData().get(key).get(idx);
                  double present = vec.getData().get(key).get(idx);
                  double error = Math.abs(expected - present);
                  totalError.put(vec, totalError.get(vec) + error);
                  if (error < smallestError) {
                     smallestError = error;
                     wonVec = present;
                  }
               }

               for (Dataset vecx : this.lessonSet) {
                  if (vecx.getData().get(key).get(idx) == wonVec) {
                     weights.put(vecx, weights.get(vecx) + 1);
                  }
               }
            }
         }

         AtomicInteger legitsInTop100 = new AtomicInteger();
         AtomicInteger legitsInTop500 = new AtomicInteger();
         NumberStorage legitsPer100 = new NumberStorage(1000);
         AtomicInteger tmpLegits = new AtomicInteger();
         AtomicDouble minLegitErr = new AtomicDouble(1.0E30);
         AtomicDouble maxLegitErr = new AtomicDouble();
         AtomicInteger index = new AtomicInteger();
         totalError.entrySet().stream().sorted(Entry.comparingByValue()).forEach(entry -> {
            Dataset dsx = entry.getKey();
            double error = entry.getValue();
            int i = index.get();
            if (dsx.isLegit()) {
               if (i < 500) {
                  legitsInTop500.getAndIncrement();
                  if (i < 100) {
                     legitsInTop100.getAndIncrement();
                  }
               }

               tmpLegits.getAndIncrement();
            }

            if (i % 100 == 0) {
               legitsPer100.add((double)tmpLegits.get());
               tmpLegits.set(0);
            }

            if (error < minLegitErr.get()) {
               minLegitErr.set(error);
            }

            if (error > maxLegitErr.get()) {
               maxLegitErr.set(error);
            }

            index.getAndIncrement();
         });
         Statistics stats = Maths.describe(legitsPer100);
         if (legitsInTop100.get() < 15) {
            this.cheatAttributes = this.cheatAttributes + "a";
            this.cheatPoints += 2;
         }

         if (legitsInTop500.get() < 80) {
            this.cheatAttributes = this.cheatAttributes + "b";
            this.cheatPoints += 2;
         }

         if (minLegitErr.get() > 650.0) {
            this.cheatAttributes = this.cheatAttributes + "c";
            this.cheatPoints += 3;
         }

         if (!Maths.matchIn(stats.getVariance(), 200.0, 265.0)) {
            boolean veryBad = stats.getVariance() > 150.0;
            this.cheatAttributes = this.cheatAttributes + (veryBad ? "d+" : "d");
            this.cheatPoints += veryBad ? 3 : 2;
         }

         if (!Maths.matchIn(stats.getSkewness(), 0.8, 1.4)) {
            this.cheatAttributes = this.cheatAttributes + "e";
            this.cheatPoints++;
         }

         if (stats.getKurtosis() < 1.0) {
            this.cheatAttributes = this.cheatAttributes + "f";
            this.cheatPoints += 2;
         }

         if (minLegitErr.get() / maxLegitErr.get() < 0.15) {
            this.cheatAttributes = this.cheatAttributes + "g";
            this.cheatPoints += 2;
         }

         Dataset[] heaviest3 = this.heaviest3(weights);
         HashMap<Dataset, Integer> sig = new HashMap<>();

         for (Dataset ds : heaviest3) {
            sig.put(ds, weights.get(ds));
         }

         this.output = new OutputLayer(weights, new SigmoidLayer(sig));
      }
   }

   @Override
   public void learn(ArrayList<Dataset> lessonSet) {
      lessonSet.stream().filter(lesson -> !this.lessonSet.contains(lesson)).forEach(this.lessonSet::add);
   }

   private Dataset[] heaviest3(HashMap<Dataset, Integer> weights) {
      ArrayList<Integer> values = new ArrayList<>(weights.values());
      int[] vec = new int[values.size()];
      int idx = 0;

      for (int i : values) {
         vec[idx++] = i;
      }

      int[] top3 = this.highest3(vec);
      Dataset[] heaviest3 = new Dataset[3];

      for (int x = 0; x < top3.length; x++) {
         for (Dataset ds : weights.keySet()) {
            if (weights.get(ds) == top3[x]) {
               heaviest3[x] = ds;
            }
         }
      }

      return heaviest3;
   }

   private int[] highest3(int[] arrOrig) {
      Arrays.sort(arrOrig);
      int[] arr = new int[3];

      for (int i = 0; i < 3; i++) {
         arr[i] = this.findAny(arrOrig, i + 1);
      }

      return arr;
   }

   private int findAny(int[] arrOrig, int index) {
      int[] arr = new int[arrOrig.length];

      for (int idx = 0; idx < arrOrig.length; idx++) {
         if (!this.arrayContains(arr, arrOrig[idx])) {
            arr[idx] = arrOrig[idx];
         }
      }

      Arrays.sort(arr);
      return arr[arr.length - index];
   }

   private boolean arrayContains(int[] arr, int element) {
      return Arrays.stream(arr).anyMatch(i -> i == element);
   }

   public OutputLayer getOutput() {
      return this.output;
   }

   public void clear() {
      this.lessonSet.clear();
   }

   public int getCheatPoints() {
      return this.cheatPoints;
   }

   public String getCheatAttributes() {
      return this.cheatAttributes;
   }
}
