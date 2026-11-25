package rip.reflex.check.combat.killaura;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.CheckLoader;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.machinelearning.Classification;
import rip.reflex.util.machinelearning.Classifier;
import rip.reflex.util.machinelearning.Dataset;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.time.Timer;

public class HeuristicsCheck extends CombatCheck {
   private long checkID;
   private int optExtremeCpThreshold;
   private int optStableCpThreshold;
   private int optCertainCpThreshold;

   public HeuristicsCheck() {
      super("KillAura", "ka.heu", false, Cheat.valueOf("KillAura"));
      if (!Reflex.getReflex().getCloudService().isConnected()) {
         Classifier.init();
      }

      this.supportCloudComputings();
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optExtremeCpThreshold = this.getAdvancedLocalSetting("extreme_cp_threshold", Integer.valueOf(6));
      this.optStableCpThreshold = this.getAdvancedLocalSetting("stable_cp_threshold", Integer.valueOf(3));
      this.optCertainCpThreshold = this.getAdvancedLocalSetting("certain_cp_threshold", Integer.valueOf(2));
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      if (stats.getLagStatus() != LagStatus.EXTREME_LAG && !this.reflex.offlineMode) {
         MethodProfiler mp = new MethodProfiler("CT-1639415");
         CheckResult result = new CheckResult();
         int heuristicsLength = 100;
         HashMap<String, ArrayList<Double>> data = stats.getHeuristicsData();
         if (data.containsKey(".4c")
            && data.containsKey(".1f")
            && data.get(".4c").size() >= heuristicsLength
            && data.get(".1f").size() >= heuristicsLength
            && Maths.objects.length >= 5
            && !(Maths.objects[4] instanceof Exception)) {
            Dataset ds = new Dataset(
               ++this.checkID + "-" + p.getName(), (HashMap<String, ArrayList<Double>>)Misc.<String, ArrayList<Double>>copyMap(data, false), false
            );
            Dataset copy = ds.clone();
            int debug = this.getLocalSetting("debug_heuristics", Integer.valueOf(0));
            if (this.reflex.getCloudService().isConnected()) {
               this.reflex.getCloudService().getCloudComputer().heuristicsCheck(p, ds, debug);
            } else {
               Classification c = Classifier.classify(ds);
               int severity = this.getLocalSetting("heuristics_severity", Integer.valueOf(1));
               HeuristicsCheck.FinalDecision decision = isCheater(c.countBadPatterns(), severity, c.getCheatPoints(), stats.getLagStatus());
               if (decision.isCheater()) {
                  result.setMod(c.countBadPatterns() * 100)
                     .addTag("suspicious combat style (NN analysis)")
                     .addTag("best match: #" + c.bestMatched().getName().replace("P", "").replace("L", "*"))
                     .addTag("conviction: " + (c.confidence() - Math.random() * 5.0) + "%")
                     .addTag("severity: " + severity)
                     .addTag("network version: " + this.reflex.getDataVersion())
                     .addTag("cp: " + c.getCheatPoints())
                     .addTagIf("attr: " + c.getCheatAttributes(), c.getCheatPoints() > 0)
                     .addTagIf("cp::wth", decision.isWayTooHighCP())
                     .addTagIf("cp::hnl", decision.isHighCPNotLagging())
                     .addTagIf("cp::hct", decision.isHighCPCertain())
                     .addTagIf("snapshot id: " + copy.getName(), debug > 0);
               }

               Classifier.debugHeur(!decision.isCheater(), copy.setLegit(!decision.isCheater()), p, debug);
            }

            stats.getStat("attackTracker", Timer.class).reset();
            stats.getHeuristicsData().clear();
            if (result.isCheckFailed()) {
               stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 1);
            }

            mp.addToTimings();
            return result;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }

   public static HeuristicsCheck.FinalDecision isCheater(int detected, int severity, int cp, LagStatus lagStatus) {
      boolean wayTooHighCP = cp > CheckLoader.heuristicsCheck.optExtremeCpThreshold;
      boolean highCPNotLagging = lagStatus.ordinal() < LagStatus.VIVID_LAG.ordinal() && cp > CheckLoader.heuristicsCheck.optStableCpThreshold;
      boolean highCPCertain = detected == 3 && cp > CheckLoader.heuristicsCheck.optCertainCpThreshold;
      return new HeuristicsCheck.FinalDecision(
         detected > severity && (wayTooHighCP || highCPNotLagging || highCPCertain), wayTooHighCP, highCPNotLagging, highCPCertain
      );
   }

   public static class FinalDecision {
      private final boolean cheater;
      private final boolean wayTooHighCP;
      private final boolean highCPNotLagging;
      private final boolean highCPCertain;

      public FinalDecision(boolean cheater, boolean wayTooHighCP, boolean highCPNotLagging, boolean highCPCertain) {
         this.cheater = cheater;
         this.wayTooHighCP = wayTooHighCP;
         this.highCPNotLagging = highCPNotLagging;
         this.highCPCertain = highCPCertain;
      }

      public boolean isCheater() {
         return this.cheater;
      }

      public boolean isWayTooHighCP() {
         return this.wayTooHighCP;
      }

      public boolean isHighCPNotLagging() {
         return this.highCPNotLagging;
      }

      public boolean isHighCPCertain() {
         return this.highCPCertain;
      }
   }
}
