package rip.reflex.check.combat.killaura;

import com.comphenix.protocol.events.PacketContainer;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.time.Timer;

public class AutoBlockCheck extends PacketCheck {
   public AutoBlockCheck() {
      super("KillAura", "ka.ab", Cheat.valueOf("KillAura"));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      MethodProfiler mp = new MethodProfiler("CT-763916791");
      CheckResult result = new CheckResult();
      if (!stats.hasStat("currentTarget")) {
         return null;
      } else {
         LivingEntity target = stats.getStat("currentTarget", LivingEntity.class);
         if (LocationUtils.checkNearbyChunks(target) && p.getItemInHand() != null && !stats.hasNoStat("attackTracker0")) {
            try {
               if (p.getItemInHand().getType().toString().contains("SWORD")
                  && Packets.isUseItemPacket(packet)
                  && p.getTargetBlock((Set)null, NumberConversions.round(Maths.distance(p.getLocation().clone(), target.getLocation().clone()))).getType()
                     == Material.AIR
                  && !stats.getStat("attackTracker0", Timer.class).hasPassed(850L)) {
                  if (!stats.hasStat("swordBlockTracker")) {
                     stats.setStat("swordBlockTracker", new Timer(System.currentTimeMillis() - 250L));
                  }

                  if (!stats.getStat("swordBlockTracker", Timer.class).hasPassed(50L)) {
                     result.setMod(5).addTag("autoblock");
                  }

                  stats.getStat("swordBlockTracker", Timer.class).reset();
               }

               if (result.isCheckFailed() && this.shouldCancel(stats, 10)) {
                  stats.resetTimer("attackPenaltyTracker");
               }

               mp.addToTimings();
               return result;
            } catch (IllegalStateException ignored) {
               return null;
            }
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
