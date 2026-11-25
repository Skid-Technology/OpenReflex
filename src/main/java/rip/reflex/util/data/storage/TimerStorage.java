package rip.reflex.util.data.storage;

import java.io.Serializable;
import java.util.List;
import rip.reflex.util.data.LimitedList;
import rip.reflex.util.time.Timer;

public class TimerStorage implements Serializable {
   private static final int DEFAULT_SIZE = 5000;
   private final LimitedList<Timer> storage;

   public TimerStorage() {
      this.storage = new LimitedList<>(5000);
   }

   public TimerStorage(int size) {
      this.storage = new LimitedList<>(size);
   }

   public List<Timer> values() {
      return this.storage;
   }

   public Timer first() {
      return this.get(0);
   }

   public Timer get(int index) {
      return index >= 0 && index < this.size() ? this.storage.get(index) : null;
   }

   public long averageTimePassed() {
      return this.isEmpty() ? 0L : this.storage.stream().mapToLong(Timer::getTimePassed).sum() / (long)this.size();
   }

   public long averageTimeTicked() {
      return this.isEmpty() ? 0L : this.storage.stream().mapToLong(Timer::getTimeTicked).sum() / (long)this.size();
   }

   public TimerStorage resetAll() {
      this.storage.forEach(Timer::reset);
      return this;
   }

   public int size() {
      return this.storage.size();
   }

   public boolean isFull() {
      return this.storage.isFull();
   }

   public void add(Timer o) {
      this.storage.add(o);
   }

   public void remove(Timer o) {
      this.storage.remove(o);
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public void clear() {
      this.storage.clear();
   }
}
