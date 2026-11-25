package me.darksidecode.easysockets.util;

import java.io.Serializable;
import java.util.ArrayList;

public class LimitedList<T> extends ArrayList<T> implements Serializable {
   private static final long serialVersionUID = -3776468374308090201L;
   private final int capacity;

   public LimitedList(int capacity) {
      if (capacity <= 50000 && capacity >= 2) {
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
