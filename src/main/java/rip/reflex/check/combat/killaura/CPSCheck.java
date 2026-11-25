package rip.reflex.check.combat.killaura;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.time.Timer;

public class CPSCheck extends PacketCheck {
   private int optMaxCps;
   private int optMinClickVariance;
   private int optMinClickDelay;

   public CPSCheck() {
      super("KillAura", "ka.clk", Cheat.valueOf("KillAura"));
      this.setLagSensetive(false);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMaxCps = this.getAdvancedLocalSetting("max_cps", Integer.valueOf(18));
      this.optMinClickVariance = this.getAdvancedLocalSetting("min_click_variance", Integer.valueOf(2));
      this.optMinClickDelay = this.getAdvancedLocalSetting("min_click_delay", Integer.valueOf(25));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      MethodProfiler mp = new MethodProfiler("CT-1863915");
      if (!stats.hasStat("currentTarget")) {
         return null;
      } else {
         LivingEntity target = stats.getStat("currentTarget", LivingEntity.class);
         double dist = Maths.distance(p.getLocation(), target.getLocation());
         if (dist < 1.75) {
            return null;
         } else {
            boolean isAttack = Packets.isAttackPacket(packet);
            boolean isUpdate = Packets.isUpdatePacket(packet);
            CheckResult result = new CheckResult();
            stats.setStatIf("cps_clickedtick_last", () -> stats.getStat("cps_clickedtick", boolean.class), isUpdate);
            stats.setStatIf("cps_clickedtick", () -> false, isUpdate);
            stats.setStatIf("cps_clickedtick", () -> true, isAttack);
            int cps = stats.getStat("cps_tickedcps", int.class);
            if (stats.getStat("hasSecondTicked", boolean.class)) {
               if (cps > this.optMaxCps) {
                  if (this.shouldCancel(stats, 10)) {
                     stats.resetTimer("attackPenaltyTracker");
                  }

                  result.setMod(cps).addTag("cps: " + cps);
               }

               if (cps == stats.getStat("cps_lastCps", int.class) && cps > 9) {
                  stats.setStat("cps_consistencyTime", stats.getStat("cps_consistencyTime", int.class) + 1);
                  result.setMod(4).addTag("cps: ~" + cps).cancel(true);
               } else {
                  stats.setStat("cps_consistencyTime", 0);
               }

               if (cps > 9) {
                  stats.setStat("cps_lastCps", cps);
               }

               stats.setStat("cps_tickedcps", 0);
            }

            if (isAttack) {
               NumberStorage expDelays = stats.getOrCreateNumberStorage("expClickDelays", 2).add(stats.getTimerPassedAndReset("expClickTracker"));
               int expCps = Maths.floor(1000.0 / expDelays.averageNotZero(1000.0));
               int dif = Math.abs(expCps - stats.getInt("lastExpCps"));
               if (expCps > this.getLocalSetting("min_exp_cps", Integer.valueOf(8)) && dif < this.optMinClickVariance && LocationUtils.isPushable(target)) {
                  stats.addNumber("expConstCpsVl", 2);
                  if (stats.getInt("expConstCpsVl") > this.getLocalSetting("max_exp_cps_vl", Integer.valueOf(10))) {
                     if (this.shouldCancel(stats, 10)) {
                        stats.resetTimer("attackPenaltyTracker");
                     }

                     result.setMod(7).addTag("not clicking naturally").addTag("e: " + expCps).addTag("d: " + dif);
                     stats.setStat("expConstCpsVl", 0);
                  }
               } else {
                  stats.subtractNumberTilPositive("expConstCpsVl", 1);
               }

               stats.setStat("lastExpCps", expCps);
               if (!stats.hasStat("cps_clickTracker")) {
                  stats.setStat("cps_clickTracker", new Timer().subtractSeconds(1L));
               }

               long delay = stats.getStat("cps_clickTracker", Timer.class).getTimePassed();
               if (delay < (long)this.optMinClickDelay
                  && stats.getLagStatus().ordinal() < LagStatus.VIVID_LAG.ordinal()
                  && cps > 1
                  && stats.getStat("cps_lastCps", int.class) > 1) {
                  if (this.shouldCancel(stats, 10)) {
                     stats.resetTimer("attackPenaltyTracker");
                  }

                  result.setMod(3).addTag("double click").addTag("delay: " + delay + " ms");
               }
            }

            if (isAttack) {
               stats.getStat("cps_clickTracker", Timer.class).reset();
            }

            stats.setStatIf("cps_tickedcps", () -> stats.getStat("cps_tickedcps", int.class) + 1, isAttack);
            if (isAttack
               && this.getLocalSetting("fair_delay", Boolean.valueOf(true))
               && stats.getStat("cps_clickedtick", boolean.class)
               && stats.getStat("cps_clickedtick_last", boolean.class)) {
               return result.cancel(true);
            } else {
               stats.setStatIf("cps_lastclicktick", () -> stats.getStat("totalTickCounter", int.class), isAttack);
               if (result.isCheckFailed()) {
                  stats.setStat("combinedFailed", stats.getStat("combinedFailed", int.class) + 1);
               }

               mp.addToTimings();
               return result;
            }
         }
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
