package rip.reflex.util.data.storage;

import java.util.function.Consumer;
import java.util.stream.Stream;
import rip.reflex.util.data.LimitedList;

public class BitStorage {
   private final LimitedList<Boolean> storage;
   private final Object lock = new Object();

   public BitStorage(int capacity) {
      this.storage = new LimitedList<>(capacity);
   }

   public boolean add(boolean bit) {
      synchronized (this.lock) {
         this.storage.add(bit);
         return this.isFull();
      }
   }

   public boolean get(int index) {
      synchronized (this.lock) {
         return this.storage.get(index);
      }
   }

   public boolean remove(int index) {
      synchronized (this.lock) {
         return this.storage.remove(index);
      }
   }

   public BitStorage clear() {
      synchronized (this.lock) {
         this.storage.clear();
         return this;
      }
   }

   public int count(boolean bit) {
      synchronized (this.lock) {
         return (int)this.storage.stream().filter(b -> b == bit).count();
      }
   }

   public double getPercentage(boolean bit) {
      synchronized (this.lock) {
         return (double)this.count(bit) / (double)this.size() * 100.0;
      }
   }

   public int size() {
      synchronized (this.lock) {
         return this.storage.size();
      }
   }

   public int getCapacity() {
      synchronized (this.lock) {
         return this.storage.getCapacity();
      }
   }

   public boolean isFull() {
      synchronized (this.lock) {
         return this.storage.isFull();
      }
   }

   public Stream<Boolean> stream() {
      synchronized (this.lock) {
         return this.storage.stream();
      }
   }

   public void forEach(Consumer<? super Boolean> action) {
      synchronized (this.lock) {
         this.storage.forEach(action);
      }
   }
}
