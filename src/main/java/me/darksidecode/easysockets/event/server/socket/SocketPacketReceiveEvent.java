package me.darksidecode.easysockets.event.server.socket;

import java.net.Socket;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.protocol.packet.Packet;

public class SocketPacketReceiveEvent extends SocketEvent {
   private Packet packet;

   public SocketPacketReceiveEvent(EventState state, Socket socket, Packet packet) {
      super(state, socket);
      this.packet = packet;
   }

   public Packet getPacket() {
      return this.packet;
   }

   public void setPacket(Packet packet) {
      this.packet = packet;
   }
}
