package rip.reflex.cloud.protocol.server;

import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.protocol.packet.PacketSignatureError;

public class ComputeResponsePacket extends Packet {
   private long requestID;
   private int checkID;
   private String result;

   public ComputeResponsePacket(long requestID, int checkID, String result) {
      super("RC_COMP_RES");
      this.requestID = requestID;
      this.checkID = checkID;
      this.result = result;
   }

   public ComputeResponsePacket(Packet source) {
      super("RC_COMP_RES");
      if (!source.getType().equals("RC_COMP_RES")) {
         throw new PacketSignatureError("Not a RC_COMP_RES packet, but " + source.getType());
      } else {
         this.setContents(source.getContents());
         this.readAll();
      }
   }

   @Override
   public Packet readAll() {
      this.requestID = this.getContents().readLong(0);
      this.checkID = this.getContents().readInteger(1);
      this.result = this.getContents().readString(2);
      return this;
   }

   @Override
   public Packet writeAll() {
      this.getContents().write(0, this.requestID);
      this.getContents().write(1, this.checkID);
      this.getContents().write(2, this.result);
      return this;
   }

   public long getRequestID() {
      return this.requestID;
   }

   public int getCheckID() {
      return this.checkID;
   }

   public String getResult() {
      return this.result;
   }

   public void setRequestID(long requestID) {
      this.requestID = requestID;
   }

   public void setCheckID(int checkID) {
      this.checkID = checkID;
   }

   public void setResult(String result) {
      this.result = result;
   }
}
