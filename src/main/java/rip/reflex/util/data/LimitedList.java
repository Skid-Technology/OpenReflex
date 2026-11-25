package rip.reflex.util.data;

import java.io.Serializable;
import java.util.ArrayList;

public class LimitedList<T> extends ArrayList<T> implements Serializable {
   private final int capacity;

   public LimitedList(int capacity) {
      if (capacity <= 10000 && capacity >= 0) {
         this.capacity = capacity;
      } else {
         throw new IllegalArgumentException("Invalid capacity: " + capacity);
      }
   }

   @Override
   public boolean add(T t) {
      boolean r = super.add(t);
      if (this.size() > this.capacity) {
         this.remove(0);
      }

      return r;
   }

   public boolean isFull() {
      return this.size() == this.capacity;
   }

   public int getCapacity() {
      return this.capacity;
   }
}
