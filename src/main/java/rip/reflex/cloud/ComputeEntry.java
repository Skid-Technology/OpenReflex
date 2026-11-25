package rip.reflex.cloud;

import org.bukkit.entity.Player;
import rip.reflex.util.machinelearning.Dataset;
import rip.reflex.util.time.Timer;

public class ComputeEntry {
   private final Player player;
   private final Timer queryTracker;
   private final int debugML;
   private final Dataset heurSnapshot;

   public ComputeEntry(Player player, int debugML, Dataset heurSnapshot) {
      this.player = player;
      this.debugML = debugML;
      this.heurSnapshot = heurSnapshot;
      this.queryTracker = new Timer();
   }

   public Player getPlayer() {
      return this.player;
   }

   public Timer getQueryTracker() {
      return this.queryTracker;
   }

   public int getDebugML() {
      return this.debugML;
   }

   public Dataset getHeurSnapshot() {
      return this.heurSnapshot;
   }
}
