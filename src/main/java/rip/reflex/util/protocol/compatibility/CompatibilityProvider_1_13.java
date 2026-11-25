package rip.reflex.util.protocol.compatibility;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Slab;

public class CompatibilityProvider_1_13 extends CompatibilityProvider_1_12 {
   @Override
   public int getLiquidLevel(Block block) {
      this.checkLiquid(block);
      return ((Levelled)block.getBlockData()).getLevel();
   }

   @Override
   public BlockFace getBlockOrientation(Block block) {
      BlockData bdata = block.getBlockData();
      if (bdata instanceof Directional) {
         return ((Directional)bdata).getFacing();
      } else {
         throw new IllegalArgumentException("block not Directional: " + block.getType());
      }
   }

   @Override
   public int getSlabType(Block block) {
      this.checkType(block, "SLAB", false);
      return ((Slab)block.getBlockData()).getType().ordinal();
   }

   @Override
   public int getBisectedBlockHalf(Block block) {
      BlockData bdata = block.getBlockData();
      if (bdata instanceof Bisected) {
         return ((Bisected)bdata).getHalf().ordinal();
      } else {
         throw new IllegalArgumentException("block not Bisected: " + block.getType());
      }
   }
}
