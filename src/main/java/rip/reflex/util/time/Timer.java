package rip.reflex.util.time;

import java.io.Serializable;

public class Timer implements Serializable {
   private static final int integrityDataFingerprint_a = -578162988;
   private static final int integrityDataArrInfo_a = 128319532;
   private long startTime;

   public Timer(long startTime) {
      this.startTime = startTime;
   }

   public Timer() {
      this(System.currentTimeMillis());
   }

   public Timer add(long millis) {
      this.startTime = this.startTime + Math.abs(millis);
      return this;
   }

   public Timer addTicks(long ticks) {
      this.startTime = this.startTime + Math.abs(ticks * 50L);
      return this;
   }

   public Timer addSeconds(long seconds) {
      this.startTime = this.startTime + Math.abs(seconds * 1000L);
      return this;
   }

   public Timer subtract(long millis) {
      this.startTime = this.startTime - Math.abs(millis);
      return this;
   }

   public Timer subtractTicks(long ticks) {
      this.startTime = this.startTime - Math.abs(ticks) * 50L;
      return this;
   }

   public Timer subtractSeconds(long seconds) {
      this.startTime = this.startTime - Math.abs(seconds * 1000L);
      return this;
   }

   public Timer offset(long millis) {
      this.startTime += millis;
      return this;
   }

   public Timer offsetTicks(long ticks) {
      this.startTime += ticks * 50L;
      return this;
   }

   public Timer offsetSeconds(long seconds) {
      this.startTime += seconds * 1000L;
      return this;
   }

   public boolean hasPassed(long millis) {
      return this.getTimePassed() >= millis;
   }

   public boolean hasTicked(long ticks) {
      return this.getTimeTicked() >= ticks;
   }

   public long getTimePassed() {
      return System.currentTimeMillis() - this.startTime;
   }

   public long getTimeTicked() {
      return this.getTimePassed() / 50L;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public Timer setStartTime(long newTime) {
      if (newTime < 1L) {
         this.reset();
      } else {
         this.startTime = newTime;
      }

      return this;
   }

   public Timer reset() {
      this.setStartTime(System.currentTimeMillis());
      return this;
   }
}
