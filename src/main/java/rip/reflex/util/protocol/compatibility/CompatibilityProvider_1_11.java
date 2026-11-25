package rip.reflex.util.protocol.compatibility;

import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;

public class CompatibilityProvider_1_11 extends CompatibilityProvider_1_10 {
   @Override
   public boolean isShulkerBoxOpen(Block block) {
      this.checkType(block, "SHULKER_BOX", false);
      return ((ShulkerBox)block.getState()).getInventory().getViewers().size() > 0;
   }
}
