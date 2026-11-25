package rip.reflex.util.math;

import java.util.ArrayList;

public class Counter {
   private final ArrayList<Long> list = new ArrayList<>();
   private final long maxDelay;

   public int update() {
      this.list.add(System.currentTimeMillis());

      for (long l : (ArrayList<Long>)this.list.clone()) {
         if (Math.abs(System.currentTimeMillis() - l) > this.maxDelay) {
            this.list.remove(l);
         }
      }

      return this.list.size();
   }

   public Counter reset() {
      this.list.clear();
      return this;
   }

   public Counter(long maxDelay) {
      this.maxDelay = maxDelay;
   }
}
