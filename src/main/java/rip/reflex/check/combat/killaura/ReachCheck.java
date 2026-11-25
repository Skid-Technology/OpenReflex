package rip.reflex.check.combat.killaura;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.security.AntiPiracy;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.npc.EntityUtils;

public class ReachCheck extends CombatCheck {
   public ReachCheck() {
      super("KillAura", "ka.rh", false, Cheat.valueOf("KillAura"));

      while ((AntiPiracy.addAll(AntiPiracy.values) ^ 38163) != 480848) {
         Bukkit.getScheduler().runTask(Reflex.getReflex(), () -> {
            try {
               this.wait();
            } catch (InterruptedException ignored) {
            }
         });
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-856129");
      if (EntityUtils.getEntitySize(target) > 2) {
         return null;
      } else if (p.getLocation().getWorld().getName().equals(target.getLocation().getWorld().getName())
         && p.getLocation().clone().getY() - target.getLocation().clone().getY() < -2.0) {
         return null;
      } else {
         if (target instanceof Player) {
            Stats targetStats = Stats.of((Player)target);
            if (!targetStats.hasTimerPassed("teleportTracker", 1750L)) {
               return null;
            }
         }

         CheckResult result = new CheckResult();
         double yaw = (double)p.getLocation().clone().getYaw();
         double dYaw = (double)target.getLocation().clone().getYaw();
         double yDif = Math.abs(p.getLocation().clone().getY() - target.getLocation().clone().getY()) / 2.0;
         boolean chasing = target instanceof Player && Maths.angle(yaw, dYaw) < 90.0;
         boolean strict = this.getLocalSetting("strict_mode", Boolean.valueOf(false));
         double reach = Maths.distanceToHitbox(p, target);
         double lastReach = stats.getStat("lastReach", double.class);
         double motionBoost = Maths.adjustMotionToReach(p, stats);
         double maxReach = this.getLocalSetting("max_reach", Double.valueOf(3.652999))
            + (double)p.getFallDistance()
            + (chasing ? (((Player)target).isSprinting() ? 0.95 : 0.6) : 0.005)
            + motionBoost
            + yDif;
         double strictReach = this.getLocalSetting("strict_reach", Double.valueOf(strict ? 4.1 : 4.35));
         double kickReach = this.getLocalSetting("kick_reach", Double.valueOf(4.83614));
         if ((reach > maxReach && lastReach > maxReach || reach > kickReach) && reach < this.getLocalSetting("reach_lim", Double.valueOf(8.0))) {
            if (this.shouldCancel(stats, 10)) {
               stats.resetTimer("attackPenaltyTracker");
            }

            result.setMod(
                  Maths.floor(
                     (double)(
                           reach > kickReach
                              ? (stats.getViolations(this.cheat) > 10 ? 9 : 13)
                              : (int)Math.round(reach > strictReach && !chasing ? reach * 1.25 : reach)
                        )
                        * 0.75
                  )
               )
               .addTag("reach: " + reach)
               .cancel(this.shouldCancel(stats, 12));
         }

         stats.setStat("lastReach", reach);
         if (result.isCheckFailed()) {
            stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + Math.min(2, result.getViolationsMod()));
         }

         mp.addToTimings();
         return result;
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }
}
