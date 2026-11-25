package rip.reflex.util.protocol.packetaccess;

import com.comphenix.protocol.events.PacketContainer;
import rip.reflex.util.protocol.packet.Packets;

public abstract class PacketAccessor {
   private final PacketContainer packet;

   public PacketAccessor(PacketContainer packet) {
      this.packet = packet;
   }

   protected int readInt(int index) {
      return Packets.readInteger(this.packet, index);
   }

   protected float readFloat(int index) {
      return (Float)this.packet.getFloat().readSafely(index);
   }

   protected boolean readBoolean(int index) {
      return (Boolean)this.packet.getBooleans().readSafely(index);
   }

   protected PacketAccessor writeInt(int index, int value) {
      this.packet.getIntegers().writeSafely(index, value);
      return this;
   }

   protected PacketAccessor writeFloat(int index, float value) {
      this.packet.getFloat().writeSafely(index, value);
      return this;
   }

   protected PacketAccessor writeBoolean(int index, boolean value) {
      this.packet.getBooleans().writeSafely(index, value);
      return this;
   }

   public PacketContainer getPacket() {
      return this.packet;
   }
}
