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

public class AccuracyCheck extends CombatCheck {
   public AccuracyCheck() {
      super("KillAura", "ka.acr", true, Cheat.valueOf("KillAura"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      MethodProfiler mp = new MethodProfiler("CT-36515");
      CheckResult result = new CheckResult();
      double angle = (double)Math.abs(from.getYaw() - to.getYaw());
      if (angle % 1.0 == 0.0 && Maths.matchEx(angle, 0, 360)) {
         stats.setStat("accuracyViolations", stats.getStat("accuracyViolations", int.class) + 3);
      } else if (stats.getStat("accuracyViolations", int.class) > 1) {
         stats.setStat("accuracyViolations", stats.getStat("accuracyViolations", int.class) - 1);
      }

      if (stats.getStat("accuracyViolations", int.class) > 18) {
         result.addTag("rotation accuracy").setMod(1000);
         stats.setStat("accuracyViolations", 0);
      }

      if (result.isCheckFailed()) {
         stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + result.getViolationsMod());
      }

      mp.addToTimings();
      return result;
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      return null;
   }
}
