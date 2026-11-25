package rip.reflex.util.data.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import rip.reflex.util.data.LimitedList;
import rip.reflex.util.math.Maths;
import rip.reflex.util.serialization.ManualSerializable;

public class NumberStorage implements ManualSerializable, Serializable {
   private final Object lock = new Object();
   private LimitedList<Number> storage;

   private NumberStorage() {
   }

   public NumberStorage(int capacity) {
      this.storage = new LimitedList<>(capacity);
   }

   public static NumberStorage deserialize(List<? extends Serializable> list) {
      Integer cap = (Integer)list.remove(0);
      LimitedList<Number> stor = new LimitedList<>(cap);
      list.forEach(nx -> stor.add((Number)nx));
      list.clear();
      NumberStorage n = new NumberStorage();
      n.setStorage(stor);
      return n;
   }

   public static NumberStorage make(double[] arr) {
      NumberStorage storage = new NumberStorage(arr.length);

      for (double n : arr) {
         storage.add(n);
      }

      return storage;
   }

   public static NumberStorage make(List<Double> list) {
      NumberStorage storage = new NumberStorage(list.size());

      for (double n : list) {
         storage.add(n);
      }

      return storage;
   }

   @Override
   public List<Serializable> serialize() {
      synchronized (this.lock) {
         ArrayList<Serializable> list = new ArrayList<>();
         list.add(this.storage.getCapacity());
         list.addAll(this.storage);
         return list;
      }
   }

   public boolean contains(Number n) {
      synchronized (this.lock) {
         return this.storage.contains(n);
      }
   }

   public List<Number> values() {
      synchronized (this.lock) {
         return this.storage;
      }
   }

   public Number[] asArray() {
      synchronized (this.lock) {
         Number[] array = (Number[])new Number[this.storage.size()].clone();

         for (int i = 0; i < array.length; i++) {
            array[i] = this.storage.get(i);
         }

         return array;
      }
   }

   public double[] asDoubleArray() {
      synchronized (this.lock) {
         double[] array = (double[])new double[this.storage.size()].clone();

         for (int i = 0; i < array.length; i++) {
            if (this.storage.get(i) == null) {
               this.storage.set(i, Integer.valueOf(0));
            }

            array[i] = (Double)this.storage.get(i);
         }

         return array;
      }
   }

   public int size() {
      synchronized (this.lock) {
         return this.storage.size();
      }
   }

   public boolean isFull() {
      synchronized (this.lock) {
         return this.storage.isFull();
      }
   }

   public double sum() {
      synchronized (this.lock) {
         return this.storage.stream().mapToDouble(Number::doubleValue).sum();
      }
   }

   public double average() {
      synchronized (this.lock) {
         return this.storage.stream().mapToDouble(Number::doubleValue).sum() / (double)this.size();
      }
   }

   public double averageNotZero(double alt) {
      synchronized (this.lock) {
         return this.averageNot(0.0, alt);
      }
   }

   public double averageNot(double not, double alt) {
      synchronized (this.lock) {
         return Maths.not(this.average(), not, alt);
      }
   }

   public double max() {
      synchronized (this.lock) {
         return Maths.max(this.asDoubleArray());
      }
   }

   public double min() {
      synchronized (this.lock) {
         return Maths.min(this.asDoubleArray());
      }
   }

   public NumberStorage add(Number number) {
      synchronized (this.lock) {
         if (number == null) {
            return this;
         } else {
            this.storage.add(number);
            return this;
         }
      }
   }

   public NumberStorage addOrClear(Number number) {
      synchronized (this.lock) {
         return this.isFull() ? this.clear() : this.add(number);
      }
   }

   public boolean addChecking(Number number) {
      synchronized (this.lock) {
         this.add(number);
         return this.isFull();
      }
   }

   public NumberStorage addIf(Number number, boolean b) {
      synchronized (this.lock) {
         if (b && number != null) {
            this.storage.add(number);
            return this;
         } else {
            return this;
         }
      }
   }

