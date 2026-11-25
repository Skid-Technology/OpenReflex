package rip.reflex.util.performance;

import me.flo.flogger.types.FlogLevel;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.math.Maths;
import rip.reflex.util.time.Timer;

public class TPSMeter {
   private final NumberStorage tickDelays = new NumberStorage(100);
   private final Timer tickTracker = new Timer();
   private final Reflex reflex;
   private double tps = 20.0;
   private double previousTps = 20.0;
   private double lowestTps = 20.0;

   public TPSMeter(Reflex reflex) {
      this.reflex = reflex;
   }

   public double getAvgTps2() {
      return this.tps;
   }

   private void onTpsDrop() {
      this.reflex
         .log(FlogLevel.DEBUG, "(TPS Meter) Lag spike detected (" + Maths.toRoundedString(this.previousTps) + "->" + Maths.toRoundedString(this.tps) + ")");

      for (Check check : CheckRegistry.loadedChecksList()) {
         check.disable();
      }
   }

   private void onTpsStabilize() {
      this.reflex.log(FlogLevel.DEBUG, "(TPS Meter) TPS has stabilized (" + Maths.toRoundedString(this.tps) + ")");

      for (Check check : CheckRegistry.loadedChecksList()) {
         check.enable();
      }
   }

   public double getTps() {
      return this.tps;
   }

   public double getPreviousTps() {
      return this.previousTps;
   }

   public double getLowestTps() {
      return this.lowestTps;
   }

   public void setLowestTps(double lowestTps) {
      this.lowestTps = lowestTps;
   }
}
