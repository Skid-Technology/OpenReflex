package me.darksidecode.easysockets.protocol.packet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PacketContents {
   private final Map<Integer, Object> raw;

   public PacketContents() {
      this(new ConcurrentHashMap<>());
   }

   public PacketContents(Map<Integer, Object> source) {
      this.raw = source;
   }

   public PacketContents write(int index, Object obj) {
      if (obj == null) {
         throw new NullPointerException("Cannot write NULL object");
      } else {
         this.raw.put(index, obj);
         return this;
      }
   }

   public String readString(int index) {
      return this.read(index);
   }

   public String readStringSafely(int index) {
      return this.readSafely(index);
   }

   public Integer readInteger(int index) {
      return this.read(index);
   }

   public Integer readIntegerSafely(int index) {
      return this.readSafely(index);
   }

   public Short readShort(int index) {
      return this.read(index);
   }

   public Short readShortSafely(int index) {
      return this.readSafely(index);
   }

   public Long readLong(int index) {
      return this.read(index);
   }

   public Long readLongSafely(int index) {
      return this.readSafely(index);
   }

   public Double readDouble(int index) {
      return this.read(index);
   }

   public Double readDoubleSafely(int index) {
      return this.readSafely(index);
   }

   public Float readFloat(int index) {
      return this.read(index);
   }

   public Float readFloatSafely(int index) {
      return this.readSafely(index);
   }

   public Byte readByte(int index) {
      return this.read(index);
   }

   public Byte readByteSafely(int index) {
      return this.readSafely(index);
   }

   public Boolean readBoolean(int index) {
      return this.read(index);
   }

   public Boolean readBooleanSafely(int index) {
      return this.readSafely(index);
   }

   public Object readObject(int index) {
      return this.read(index);
   }

   public Object readObjectSafely(int index) {
      return this.readSafely(index);
   }

   public <T> T read(int index) {
      return (T)this.readRaw(index);
   }

   public <T> T readSafely(int index) {
      Object obj = this.readRawSafely(index);
      return (T)(obj == null ? null : obj);
   }

   public Object readRaw(int index) {
      if (!this.raw.containsKey(index)) {
         throw new IndexOutOfBoundsException("Index out of packet content range: " + index);
      } else {
         return this.raw.get(index);
      }
   }

   public Object readRawSafely(int index) {
      return this.raw.getOrDefault(index, null);
   }

   public Class getClass(int index) {
      Object obj = this.readRawSafely(index);
      return obj == null ? null : obj.getClass();
   }

   @Override
   public String toString() {
      StringBuilder s = new StringBuilder();
      s.append('[');

      for (Integer k : this.raw.keySet()) {
         s.append(k).append(": ").append(this.raw.get(k).toString()).append(',');
      }

      if (s.length() > 1) {
         s.deleteCharAt(s.length() - 1);
      }

      s.append(']');
      return s.toString();
   }

   public Map<Integer, Object> getRaw() {
      return this.raw;
   }
}
