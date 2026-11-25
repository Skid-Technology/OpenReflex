package rip.reflex.util.mod;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WDLPackets {
   public static byte[] createWDLPacket0(boolean canDoNewThings) {
      ByteArrayDataOutput output = ByteStreams.newDataOutput();
      output.writeInt(0);
      output.writeBoolean(canDoNewThings);
      return output.toByteArray();
   }

   public static byte[] createWDLPacket1(
      boolean globalIsEnabled, int saveRadius, boolean cacheChunks, boolean saveEntities, boolean saveTileEntities, boolean saveContainers
   ) {
      ByteArrayDataOutput output = ByteStreams.newDataOutput();
      output.writeInt(1);
      output.writeBoolean(globalIsEnabled);
      output.writeInt(saveRadius);
      output.writeBoolean(cacheChunks && globalIsEnabled);
      output.writeBoolean(saveEntities && globalIsEnabled);
      output.writeBoolean(saveTileEntities && globalIsEnabled);
      output.writeBoolean(saveContainers && saveTileEntities && globalIsEnabled);
      return output.toByteArray();
   }

   public static byte[] createWDLPacket2(Map<String, Integer> ranges) {
      ByteArrayDataOutput output = ByteStreams.newDataOutput();
      output.writeInt(2);
      output.writeInt(ranges.size());

      for (Entry<String, Integer> e : ranges.entrySet()) {
         output.writeUTF(e.getKey());
         output.writeInt(e.getValue());
      }

      return output.toByteArray();
   }

   public static byte[] createWDLPacket3(String message) {
      ByteArrayDataOutput output = ByteStreams.newDataOutput();
      output.writeInt(3);
      output.writeBoolean(true);
      output.writeUTF(message);
      return output.toByteArray();
   }

   public static byte[] createWDLPacket6(String group, List<String> tags) {
      ByteArrayDataOutput output = ByteStreams.newDataOutput();
      output.writeInt(6);
      output.writeUTF(group);
      output.writeInt(tags.size());

      for (String tag : tags) {
         output.writeUTF(tag);
      }

      return output.toByteArray();
   }
}
