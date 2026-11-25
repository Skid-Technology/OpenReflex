package rip.reflex.check.combat.killaura;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.time.Timer;

public class FightspeedCheck extends CombatCheck {
   public FightspeedCheck() {
      super("KillAura", "ka.fts", false, Cheat.valueOf("KillAura"));
      this.setSync(true);
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      MethodProfiler mp = new MethodProfiler("CT-96519");
      CheckResult result = new CheckResult();
      if ((!VersionUtils.isOn19mechanics() || p.getNearbyEntities(5.0, 5.0, 5.0).size() <= 1) && stats.getStat("attackCounter", int.class) >= 2) {
         if (!stats.getStat("attackTracker0", Timer.class).hasPassed(50L)) {
            if (this.shouldCancel(stats, 10)) {
               stats.resetTimer("attackPenaltyTracker");
            }

            result.setMod(4).cancel(this.shouldCancel(stats, 9)).addTag("hit delay: " + stats.getStat("attackTracker0", Timer.class).getTimePassed());
         }

         if (result.isCheckFailed()) {
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
