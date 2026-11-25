package me.darksidecode.kantanj.wrapper;

import java.util.concurrent.TimeUnit;

public abstract class ExpiringObject<T> {
   protected T value;
   private final long lifespanMillis;
   private long lastUpdateTime;

   public ExpiringObject(long lifespan, TimeUnit lifespanUnit) {
      if ((this.lifespanMillis = lifespanUnit.toMillis(lifespan)) < 1L) {
         throw new IllegalArgumentException("lifespan cannot be less than 1 millisecond");
      } else {
         this.update();
      }
   }

   public ExpiringObject(T initialValue, long lifespan, TimeUnit lifespanUnit) {
      this.value = initialValue;
      if ((this.lifespanMillis = lifespanUnit.toMillis(lifespan)) < 1L) {
         throw new IllegalArgumentException("lifespan cannot be less than 1 millisecond");
      } else {
         this.lastUpdateTime = System.currentTimeMillis();
      }
   }

   public boolean hasExpired() {
      return System.currentTimeMillis() > this.lastUpdateTime + this.lifespanMillis;
   }

   public T get() {
      if (this.hasExpired()) {
         this.update();
      }

      return this.value;
   }

   public final void update() {
      try {
         this.update0();
      } catch (Exception ex) {
         throw new RuntimeException("failed to update " + this.getClass().getName(), ex);
      } finally {
         this.lastUpdateTime = System.currentTimeMillis();
      }
   }

   protected abstract void update0() throws Exception;
}