   public boolean addCheckingIf(Number number, boolean b) {
      synchronized (this.lock) {
         if (!b) {
            return false;
         } else {
            this.add(number);
            return this.isFull();
         }
      }
   }

   public NumberStorage remove(Number number) {
      synchronized (this.lock) {
         this.storage.remove(number);
         return this;
      }
   }

   public NumberStorage remove(int index) {
      synchronized (this.lock) {
         this.storage.remove(index);
         return this;
      }
   }

   public NumberStorage removeLast() {
      synchronized (this.lock) {
         int size = this.storage.size();
         return size == 0 ? this : this.remove(size - 1);
      }
   }

   public Number get(int index) {
      synchronized (this.lock) {
         return index >= 0 && index < this.size() ? this.storage.get(index) : null;
      }
   }

   public Integer getInteger(int index) {
      synchronized (this.lock) {
         Number n = this.get(index);
         if (n == null) {
            return null;
         } else if (n instanceof Integer) {
            return (Integer)n;
         } else if (n instanceof Long) {
            return Math.toIntExact((Long)n);
         } else if (n instanceof Short) {
            return Integer.valueOf((Short)n);
         } else if (n instanceof Double) {
            return Maths.floor((Double)n);
         } else if (n instanceof Float) {
            return Maths.floor((Float)n);
         } else {
            return n instanceof Byte ? Integer.valueOf((Byte)n) : null;
         }
      }
   }

   public Long getLong(int index) {
      synchronized (this.lock) {
         Number n = this.get(index);
         if (n == null) {
            return null;
         } else if (n instanceof Integer) {
            return (long)((Integer)n).intValue();
         } else if (n instanceof Long) {
            return (Long)n;
         } else if (n instanceof Short) {
            return (long)((Short)n).shortValue();
         } else if (n instanceof Double) {
            return Maths.floor64((Double)n);
         } else if (n instanceof Float) {
            return Maths.floor64((Float)n);
         } else {
            return n instanceof Byte ? (long)((Byte)n).byteValue() : null;
         }
      }
   }

   public NumberStorage clear() {
      synchronized (this.lock) {
         this.storage.clear();
         return this;
      }
   }

   public int getCapacity() {
      synchronized (this.lock) {
         return this.storage.getCapacity();
      }
   }

   public NumberStorage copy() {
      synchronized (this.lock) {
         NumberStorage dest = new NumberStorage(this.getCapacity());

         for (Number n : (LimitedList<Number>)this.storage.clone()) {
            dest.add(n);
         }

         return dest;
      }
   }

   public Stream<Number> stream() {
      synchronized (this.lock) {
         return this.storage.stream();
      }
   }

   public void forEach(Consumer<? super Number> action) {
      synchronized (this.lock) {
         this.storage.forEach(action);
      }
   }

   @Override
   public String toString() {
      synchronized (this.lock) {
         return this.storage.toString() + "(" + this.storage.size() + "/" + this.storage.getCapacity() + ")";
      }
   }

   public String toString(int placesAfterDot) {
      if (placesAfterDot < 0) {
         throw new IllegalArgumentException("illegal rounding: " + placesAfterDot + " places after dot");
      } else {
         synchronized (this.lock) {
            if (this.storage.isEmpty()) {
               return "[](0/" + this.storage.getCapacity() + ")";
            } else {
               StringBuilder sb = new StringBuilder();
               sb.append('[');
               Number first = this.storage.get(0);
               sb.append(round(first, placesAfterDot));
               if (this.storage.size() > 1) {
                  for (int i = 1; i < this.storage.size(); i++) {
                     sb.append(", ").append(round(this.storage.get(i), placesAfterDot));
                  }
               }

               sb.append(']');
               return sb.toString() + "(" + this.storage.size() + "/" + this.storage.getCapacity() + ")";
            }
         }
      }
   }

   private static String round(Number n, int placesAfterDot) {
      return String.format("%." + placesAfterDot + "f", n);
   }

   private void setStorage(LimitedList<Number> storage) {
      this.storage = storage;
   }
}
