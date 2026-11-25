package me.darksidecode.easysockets.protocol.packet;

import java.io.Serializable;
import java.util.Map;

public class RawPacket implements Serializable {
   private static final long serialVersionUID = 331896648141185773L;
   private final String type;
   private final Map<Integer, Object> contents;

   public RawPacket(String type, Map<Integer, Object> contents) {
      this.type = type;
      this.contents = contents;
   }

   public String getType() {
      return this.type;
   }

   public Map<Integer, Object> getContents() {
      return this.contents;
   }
}
