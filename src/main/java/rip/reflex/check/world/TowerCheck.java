package rip.reflex.check.world;

import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.impl.WorldCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.performance.MethodProfiler;

public class TowerCheck extends WorldCheck {
   private double optMinTw;

   public TowerCheck() {
      super("BlockActions", "blckact.tw", Cheat.valueOf("BlockActions"));
      this.setSync(true);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optMinTw = this.getAdvancedLocalSetting("min_tw", Double.valueOf(7.5));
   }

   @Override
   public CheckResult check(Player p, Stats stats, Block placed, Block against, ItemStack heldItem) {
      return null;
   }

   @Override
   public CheckResult packetBlockPlace(
      Player p,
      Stats stats,
      Location pLoc,
      BlockPosition pos,
      ItemStack heldItem,
      Material type,
      int ticksSincePlace,
      int missesSincePlace,
      Storage recentPlaced
   ) {
      if (this.isServerLagging()) {
         return null;
      } else {
         MethodProfiler mp = new MethodProfiler("YR-2918381492");
         if (!LocationUtils.isTowering(recentPlaced)) {
            return null;
         } else {
            for (PotionEffect effect : p.getActivePotionEffects()) {
               if (effect.getType().equals(PotionEffectType.JUMP) && effect.getAmplifier() > 0) {
                  return null;
               }
            }

            CheckResult result = new CheckResult();
            NumberStorage delays = stats.getOrCreateNumberStorage("twDelays", 5);
            if (delays.addChecking(ticksSincePlace)) {
               double avgDelay = delays.average();
               if (avgDelay < this.optMinTw) {
                  result.setMod(12).addTag("t: " + avgDelay).cancel(this.isNotSilent());
               }

               delays.clear();
            }

            mp.addToTimings();
            return result;
         }
      }
   }
}
