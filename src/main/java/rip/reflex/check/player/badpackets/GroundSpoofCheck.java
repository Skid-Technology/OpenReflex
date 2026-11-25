package rip.reflex.check.player.badpackets;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class GroundSpoofCheck extends PacketCheck {
   public GroundSpoofCheck() {
      super("BadPackets", "badpcks.grd", Cheat.valueOf("BadPackets"));
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      MethodProfiler mp = new MethodProfiler("RE-8132980");
      CheckResult result = new CheckResult();
      if (Packets.isUpdatePacket(packet)) {
         boolean clientOnGround = Packets.readBoolean(packet, 0);
         if (clientOnGround && stats.hasTimerPassed("grdCheckTracker", 300L)) {
            Location loc = p.getLocation().clone();
            Block blockInside = loc.getBlock();
            Block blockBeneath = blockInside.getRelative(BlockFace.DOWN);
            Block farBelow = blockBeneath.getRelative(BlockFace.DOWN);
            boolean actuallyMidair = blockInside.getType() == Material.AIR
               && blockBeneath.getType() == Material.AIR
               && farBelow.getType() == Material.AIR
               && !LocationUtils.isInLiquid(p);
            if (actuallyMidair) {
               actuallyMidair = LocationUtils.isPushable(loc);
               if (actuallyMidair) {
                  actuallyMidair = LocationUtils.isPushable(blockBeneath.getLocation());
               }
            }

            if (actuallyMidair) {
               if (!stats.hasTimerPassed("grdVlTracker", 700L)) {
                  result.setMod(1).addTag("tried to spoof their ground position").cancel(this.isNotSilent());
                  if (result.shouldCancel()) {
                     Location tpTo = loc.add(0.0, 0.74, 0.0);
                     double fallDistEstimate = stats.getDouble("fallDistEstimate");
                     double damage = Math.max(2.0, fallDistEstimate);
                     result.addTag("estimated fall distance: " + fallDistEstimate);
                     p.sendMessage("§bDAMAGE :: §e" + damage);
                     Bukkit.getScheduler().runTask(this.reflex, () -> {
                        p.damage(damage);
                        p.teleport(tpTo);
                     });
                  }
               }

               stats.resetTimer("grdVlTracker");
            }

            stats.resetTimer("grdCheckTracker");
         }
      }

      mp.addToTimings();
      return result;
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
