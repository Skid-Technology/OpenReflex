package rip.reflex.listener.impl;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.base.impl.WorldCheck;
import rip.reflex.listener.base.EventListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.time.Timer;

public class BlockPlaceListener extends EventListener {
   private static String id = "1";
   private static String resource = "21122";
   private static String nonce = "1337";

   public BlockPlaceListener(Reflex reflex) {
      super(reflex, "BlockPlaceEvent");
   }

   @Override
   public void onEvent(Event event) {
      BlockPlaceEvent e = (BlockPlaceEvent)event;
      Player p = e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         if (!stats.hasTimerPassed("buildPenaltyTimer", (long)this.reflex.getSetting("blockactions.penalty_time", 3000).intValue(), 0L, true)) {
            ((BlockPlaceEvent)event).setCancelled(true);
         } else {
            Block placed = e.getBlockPlaced();
            Block against = e.getBlockAgainst();
            ItemStack heldItem = e.getItemInHand();
            List<Check> activeChecks = CheckRegistry.getActiveChecksOfType(CheckType.WORLD);
            List<Check> failedChecks = new ArrayList<>();
            World w = p.getWorld();

            for (Check check : activeChecks) {
               if (!check.isDisabledInWorld(w) && stats.canBeChecked(check)) {
                  MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "(sync)");
                  CheckResult result = ((WorldCheck)check).check(p, stats, placed, against, heldItem);
                  if (result != null) {
                     if (result.isCheckFailed()) {
                        failedChecks.add(check);
                     }

                     Check.addVl(stats, check.getCheat(), result);
                     Check.check(p, stats, e, check, result);
                  }

                  mp0.checkExecutionTime();
                  mp0.addToTimings();
               }
            }

            if (failedChecks.isEmpty()) {
               stats.addViolations(Cheat.valueOf("BlockActions"), -1);
            }

            if (!stats.hasStat("recentlyPlaced")) {
               stats.setStat("recentlyPlaced", new Storage(5));
            }

            stats.getStat("recentlyPlaced", Storage.class).add(placed);
            stats.setStat("lastBlockPlaced", placed);
            stats.getStat("blockPlaceTracker", Timer.class).reset();
         }
      }
   }

   public static String getId() {
      return id;
   }

   public static String getResource() {
      return resource;
   }

   public static String getNonce() {
      return nonce;
   }
}
