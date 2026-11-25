package rip.reflex.util.protocol.compatibility;

import com.comphenix.protocol.events.PacketContainer;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import rip.reflex.util.protocol.packet.Packets;

public class CompatibilityProvider_1_8 implements CompatibilityProvider {
   private final Map<Byte, BlockFace> blockOrientationConversion = new HashMap<>();

   CompatibilityProvider_1_8() {
      this.blockOrientationConversion.put((byte)0, BlockFace.DOWN);
      this.blockOrientationConversion.put((byte)1, BlockFace.UP);
      this.blockOrientationConversion.put((byte)2, BlockFace.NORTH);
      this.blockOrientationConversion.put((byte)3, BlockFace.SOUTH);
      this.blockOrientationConversion.put((byte)4, BlockFace.WEST);
      this.blockOrientationConversion.put((byte)5, BlockFace.EAST);
   }

   @Override
   public int getLiquidLevel(Block block) {
      this.checkLiquid(block);
      return block.getData();
   }

   @Override
   public boolean isLiquidFlowing(Block block) {
      return this.getLiquidLevel(block) != 0;
   }

   @Override
   public boolean isShulkerBoxOpen(Block block) {
      this.notAvailable("Shulker Box");
      return false;
   }

   @Override
   public BlockFace getBlockOrientation(Block block) {
      byte data = block.getData();
      BlockFace facing = this.blockOrientationConversion.get(data);
      if (facing == null) {
         throw new IllegalArgumentException(String.format("invalid block data: %s (is %s even orientable?)", data, block.getType().name()));
      } else {
         return facing;
      }
   }

   @Override
   public int getSlabType(Block block) {
      return -1;
   }

   @Override
   public int getBisectedBlockHalf(Block block) {
      return -1;
   }

   @Override
   public boolean isItemInHand(Player p, Material itemType) {
      return p.getInventory().getItemInHand().getType() == itemType;
   }

   @Override
   public boolean isBlockInHand(Player p) {
      Material item = p.getInventory().getItemInHand().getType();
      return item != Material.AIR && item.isBlock();
   }

   @Override
   public boolean isBlockPlacePacket(PacketContainer packet) {
      return packet.getType().equals(Packets.IN_BLOCKPLACE);
   }
}
