package rip.reflex.cloud;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckLoader;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.combat.killaura.HeuristicsCheck;
import rip.reflex.cloud.protocol.client.ComputeRequestPacket;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.machinelearning.Classifier;
import rip.reflex.util.machinelearning.Dataset;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class CloudComputer {
   private final ConcurrentHashMap<Long, ComputeEntry> awaitingResponse = new ConcurrentHashMap<>();
   private static final long MAX_RESPONSE_AWAIT_TIME = TimeUnit.SECONDS.toMillis(15L);
   private long requestID = 0L;
   private static Check killaura = null;
   private final Object requestIDLock = new Object();

   public static void init() {
      killaura = CheckRegistry.getCheck(Cheat.valueOf("KillAura"));
   }

   public void cleanup() {
      List<Long> toRem = new ArrayList<>();

      for (long id : this.awaitingResponse.keySet()) {
         if (this.awaitingResponse.get(id).getQueryTracker().hasPassed(MAX_RESPONSE_AWAIT_TIME)) {
            toRem.add(id);
         }
      }

      toRem.forEach(this.awaitingResponse::remove);
   }

   private void requestCompute(Player p, int checkID, Object checkData, int debugML, Dataset heurSnapshot) {
      if (!Reflex.getReflex().offlineMode) {
         synchronized (this.requestIDLock) {
            long id = ++this.requestID;
            this.awaitingResponse.put(id, new ComputeEntry(p, debugML, heurSnapshot));
            ReflexScheduler.async()
               .run(
                  ReflexRunnable.simple(
                     "KR-56791238",
                     () -> Reflex.getReflex().getCloudService().getClient().getProtocolManager().sendPacket(new ComputeRequestPacket(id, checkID, checkData))
                  )
               );
         }
      }
   }

   public void heuristicsCheck(Player p, Dataset ds, int debug) {
      if (killaura == null) {
         init();
      }

      this.requestCompute(p, 0, ds.serialize(), debug, debug > 0 ? ds.clone() : null);
   }

   public void smallYawStatisticCheck(Player p, NumberStorage smallStorage) {
      if (killaura == null) {
         init();
      }

      this.requestCompute(p, 1, smallStorage.serialize(), 0, null);
   }

   public void mediumYawStatisticCheck(Player p, NumberStorage mediumStorage) {
      if (killaura == null) {
         init();
      }

      this.requestCompute(p, 2, mediumStorage.serialize(), 0, null);
   }

   public void bigYawStatisticCheck(Player p, NumberStorage bigStorage) {
      if (killaura == null) {
         init();
      }

      this.requestCompute(p, 3, bigStorage.serialize(), 0, null);
   }

   public void process(Player p, int checkID, String response, int debugML, Dataset heurSnapshot) {
      if (!Reflex.getReflex().offlineMode) {
         try {
            MethodProfiler mp = new MethodProfiler("DX-1293091");
            if (checkID == -1) {
               Reflex.getReflex().warn("Cloud responsed with an error. Please report this!");
               return;
            }

            String[] s = response.split("//");
            Stats stats = Stats.of(p);
            boolean legit = s[0].equals("LEGIT");
            switch (checkID) {
               case 0:
                  if (!legit && stats.canBeChecked(killaura)) {
                     int severity = killaura.getLocalSetting("heuristics_severity", 1);
                     int detected = Integer.parseInt(s[1]);
                     int cp = Integer.parseInt(s[4]);
                     String cheatAttr = s[5];
                     HeuristicsCheck.FinalDecision decision = HeuristicsCheck.isCheater(detected, severity, cp, stats.getLagStatus());
                     if (decision.isCheater()) {
                        CheckResult result = new CheckResult()
                           .setMod(detected * 100)
                           .addTag("suspicious combat style (NN analysis)")
                           .addTag("best match: #" + s[2])
                           .addTag("conviction: " + (Double.valueOf(s[3]) - Math.random() * 5.0) + "%")
                           .addTag("severity: " + severity)
                           .addTag("cloud")
                           .addTag("cp: " + cp)
                           .addTagIf("attr: " + cheatAttr, cp > 0)
                           .addTagIf("cp::wth", decision.isWayTooHighCP())
                           .addTagIf("cp::hnl", decision.isHighCPNotLagging())
                           .addTagIf("cp::hct", decision.isHighCPCertain())
                           .addTagIf(heurSnapshot == null ? "?" : "snapshot id: " + heurSnapshot.getName(), debugML > 0);
                        Check.addVl(stats, CheckLoader.heuristicsCheck.getCheat(), result);
                        Check.check(p, stats, null, CheckLoader.heuristicsCheck, result);
                        stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 100);
                     }

                     if (heurSnapshot != null) {
                        Classifier.debugHeur(!decision.isCheater(), heurSnapshot.setLegit(!decision.isCheater()), p, debugML);
                     }
                  }
                  break;
               case 1:
                  if (!legit && stats.canBeChecked(killaura)) {
                     CheckResult result = new CheckResult()
                        .setMod(100)
                        .addTag("aimbot (short-term statistic)")
                        .addTag("s: " + s[1])
                        .addTag("m: " + s[2])
                        .addTag("dm: " + s[3])
                        .addTag("dv: " + s[4])
                        .addTag("a: " + s[5])
                        .addTag("l: " + s[6])
                        .addTag("r: " + s[7])
                        .addTag("cloud");
                     Check.addVl(stats, CheckLoader.yawStatCheck.getCheat(), result);
                     Check.check(p, stats, null, CheckLoader.yawStatCheck, result);
                     stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 100);
                  }
                  break;
               case 2:
                  if (!legit && stats.canBeChecked(killaura)) {
                     CheckResult result = new CheckResult()
                        .setMod(100)
                        .addTag("aimbot (middle-term statistic)")
                        .addTag("m: " + s[1])
                        .addTag("v: " + s[2])
                        .addTag("s: " + s[3])
                        .addTag("k: " + s[4])
                        .addTag("cloud");
                     Check.addVl(stats, CheckLoader.yawStatCheck.getCheat(), result);
                     Check.check(p, stats, null, CheckLoader.yawStatCheck, result);
                     stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 100);
                  }
                  break;
               case 3:
                  if (!legit && stats.canBeChecked(killaura)) {
                     CheckResult result = new CheckResult()
                        .setMod(100)
                        .addTag("aimbot (long-term statistic)")
                        .addTag("s: " + s[1])
                        .addTag("k: " + s[2])
                        .addTag("a: " + s[3])
                        .addTag("l: " + s[4])
                        .addTag("cloud");
                     Check.addVl(stats, CheckLoader.yawStatCheck.getCheat(), result);
                     Check.check(p, stats, null, CheckLoader.yawStatCheck, result);
                     stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 100);
                  }
                  break;
               default:
                  Reflex.getReflex().getLogger().warning("Unexpected response from Cloud: \"" + response + "\" (" + checkID + ")");
            }

            mp.addToTimings();
         } catch (Exception ex) {
            System.err.println("ERROR PROCESSING CLOUD COMPUTE RESPONSE");
            ex.printStackTrace();
         }
      }
   }

   public ConcurrentHashMap<Long, ComputeEntry> getAwaitingResponse() {
      return this.awaitingResponse;
   }
}
