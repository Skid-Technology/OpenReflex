package me.darksidecode.easysockets.event.client;

import me.darksidecode.easysockets.event.Event;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.protocol.packet.Packet;

public class ClientPacketReceiveEvent extends Event {
   private Packet packet;

   public ClientPacketReceiveEvent(EventState state, Packet packet) {
      super(state);
      this.packet = packet;
   }

   public Packet getPacket() {
      return this.packet;
   }

   public void setPacket(Packet packet) {
      this.packet = packet;
   }
}
