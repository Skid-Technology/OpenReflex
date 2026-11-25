package rip.reflex.util.protocol.compatibility;

import org.bukkit.block.Block;

public class BlockInfo {
   private final Block block;
   private final BlockHeight height;

   public BlockInfo(Block block) {
      this.block = block;
      this.height = new BlockHeight(block);
   }

   public Block getBlock() {
      return this.block;
   }

   public BlockHeight getHeight() {
      return this.height;
   }
}
