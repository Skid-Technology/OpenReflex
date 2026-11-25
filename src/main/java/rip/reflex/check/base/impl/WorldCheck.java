package rip.reflex.check.base.impl;

import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckType;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.Storage;

public abstract class WorldCheck extends Check {
   public WorldCheck(String name, String comp, Cheat cheat) {
      super(name, comp, CheckType.WORLD, cheat);
   }

   public abstract CheckResult check(Player player, Stats stats, Block placed, Block against, ItemStack heldItem);

   public abstract CheckResult packetBlockPlace(
      Player player, Stats stats, Location playerLoc, BlockPosition pos, ItemStack heldItem, Material type, int ticksSincePlace, int missesSincePlace, Storage recentPlaced
   );
}
