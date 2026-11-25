package rip.reflex.util.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class UniqueRandom extends Random {
   private static final List<UniqueRandom> list = new ArrayList<>();
   private final ConcurrentHashMap<Class<?>, List<Object>> cache = new ConcurrentHashMap<>();

   public UniqueRandom() {
      this(System.currentTimeMillis());
   }

   public UniqueRandom(long seed) {
      super(seed);
      list.add(this);
   }

   public byte[] randomByteStream(int length) {
      byte[] stream = new byte[length];

      for (int i = 0; i < stream.length; i++) {
         stream[i] = (byte)(this.nextBoolean() ? this.nextInt(128) : -this.nextInt(-129));
      }

      try {
         if (this.contains(stream)) {
            return this.randomByteStream(length);
         }
      } catch (StackOverflowError ignored) {
         throw new OutOfMemoryError("This UniqueRandom object is out! Create a new one");
      }

      return (byte[])this.add(stream);
   }

   @Override
   public int nextInt(int bound) {
      int i = super.nextInt(bound);

      try {
         if (this.contains(i)) {
            return this.nextInt(bound);
         }
      } catch (StackOverflowError ignoredEx) {
         throw new OutOfMemoryError("This UniqueRandom object is out! Create a new one");
      }

      return (Integer)this.add(i);
   }

   @Override
   public int nextInt() {
      int i = super.nextInt();

      try {
         if (this.contains(i)) {
            return this.nextInt();
         }
      } catch (StackOverflowError ignored) {
         throw new OutOfMemoryError("This UniqueRandom object is out! Create a new one");
      }

      return (Integer)this.add(i);
   }

   @Override
   public long nextLong() {
      long i = super.nextLong();

      try {
         if (this.contains(i)) {
            return this.nextLong();
         }
      } catch (StackOverflowError ignoredEx) {
         throw new OutOfMemoryError("This UniqueRandom object is out! Create a new one");
      }

      return (Long)this.add(i);
   }

   @Override
   public double nextDouble() {
      double i = super.nextDouble();

      try {
         if (this.contains(i)) {
            return this.nextDouble();
         }
      } catch (StackOverflowError ignored) {
         throw new OutOfMemoryError("This UniqueRandom object is out! Create a new one");
      }

      return (Double)this.add(i);
   }

   @Override
   public float nextFloat() {
      float i = super.nextFloat();

      try {
         if (this.contains(i)) {
            return this.nextFloat();
         }
      } catch (StackOverflowError ignoredEx) {
         throw new OutOfMemoryError("This UniqueRandom object is out! Create a new one");
      }

      return (Float)this.add(i);
   }

   private boolean contains(Object o) {
      return this.get(o.getClass()).contains(o);
   }

   private Object add(Object o) {
      this.get(o.getClass()).add(o);
      return o;
   }

   private List<Object> get(Class<?> type) {
      if (!this.cache.containsKey(type)) {
         this.cache.put(type, new ArrayList<>());
      }

      return this.cache.get(type);
   }

   public static List<UniqueRandom> getList() {
      return list;
   }

   public ConcurrentHashMap<Class<?>, List<Object>> getCache() {
      return this.cache;
   }
}
