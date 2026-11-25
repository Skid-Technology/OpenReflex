package rip.reflex.check.combat.killaura;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.mcaccess.BoundingBox;
import rip.reflex.util.protocol.mcaccess.MCAccess;
import rip.reflex.util.protocol.npc.EntityUtils;
import rip.reflex.util.time.Timer;

public class DirectionCheck extends CombatCheck {
   public DirectionCheck() {
      super("KillAura", "ka.drn", false, Cheat.valueOf("KillAura"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-2658713");
      if (EntityUtils.getEntitySize(target) != 2) {
         return null;
      } else {
         CheckResult result = new CheckResult();
         int nearbyEntities = p.getNearbyEntities(5.0, 5.0, 5.0).size();
         if (nearbyEntities <= (VersionUtils.isOn19mechanics() ? 1 : 3) && stats.getStat("targetSwitchTracker", Timer.class).hasPassed(1300L)) {
            BoundingBox bb = MCAccess.getEntityBoundingBox(target);
            Location loc = p.getLocation().clone();
            Location dLoc = target.getLocation().clone();
            Location hitPoint = loc.add(loc.getDirection().normalize().multiply(Maths.distance(loc, dLoc)));
            double offset = bb.distance(hitPoint);
            if (!bb.isWithinBoundingBox(hitPoint) && offset < 8.0) {
               if (this.shouldCancel(stats, 10)) {
                  stats.resetTimer("attackPenaltyTracker");
               }

               result.cancel(this.shouldCancel(stats, 8)).setMod((int)(offset * 1.3)).addTag("angle");
            }

            if (result.isCheckFailed()) {
               stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 1);
            }

            mp.addToTimings();
            return result;
         } else {
            return null;
         }
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }
}
