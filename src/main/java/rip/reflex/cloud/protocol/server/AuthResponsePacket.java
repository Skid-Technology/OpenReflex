package rip.reflex.cloud.protocol.server;

import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.protocol.packet.PacketSignatureError;

public class AuthResponsePacket extends Packet {
   private int responseCode;
   private long expireDate;

   public AuthResponsePacket(int responseCode, long expireDate) {
      super("RC_AUTH_RES");
      this.responseCode = responseCode;
      this.expireDate = expireDate;
   }

   public AuthResponsePacket(Packet source) {
      super("RC_AUTH_RES");
      if (!source.getType().equals("RC_AUTH_RES")) {
         throw new PacketSignatureError("Not a RC_AUTH_RES packet, but " + source.getType());
      } else {
         this.setContents(source.getContents());
         this.readAll();
      }
   }

   @Override
   public Packet readAll() {
      this.responseCode = this.getContents().readInteger(0);
      this.expireDate = this.getContents().readLong(1);
      return this;
   }

   @Override
   public Packet writeAll() {
      this.getContents().write(0, this.responseCode);
      this.getContents().write(1, this.expireDate);
      return this;
   }

   public int getResponseCode() {
      return this.responseCode;
   }

   public long getExpireDate() {
      return this.expireDate;
   }

   public void setResponseCode(int responseCode) {
      this.responseCode = responseCode;
   }

   public void setExpireDate(long expireDate) {
      this.expireDate = expireDate;
   }
}
