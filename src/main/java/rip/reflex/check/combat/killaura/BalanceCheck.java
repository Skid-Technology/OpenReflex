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

public class BalanceCheck extends CombatCheck {
   public BalanceCheck() {
      super("KillAura", "ka.blc", true, Cheat.valueOf("KillAura"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      MethodProfiler mp = new MethodProfiler("CT-927421074");
      CheckResult result = new CheckResult();
      double yaw = Maths.angle((double)from.getYaw(), (double)to.getYaw());
      double pitch = Maths.angle((double)from.getPitch(), (double)to.getPitch());
      NumberStorage yaws;
      NumberStorage pitches;
      if ((yaws = stats.addInCreatedNumberStorage("balYaws", 80, yaw)).isFull()
         && (pitches = stats.addInCreatedNumberStorage("balPitches", 80, pitch)).isFull()
         && !this.reflex.offlineMode) {
         Statistics ys = Maths.describe(yaws);
         Statistics ps = Maths.describe(pitches);
         double balY = ys.getBalance();
         double balP = ps.getBalance();
         double mY = ys.getMean();
         double mP = ps.getMean();
         double hvarY = Math.abs(ys.getVariance() - ys.getHighest());
         double hvarP = Math.abs(ps.getVariance() - ps.getHighest());
         double rY = ys.getBalance() / ys.getMedian();
         double rP = ps.getBalance() / ps.getMedian();
         if (balY > 1.2
            && balP > 1.2
            && mY > 1.25
            && mP > 1.25
            && !Maths.matchIn(hvarY, 7.5, 15.0)
            && !Maths.matchIn(hvarP, 7.5, 15.0)
            && Math.abs(hvarY - hvarP) > 0.98
            && !Maths.checknum(rY)
            && !Maths.checknum(rP)) {
            result.setMod(100)
               .addTag("improperly balanced head movement")
               .addTag("by: " + balY)
               .addTag("bp: " + balP)
               .addTag("my: " + mY)
               .addTag("mp: " + mP)
               .addTag("hvy: " + hvarY)
               .addTag("hvp: " + hvarP);
         }

         yaws.clear();
         pitches.clear();
      }

      mp.addToTimings();
      return result;
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      return null;
   }
}
