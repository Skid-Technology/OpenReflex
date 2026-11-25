package rip.reflex.check.combat.killaura;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.CombatCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;

public class CombinedCheck extends CombatCheck {
   private int optThreshold;
   private int optPeriod;

   public CombinedCheck() {
      super("KillAura", "ka.cbd", false, Cheat.valueOf("KillAura"));
      final CombinedCheck check = this;
      this.runTimer(new ReflexRunnable("AR-CT-21919574") {
         @Override
         public void execute() throws Exception {
            MethodProfiler mp = new MethodProfiler("CT-21919574");
            if (Maths.objects[0] != null && Maths.objects[1] != null && Maths.objects[1].toString().endsWith(Maths.objects[0].toString().replace(".0", ""))) {
               for (Player p : Bukkit.getOnlinePlayers()) {
                  World w = p.getWorld();
                  Stats stats = Stats.of(p);
                  if (!check.isDisabledInWorld(w) && stats.canBeChecked(check)) {
                     int f = stats.getInt("combinedFailed");
                     if (CombinedCheck.this.getLocalSetting("strict_mode", Boolean.valueOf(false))) {
                        stats.setStat("combinedFailed", f + 3);
                     }

                     if (f >= CombinedCheck.this.optThreshold) {
                        int vl = Maths.floor(2.5 * (double)f);
                        CheckResult result = new CheckResult().setMod(vl).addTag("failed " + f + " analyses");
                        Check.addVl(stats, CombinedCheck.this.getCheat(), result);
                        Check.check(p, stats, null, check, result);
                     }

                     stats.setStat("combinedFailed", 0);
                  }
               }
            }

            mp.addToTimings();
         }
      }, this.optPeriod, true);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optThreshold = this.getAdvancedLocalSetting("threshold", Integer.valueOf(35));
      this.optPeriod = this.getAdvancedLocalSetting("period", Integer.valueOf(600));
   }

   @Override
   public CheckResult check(Player p, Stats stats, LivingEntity target) {
      return null;
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      return null;
   }
}
