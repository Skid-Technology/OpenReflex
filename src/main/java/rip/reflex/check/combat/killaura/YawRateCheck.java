package rip.reflex.check.combat.killaura;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;

public class YawRateCheck extends CombatCheck {
   private int optMaxProbability;

   public YawRateCheck() {
      super("KillAura", "ka.yrt", true, Cheat.valueOf("KillAura"));
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMaxProbability = this.getAdvancedLocalSetting("max_probability", Integer.valueOf(70));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      MethodProfiler mp = new MethodProfiler("CT-561249");
      CheckResult result = new CheckResult();
      if (!stats.hasStat("currentTarget")) {
         return null;
      } else {
         LivingEntity target = stats.getStat("currentTarget", LivingEntity.class);
         if (!LocationUtils.isVerticalOnly(target.getVelocity().clone()) && LocationUtils.isPushable(target)) {
            double d0 = Maths.getDistFromMouse(p, target);
            double d1 = Maths.angle((double)from.getYaw(), (double)to.getYaw());
            double yaw = stats.getStat("lastRotateYaw", double.class);
            if (Maths.matchEx(yaw, 0.85, 360.0) && Maths.matchEx(d1, 0.85, 360.0)) {
               double d = Maths.angle(d0, yaw);
               double y = d / 180.0 * Math.PI;
               if (y < 0.05) {
                  stats.setStat("failedYawRateChecks", stats.getStat("failedYawRateChecks", int.class) + 1);
               }

               stats.setStat("totalYawRateChecks", stats.getStat("totalYawRateChecks", int.class) + 1);
               if (stats.getStat("totalYawRateChecks", int.class) >= this.optMaxProbability) {
                  int probability = Maths.efficiency(stats.getStat("totalYawRateChecks", int.class), stats.getStat("failedYawRateChecks", int.class));
                  int maxProbability = this.getLocalSetting("yawrate_chance", Integer.valueOf(80));
                  if (probability >= maxProbability) {
                     result.addTag("suspicious head movement").addTag("cheating probability: " + probability + "%").setMod(100);
                  }

                  stats.setStat("totalYawRateChecks", 0);
                  stats.setStat("failedYawRateChecks", 0);
               }

               if (result.isCheckFailed()) {
                  stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + result.getViolationsMod());
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
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      return null;
   }
}
