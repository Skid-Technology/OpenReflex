package rip.reflex.check.player.badpackets;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.MovementCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;

public class ReverseTimerGlideCheck extends MovementCheck {
   private boolean enabled = this.getLocalSetting("midair_lag_dragdown", Boolean.valueOf(true));

   public ReverseTimerGlideCheck() {
      super("BadPackets", "badpcks.rtg", 0, Cheat.valueOf("BadPackets"));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Location from, Location to) {
      if (this.enabled && from.getWorld().equals(to.getWorld())) {
         MethodProfiler mp = new MethodProfiler("HA-218371928");
         if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X)
            && p.getInventory().getChestplate() != null
            && p.getInventory().getChestplate().getType() == Material.ELYTRA) {
            return null;
         }

         double movedHorizontally = Maths.horizontalDistance(from, to);
         Block previous = from.getBlock();
         Block inside = to.getBlock();
         Block beneath = inside.getRelative(BlockFace.DOWN);
         boolean airborne = previous.getType() == Material.AIR && inside.getType() == Material.AIR && beneath.getType() == Material.AIR;
         if (airborne) {
            if (movedHorizontally > 0.228 && stats.hasTimerPassed("rtgOnGroundTracker", 900L) && stats.hasTimerPassed("rtgMoveTracker", 85L)) {
               Location tpTo = from.clone().add(0.0, -1.45, 0.0);
               if (tpTo.getBlock().getType().isSolid()) {
                  tpTo.add(0.0, 0.85, 0.0);
               }

               tpTo.setYaw(to.getYaw());
               tpTo.setPitch(to.getPitch());
               stats.syncTp(tpTo);
            }
         } else {
            stats.resetTimer("rtgOnGroundTracker");
         }

         stats.resetTimer("rtgMoveTracker");
         mp.addToTimings();
      }

      return null;
   }
}
