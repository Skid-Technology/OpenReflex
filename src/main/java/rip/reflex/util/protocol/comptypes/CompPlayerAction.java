package rip.reflex.util.protocol.comptypes;

import com.comphenix.protocol.events.PacketContainer;
import java.util.Arrays;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.packet.Packets;

public enum CompPlayerAction {
   START_SNEAKING((byte)1),
   STOP_SNEAKING((byte)2),
   STOP_SLEEPING((byte)3),
   START_SPRINTING((byte)4),
   STOP_SPRINTING((byte)5),
   START_RIDING_JUMP((byte)6),
   STOP_RIDING_JUMP((byte)-1),
   OPEN_INVENTORY((byte)-1),
   START_FALL_FLYING((byte)-1);

   private final byte id1_7;

   public static CompPlayerAction byPacket(PacketContainer packet) {
      if (VersionUtils.getServerVersion().getId() < MCVersion.V1_8_X.getId()) {
         byte id = Packets.readByte(packet, 0);
         return Arrays.stream(values())
            .filter(cpa -> cpa.getId1_7() == id)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Protocol incompatibility (" + id + ")"));
      } else {
         return byPacket1_8(packet);
      }
   }

   private static CompPlayerAction byPacket1_8(PacketContainer packet) {
      return valueOf(Packets.readPlayerActionSafely(packet, 0).name());
   }

   private CompPlayerAction(byte id1_7) {
      this.id1_7 = id1_7;
   }

   public byte getId1_7() {
      return this.id1_7;
   }
}
