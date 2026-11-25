package rip.reflex.util.performance;

import java.util.Timer;
import java.util.TimerTask;
import org.bukkit.Bukkit;
import rip.reflex.Reflex;
import rip.reflex.util.math.Maths;

public class PlayerPacketBasedServerPerformanceTracker implements ServerPerformanceTracker {
   private final Reflex reflex;
   private int optPPTrackFreq;
   private int optNoPPTrigger;
   private long lastTimeSinceLastPP;

   @Override
   public void start() {
      this.optPPTrackFreq = this.reflex.getSetting("general.server_performance_tracking.pp_track_freq", 750);
      if (!Maths.matchIn(this.optPPTrackFreq, 50, 30000)) {
         this.reflex.warn("INVALID CONFIGURATION! PLEASE FIX ALL THE ISSUES LISTED BELOW BEFORE RESTARTING:");
         this.reflex.warn("    --> pp_track_freq: " + this.optPPTrackFreq);
         this.reflex.warn("        ^ This option must be at least 50 and at most 30000");
         this.reflex.warn(" ");
         this.reflex.warn("Reflex will self-disable.");
         Bukkit.getPluginManager().disablePlugin(this.reflex);
      } else {
         this.optNoPPTrigger = this.reflex.getSetting("general.server_performance_tracking.no_pp_trigger", 150);
         if (!Maths.matchIn(this.optNoPPTrigger, 50, 30000)) {
            this.reflex.warn("INVALID CONFIGURATION! PLEASE FIX ALL THE ISSUES LISTED BELOW BEFORE RESTARTING:");
            this.reflex.warn("    --> no_pp_trigger: " + this.optNoPPTrigger);
            this.reflex.warn("        ^ This option must be at least 50 and at most 30000");
            this.reflex.warn(" ");
            this.reflex.warn("Reflex will self-disable.");
            Bukkit.getPluginManager().disablePlugin(this.reflex);
         } else {
            TimerTask task = new TimerTask() {
               @Override
               public void run() {
                  long timeSinceLastPP = PlayerPacketBasedServerPerformanceTracker.this.reflex.getGeneralPlayerPacketReceiveTracker().getTimePassed();
                  PlayerPacketBasedServerPerformanceTracker.this.lastTimeSinceLastPP = timeSinceLastPP;
                  if (Bukkit.getOnlinePlayers().size() > 1 && timeSinceLastPP > (long)PlayerPacketBasedServerPerformanceTracker.this.optNoPPTrigger) {
                     PlayerPacketBasedServerPerformanceTracker.this.handleSingleLag();
                  }
               }
            };
            new Timer().scheduleAtFixedRate(task, (long)this.optPPTrackFreq, (long)this.optPPTrackFreq);
            this.reflex
               .info(
                  "Started player-packet-based server performance tracker. Running every "
                     + this.optPPTrackFreq
                     + " ms, triggering after "
                     + this.optNoPPTrigger
                     + " ms outages."
               );
         }
      }
   }

   @Override
   public void handleSingleLag() {
      this.reflex.getSingleServerLagTracker().reset();
      if (this.reflex.getSingleServerLagTracker().hasPassed(10000L)) {
         System.err.println("Reflex | WARN: =======================================================");
         System.err.println("Reflex | WARN:   THE SERVER COULD BE LAGGING!");
         System.err.println("Reflex | WARN:       There have been no packets from any online players");
         System.err.println("Reflex | WARN:       for over " + this.lastTimeSinceLastPP + " ms now. Looks bad :(");
         System.err.println("Reflex | WARN:   Some Reflex's components have been disabled temporarily.");
         System.err.println("Reflex | WARN: =======================================================");
      }
   }

   public PlayerPacketBasedServerPerformanceTracker(Reflex reflex) {
      this.reflex = reflex;
   }
}
