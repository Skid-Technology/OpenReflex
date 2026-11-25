package rip.reflex.util.performance;

import rip.reflex.util.performance.timings.ReflexTimings;
import rip.reflex.util.performance.timings.TimingsEntry;
import rip.reflex.util.time.Timer;

public class MethodProfiler {
   private Boolean profile;
   private final String method;
   private final Timer beginTracker;

   public MethodProfiler(String method) {
      this.method = method;
      this.beginTracker = new Timer();
   }

   public void checkExecutionTime() {
   }

   public void addToTimings() {
      ReflexTimings.addEntry(new TimingsEntry(this.method, this.beginTracker.getTimePassed()));
   }
}
