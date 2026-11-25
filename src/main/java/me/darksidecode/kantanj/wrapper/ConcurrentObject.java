package me.darksidecode.kantanj.wrapper;

import me.darksidecode.kantanj.types.Check;

public class ConcurrentObject<T> {
   private final Object lock;
   private T value;

   public ConcurrentObject() {
      this(new Object(), null);
   }

   public ConcurrentObject(T initialValue) {
      this(new Object(), initialValue);
   }

   public ConcurrentObject(Object lock, T initialValue) {
      this.lock = Check.notNull(lock, "lock cannot be null");
      this.value = initialValue;
   }

   public void set(T newValue) {
      synchronized (this.lock) {
         this.value = newValue;
      }
   }

   public T get() {
      synchronized (this.lock) {
         return this.value;
      }
   }

   public T getAndSwap(T newValue) {
      synchronized (this.lock) {
         T oldValue = this.value;
         this.value = newValue;
         return oldValue;
      }
   }

   public Object getLock() {
      return this.lock;
   }
}
