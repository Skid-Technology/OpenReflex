package rip.reflex.cloud.protocol.client;

import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.protocol.packet.PacketSignatureError;

public class AuthRequestPacket extends Packet {
   private int spigotID;
   private String key;

   public AuthRequestPacket(int spigotID, String key) {
      super("RC_AUTH_REQ");
      this.spigotID = spigotID;
      this.key = key;
   }

   public AuthRequestPacket(Packet source) {
      super("RC_AUTH_REQ");
      if (!source.getType().equals("RC_AUTH_REQ")) {
         throw new PacketSignatureError("Not a RC_AUTH_REQ packet, but " + source.getType());
      } else {
         this.setContents(source.getContents());
         this.readAll();
      }
   }

   @Override
   public Packet readAll() {
      this.spigotID = this.getContents().readInteger(0);
      this.key = this.getContents().readString(1);
      return this;
   }

   @Override
   public Packet writeAll() {
      this.getContents().write(0, this.spigotID);
      this.getContents().write(1, this.key);
      return this;
   }

   public int getSpigotID() {
      return this.spigotID;
   }

   public String getKey() {
      return this.key;
   }

   public void setSpigotID(int spigotID) {
      this.spigotID = spigotID;
   }

   public void setKey(String key) {
      this.key = key;
   }
}
