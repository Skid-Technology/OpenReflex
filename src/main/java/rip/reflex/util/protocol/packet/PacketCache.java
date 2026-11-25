package rip.reflex.util.protocol.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import rip.reflex.util.time.Timer;

public class PacketCache implements Serializable {
   private final Map<Class<?>, Map<Integer, Object>> map = new ConcurrentHashMap<>();
   private final Timer accessTracker = new Timer();
   private final PacketContainer packet;

   public PacketCache(PacketContainer packet) {
      this.packet = Objects.requireNonNull(packet, "[PC] Packet cannot be null");
   }

   public Integer readInteger(int index) {
      Map<Integer, Object> m = this.getTypeMap(Integer.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getIntegers().read(index));
   }

   public Short readShort(int index) {
      Map<Integer, Object> m = this.getTypeMap(Short.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getShorts().read(index));
   }

   public Long readLong(int index) {
      Map<Integer, Object> m = this.getTypeMap(Long.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getLongs().read(index));
   }

   public Float readFloat(int index) {
      Map<Integer, Object> m = this.getTypeMap(Float.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getFloat().read(index));
   }

   public Double readDouble(int index) {
      Map<Integer, Object> m = this.getTypeMap(Double.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getDoubles().read(index));
   }

   public Boolean readBoolean(int index) {
      Map<Integer, Object> m = this.getTypeMap(Boolean.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getBooleans().read(index));
   }

   public Byte readByte(int index) {
      Map<Integer, Object> m = this.getTypeMap(Byte.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getBytes().read(index));
   }

   public String readString(int index) {
      Map<Integer, Object> m = this.getTypeMap(String.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getStrings().read(index));
   }

   public PlayerAction readPlayerAction(int index) {
      Map<Integer, Object> m = this.getTypeMap(PlayerAction.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, Packets.readPlayerActionSafely(this.packet, index));
   }

   public EntityUseAction readEntityUseAction(int index) {
      Map<Integer, Object> m = this.getTypeMap(EntityUseAction.class);
      return m.containsKey(index) ? this.fromMap(m, index) : this.setAndReturn(m, index, this.packet.getEntityUseActions().read(index));
   }

   public void clear() {
      this.map.clear();
   }

   private <T> T fromMap(Map<Integer, Object> m, int index) {
      this.accessTracker.reset();
      return (T)m.get(index);
   }

   private <T> T setAndReturn(Map<Integer, Object> m, int index, Object value) {
      this.accessTracker.reset();
      m.put(index, value);
      return (T)value;
   }

   private Map<Integer, Object> getTypeMap(Class<?> type) {
      if (!this.map.containsKey(type)) {
         this.map.put(type, new ConcurrentHashMap<>());
      }

      return this.map.get(type);
   }

   public Map<Class<?>, Map<Integer, Object>> getMap() {
      return this.map;
   }

   public Timer getAccessTracker() {
      return this.accessTracker;
   }
}
