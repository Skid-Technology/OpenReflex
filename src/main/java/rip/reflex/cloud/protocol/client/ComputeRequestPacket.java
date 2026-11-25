package rip.reflex.cloud.protocol.client;

import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.protocol.packet.PacketSignatureError;

public class ComputeRequestPacket extends Packet {
   private long requestID;
   private int checkID;
   private Object checkData;

   public ComputeRequestPacket(long requestID, int checkID, Object checkData) {
      super("RC_COMP_REQ");
      this.requestID = requestID;
      this.checkID = checkID;
      this.checkData = checkData;
   }

   public ComputeRequestPacket(Packet source) {
      super("RC_COMP_REQ");
      if (!source.getType().equals("RC_COMP_REQ")) {
         throw new PacketSignatureError("Not a RC_COMP_REQ packet, but " + source.getType());
      } else {
         this.setContents(source.getContents());
         this.readAll();
      }
   }

   @Override
   public Packet readAll() {
      this.requestID = this.getContents().readLong(0);
      this.checkID = this.getContents().readInteger(1);
      this.checkData = this.getContents().readObject(2);
      return this;
   }

   @Override
   public Packet writeAll() {
      this.getContents().write(0, this.requestID);
      this.getContents().write(1, this.checkID);
      this.getContents().write(2, this.checkData);
      return this;
   }

   public long getRequestID() {
      return this.requestID;
   }

   public int getCheckID() {
      return this.checkID;
   }

   public Object getCheckData() {
      return this.checkData;
   }

   public void setRequestID(long requestID) {
      this.requestID = requestID;
   }

   public void setCheckID(int checkID) {
      this.checkID = checkID;
   }

   public void setCheckData(Object checkData) {
      this.checkData = checkData;
   }
}
