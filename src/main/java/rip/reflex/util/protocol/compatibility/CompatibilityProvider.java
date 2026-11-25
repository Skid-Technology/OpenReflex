package rip.reflex.util.protocol.compatibility;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import rip.reflex.util.protocol.VersionUtils;

public interface CompatibilityProvider {
   int getLiquidLevel(Block block);

   boolean isLiquidFlowing(Block block);

   boolean isShulkerBoxOpen(Block block);

   BlockFace getBlockOrientation(Block block);

   int getSlabType(Block block);

   int getBisectedBlockHalf(Block block);

   boolean isItemInHand(Player player, Material material);

   boolean isBlockInHand(Player player);

   boolean isBlockPlacePacket(PacketContainer packet);

   default void checkLiquid(Block block) {
      if (!block.isLiquid()) {
         throw new IllegalArgumentException("block not liquid: " + block.getType());
      }
   }

   default void checkType(Block block, String required, boolean strict) {
      String type = block.getType().name();
      if (strict && !type.equals(required) || !type.contains(required)) {
         throw new IllegalArgumentException(String.format("illegal block type: expected %s, got %s", required, type));
      }
   }

   default void notAvailable(String what) {
      throw new IllegalStateException(String.format("%s is not available in %s", what, VersionUtils.getServerVersion().fancyName()));
   }
}
