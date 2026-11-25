package rip.reflex.cloud.protocol;

import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.protocol.packet.PacketSignatureError;

public class PingPacket extends Packet {
   private int id;

   public PingPacket(int id) {
      super("RC_PING");
      this.id = id;
   }

   public PingPacket(Packet source) {
      super("RC_PING");
      if (!source.getType().equals("RC_PING")) {
         throw new PacketSignatureError("Not a RC_PING packet, but " + source.getType());
      } else {
         this.setContents(source.getContents());
         this.readAll();
      }
   }

   @Override
   public Packet readAll() {
      this.id = this.getContents().readInteger(0);
      return this;
   }

   @Override
   public Packet writeAll() {
      this.getContents().write(0, this.id);
      return this;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }
}
