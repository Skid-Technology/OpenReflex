package rip.reflex.util.performance;

public interface ServerPerformanceTracker {
   void start();

   default void handleSingleLag() {
   }

   default void startedLagging() {
   }

   default void endedLagging() {
   }
}
