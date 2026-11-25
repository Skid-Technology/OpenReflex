package rip.reflex.check.combat.killaura;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Averanger;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.time.Timer;

public class SwingCheck extends PacketCheck {
   public SwingCheck() {
      super("KillAura", "ka.pod", Cheat.valueOf("KillAura"));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("CT-193591");
         CheckResult result = new CheckResult();
         if (VersionUtils.getClientVersion(p).equals(MCVersion.V1_8_X) && stats.getLagStatus().ordinal() < LagStatus.VIVID_LAG.ordinal()) {
            if (Packets.isAttackPacket(packet)) {
               if (stats.getStat("lookDelayAveranger", Averanger.class) == null) {
                  stats.setStat("lookDelayAveranger", new Averanger(6));
               }

               stats.getStat("lookDelayAveranger", Averanger.class).next(stats.getStat("packetFlushTracker0", Timer.class).getTimePassed());
               if (stats.getStat("lookDelayAveranger", Averanger.class).value() <= 8L
                  && stats.getStat("currentTarget", LivingEntity.class) != null
                  && Maths.distanceExact(p, stats.getStat("currentTarget", LivingEntity.class)) > 2.25) {
                  stats.setStat("lookFrequencyViolations", stats.getStat("lookFrequencyViolations", int.class) + 2);
                  if (stats.getStat("lookFrequencyViolations", int.class) > 18) {
                     result.addTag("packets out of order (A)").setMod(100);
                  }
               } else if (stats.getStat("lookFrequencyViolations", int.class) > 0) {
                  stats.setStat("lookFrequencyViolations", stats.getStat("lookFrequencyViolations", int.class) - 1);
               }

               if (stats.getStat("lastSwingType", PacketType.class) != null && !stats.getStat("lastSwingType", PacketType.class).equals(Packets.IN_SWING)) {
                  result.addTag("packets out of order (B)").setMod(9);
               }
            } else if (Packets.isUpdatePacket(packet)) {
               stats.getStat("packetFlushTracker0", Timer.class).reset();
            }

            stats.setStat("lastSwingType", packet.getType());
            if (this.getLocalSetting("strict_mode", Boolean.valueOf(false))) {
               result.cancel(this.shouldCancel(stats));
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
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
