package rip.reflex.check.combat.killaura;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.descriptive.stat.Statistics;
import rip.reflex.util.performance.MethodProfiler;

public class YawStatisticCheck extends CombatCheck {
   public YawStatisticCheck() {
      super("KillAura", "ka.yst", true, Cheat.valueOf("KillAura"));
      this.supportCloudComputings();
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      MethodProfiler mp = new MethodProfiler("CT-2983719");
      CheckResult result = new CheckResult();
      if (stats.hasStat("currentTarget") && !this.reflex.offlineMode) {
         LivingEntity target = stats.getStat("currentTarget", LivingEntity.class);
         if (Maths.distanceExact(p, target) < 1.8) {
            return null;
         } else {
            if (!stats.hasStat("headMovementPatternSmall")) {
               stats.setStat("headMovementPatternSmall", new NumberStorage(50));
            }

            if (!stats.hasStat("headMovementPatternMedium")) {
               stats.setStat("headMovementPatternMedium", new NumberStorage(75));
            }

            if (!stats.hasStat("headMovementPatternBig")) {
               stats.setStat("headMovementPatternBig", new NumberStorage(100));
            }

            NumberStorage smallStorage = stats.getStat("headMovementPatternSmall", NumberStorage.class);
            NumberStorage mediumStorage = stats.getStat("headMovementPatternMedium", NumberStorage.class);
            NumberStorage bigStorage = stats.getStat("headMovementPatternBig", NumberStorage.class);
            double angle = Maths.angle((double)from.getYaw(), (double)to.getYaw());
            if (!Maths.matchEx(angle, 0.7, 60)) {
               return null;
            } else {
               smallStorage.add(angle);
               mediumStorage.add(angle);
               bigStorage.add(angle);
               if (Maths.objects.length > 11
                  && Maths.objects[11] != null
                  && Long.toString((Long)Maths.objects[11] ^ -58791205879L).startsWith("-95953614030458")) {
                  if (smallStorage.isFull()) {
                     if (this.reflex.getCloudService().isConnected()) {
                        this.reflex.getCloudService().getCloudComputer().smallYawStatisticCheck(p, smallStorage.copy());
                     } else {
                        Statistics stat = Maths.describe(smallStorage);
                        double deltaMeaning = Math.abs(stat.getMean() - stat.getMedian());
                        double deltaVariance = Math.abs(stat.getHdev() - stat.getVariance());
                        double ratio = stat.getBalance() / stat.getMedian();
                        double hvar = Math.abs(stat.getVariance() - stat.getHighest());
                        if (smallStorage.sum() != 0.0
                           && hvar > 0.1
                           && !Maths.matchEx(ratio, 0.0575, 0.08)
                           && !Maths.matchEx(ratio, 0.09, 0.13)
                           && stat.getBalance() < 0.5025
                           && !Maths.matchEx(stat.getBalance(), 0.46, 0.485)
                           && stat.getMedian() < 10.0
                           && Maths.matchEx(stat.getLowest(), 0.5, 1.8)
                           && Maths.matchEx(stat.getHighest(), 7.5, 30.0)
                           && Maths.matchEx(deltaMeaning, 0.0215, 1.3)
                           && Maths.matchEx(stat.getVariance(), 5.0, 38.0)
                           && stat.getAdev() > 2.45
                           && !Maths.matchIn(stat.getAdev(), 3.05, 3.35)
                           && stat.getLdev() > 5.6
                           && Maths.matchEx(deltaVariance, 0.4, 2.8)
                           && deltaVariance < 8.5) {
                           result.setMod(100)
                              .addTag("aimbot (short-term statistic)")
                              .addTag("s: " + stat.getBalance())
                              .addTag("m: " + stat.getMedian())
                              .addTag("dm: " + deltaMeaning)
                              .addTag("dv: " + deltaVariance)
                              .addTag("a: " + stat.getAdev())
                              .addTag("l: " + stat.getLdev())
                              .addTag("r: " + ratio);
                        }
                     }

                     smallStorage.clear();
                  }

                  if (mediumStorage.isFull()) {
                     if (this.reflex.getCloudService().isConnected()) {
                        this.reflex.getCloudService().getCloudComputer().mediumYawStatisticCheck(p, mediumStorage.copy());
                     } else {
                        Statistics stat = Maths.describe(mediumStorage);
                        double mean = stat.getMean();
                        double var = stat.getVariance();
                        double sk = stat.getSkewness();
                        double kt = stat.getKurtosis();
                        if (mediumStorage.sum() != 0.0 && (var > 850.0 || !Maths.matchEx(sk, -0.525, 5.8) || !Maths.matchEx(kt, -1.26, 42.8))) {
                           result.setMod(100)
                              .addTag("aimbot (middle-term statistic)")
                              .addTag("m: " + mean)
                              .addTag("v: " + var)
                              .addTag("s: " + sk)
                              .addTag("k: " + kt);
                        }
                     }

                     mediumStorage.clear();
                  }

                  if (bigStorage.isFull()) {
                     if (this.reflex.getCloudService().isConnected()) {
                        this.reflex.getCloudService().getCloudComputer().bigYawStatisticCheck(p, bigStorage.copy());
                     } else {
                        Statistics stat = Maths.describe(bigStorage);
                        double sk = stat.getSkewness();
                        double kt = stat.getKurtosis();
                        double ldev = stat.getLdev();
                        double adev = stat.getAdev();
                        if (bigStorage.sum() != 0.0
                           && (
                              !Maths.matchEx(sk, 0.03, 5.675)
                                 || !Maths.matchEx(kt, -1.19, 37.5)
                                 || adev < 0.475 && !Maths.matchIn(adev, 0.2, 0.3)
                                 || ldev < 1.225
                           )) {
                           result.setMod(100)
                              .addTag("aimbot (long-term statistic)")
                              .addTag("s: " + sk)
                              .addTag("k: " + kt)
                              .addTag("a: " + adev)
                              .addTag("l: " + ldev);
                        }
                     }

                     bigStorage.clear();
                  }

                  if (result.isCheckFailed()) {
                     stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + result.getViolationsMod());
                  }

                  mp.addToTimings();
               }

               return result;
            }
         }
      } else {
         return null;
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      return null;
   }
}
