package rip.reflex.check.combat;

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
import rip.reflex.util.time.Timer;

public class CriticalsCheck extends CombatCheck {
   public CriticalsCheck() {
      super("Criticals", "crit.mtn", false, Cheat.valueOf("Criticals"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-84928105");
      CheckResult result = new CheckResult();
      if (stats.getStat("damageByEntityTracker", Timer.class).hasPassed(500L) && !p.isSprinting() && !Maths.isInDamageMotion(p)) {
         if (LocationUtils.isGroundExact(p)
            && !LocationUtils.isOnUnstableBlock(p)
            && !p.isSprinting()
            && !LocationUtils.isBelowBlocks(p)
            && p.getFallDistance() >= 0.0625F) {
            result.setMod(4).cancel(this.shouldCancel(stats));
         } else {
            result.setMod(-1);
         }

         result.addTag("motion: " + Maths.toRoundedString((double)p.getFallDistance()));
         if (result.isCheckFailed()) {
            if (!this.isSilent()) {
               stats.setback(stats.getLocation("setbackPos"));
            }

            stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + result.getViolationsMod());
         }

         mp.addToTimings();
         return result;
      } else {
         return null;
      }
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }
}
