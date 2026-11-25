package rip.reflex.util.data.storage;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import rip.reflex.util.data.LimitedList;

public class Storage implements Serializable {
   private static final int DEFAULT_SIZE = 5000;
   private final LimitedList<Object> storage;

   public Storage() {
      this.storage = new LimitedList<>(5000);
   }

   public Storage(int size) {
      this.storage = new LimitedList<>(size);
   }

   private Storage(LimitedList<Object> storage) {
      this.storage = storage;
   }

   public List<Object> values() {
      return this.storage;
   }

   public Object first() {
      return this.get(0);
   }

   public Object get(int index) {
      return index >= 0 && index < this.size() ? this.storage.get(index) : null;
   }

   public Object[] asArray() {
      return (Object[])this.storage.toArray().clone();
   }

   public int size() {
      return this.storage.size();
   }

   public boolean isFull() {
      return this.storage.isFull();
   }

   public Storage add(Object o) {
      this.storage.add(o);
      return this;
   }

   public void remove(Object o) {
      this.storage.remove(o);
   }

   public boolean isEmpty() {
      return this.storage.isEmpty();
   }

   public void clear() {
      this.storage.clear();
   }

   public Storage copy() {
      return new Storage((LimitedList<Object>)this.storage.clone());
   }

   public Stream<Object> stream() {
      return this.storage.stream();
   }

   public void forEach(Consumer<? super Object> action) {
      this.storage.forEach(action);
   }
}
