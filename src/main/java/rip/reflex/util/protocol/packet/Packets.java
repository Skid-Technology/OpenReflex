package rip.reflex.util.protocol.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.PacketType.Play.Client;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import rip.reflex.Reflex;
import rip.reflex.util.debugging.PacketDebugger;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.comptypes.CompPlayerAction;
import rip.reflex.util.protocol.npc.Animations;

public class Packets {
   public static final PacketType IN_FLYING = Client.FLYING;
   public static final PacketType IN_POSITION = Client.POSITION;
   public static final PacketType IN_LOOK = Client.LOOK;
   public static final PacketType IN_POSLOOK = Client.POSITION_LOOK;
   public static final PacketType IN_USEENTITY = Client.USE_ENTITY;
   public static final PacketType IN_USEITEM = Client.USE_ITEM;
   public static final PacketType IN_ENTITYACTION = Client.ENTITY_ACTION;
   public static final PacketType IN_BLOCKPLACE = Client.BLOCK_PLACE;
   public static final PacketType IN_SWING = Client.ARM_ANIMATION;
   public static final PacketType IN_KEEPALIVE = Client.KEEP_ALIVE;
   public static final PacketType IN_TRANSACTION = Client.TRANSACTION;
   public static final PacketType IN_ABILITIES = Client.ABILITIES;
   public static final PacketType IN_CLIENTCOMMAND = Client.CLIENT_COMMAND;
   public static final PacketType IN_CHAT = Client.CHAT;
   public static final PacketType IN_CLIENTSETTINGS = Client.SETTINGS;
   public static final PacketType IN_HELDITEMCHANGE = Client.HELD_ITEM_SLOT;
   public static final PacketType IN_VEHICLEMOVE = Client.VEHICLE_MOVE;
   public static final PacketType IN_RESOURCEPACKSTATUS = Client.RESOURCE_PACK_STATUS;
   public static final PacketType IN_CLICKWINDOW = Client.WINDOW_CLICK;
   public static final PacketType[] CLIENT_UPDATE_PACKETS = new PacketType[]{IN_FLYING, IN_POSITION, IN_LOOK, IN_POSLOOK};
   public static final PacketType OUT_ENTITYVELOCITY = Server.ENTITY_VELOCITY;
   public static final PacketType OUT_KEEPALIVE = Server.KEEP_ALIVE;
   public static final PacketType OUT_TRANSACTION = Server.TRANSACTION;
   public static final PacketType OUT_TEAMS = Server.SCOREBOARD_TEAM;
   public static final PacketType OUT_ABILITIES = Server.ABILITIES;
   public static final PacketType OUT_ENTITYMETADATA = Server.ENTITY_METADATA;
   public static final PacketType OUT_ANIMATION = Server.ANIMATION;
   public static final PacketType OUT_CHAT = Server.CHAT;
   public static final PacketType OUT_RESOURCEPACKSEND = Server.RESOURCE_PACK_SEND;
   private static final Map<PacketContainer, PacketCache> caches = new ConcurrentHashMap<>();

   public static PlayerAction readPlayerActionSafely(PacketContainer p, int i) {
      try {
         return (PlayerAction)p.getPlayerActions().read(i);
      } catch (IllegalArgumentException ex) {
         if (ex.getMessage().contains("RIDING_JUMP")) {
            return PlayerAction.START_RIDING_JUMP;
         } else {
            throw ex;
         }
      }
   }

   public static Integer readInteger(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readInteger(index) : (Integer)packet.getIntegers().read(index);
   }

   public static Short readShort(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readShort(index) : (Short)packet.getShorts().read(index);
   }

   public static Long readLong(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readLong(index) : (Long)packet.getLongs().read(index);
   }

   public static Float readFloat(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readFloat(index) : (Float)packet.getFloat().read(index);
   }

   public static Double readDouble(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readDouble(index) : (Double)packet.getDoubles().read(index);
   }

   public static Boolean readBoolean(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readBoolean(index) : (Boolean)packet.getBooleans().read(index);
   }

   public static Byte readByte(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readByte(index) : (Byte)packet.getBytes().read(index);
   }

   public static String readString(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readString(index) : (String)packet.getStrings().read(index);
   }

   public static PlayerAction readPlayerAction(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readPlayerAction(index) : (PlayerAction)packet.getPlayerActions().read(index);
   }

   public static EntityUseAction readEntityUseAction(PacketContainer packet, int index) {
      return Reflex.getReflex().isCachingPackets() ? cachePacket(packet).readEntityUseAction(index) : (EntityUseAction)packet.getEntityUseActions().read(index);
   }

   public static void clearPacketCaches() {
      if (Reflex.getReflex().isCachingPackets()) {
         List<PacketContainer> toRem = new ArrayList<>();

         for (PacketContainer packet : caches.keySet()) {
            PacketCache cache = caches.get(packet);
            if (cache.getAccessTracker().hasPassed(5000L)) {
               toRem.add(packet);
               cache.clear();
            }
         }

         toRem.forEach(caches::remove);
      }
   }

   private static PacketCache cachePacket(PacketContainer packet) {
      if (!caches.containsKey(packet)) {
         caches.put(packet, new PacketCache(packet));
      }

      return caches.get(packet);
   }

   public static boolean isEmptyFlying(PacketContainer packet) {
      return (
            packet.getType().equals(IN_LOOK)
               || packet.getType().equals(IN_FLYING)
               || packet.getType().equals(IN_POSITION)
               || packet.getType().equals(IN_POSLOOK)
         )
         && !readBoolean(packet, 1);
   }

   public static boolean isMove(PacketContainer packet) {
      return packet != null && (packet.getType().equals(IN_POSITION) || packet.getType().equals(IN_POSLOOK));
   }

   public static boolean isAttackPacket(PacketContainer packet) {
      if (packet == null || !packet.getType().equals(IN_USEENTITY)) {
         return false;
      } else {
         return VersionUtils.getServerVersion().getId() > MCVersion.V1_7_X.getId()
            ? readEntityUseAction(packet, 0).equals(EntityUseAction.ATTACK)
            : readByte(packet, 0) == 1;
      }
   }

   public static boolean isSneakPacket(PacketContainer packet) {
      if (packet != null && packet.getType().equals(IN_ENTITYACTION)) {
         CompPlayerAction action = CompPlayerAction.byPacket(packet);
         return action == CompPlayerAction.START_SNEAKING || action == CompPlayerAction.STOP_SNEAKING;
      } else {
         return false;
      }
   }

   public static boolean isStartSneakingPacket(PacketContainer packet) {
      if (packet != null && packet.getType().equals(IN_ENTITYACTION)) {
         CompPlayerAction action = CompPlayerAction.byPacket(packet);
         return action == CompPlayerAction.START_SNEAKING;
      } else {
         return false;
      }
   }

   public static boolean isStopSneakingPacket(PacketContainer packet) {
      if (packet != null && packet.getType().equals(IN_ENTITYACTION)) {
         CompPlayerAction action = CompPlayerAction.byPacket(packet);
         return action == CompPlayerAction.STOP_SNEAKING;
      } else {
         return false;
      }
   }

   public static boolean isDamageAnimOf(PacketContainer p, Entity e) {
      return p != null && p.getType().equals(OUT_ANIMATION) && readInteger(p, 0) == e.getEntityId() && readInteger(p, 1) == Animations.TAKE_DAMAGE.ordinal();
   }

   public static boolean isUseItemPacket(PacketContainer packet) {
      return packet != null && packet.getType().equals(IN_BLOCKPLACE);
   }

   public static boolean isUpdatePacket(PacketContainer packet) {
      return packet.getType().equals(IN_FLYING)
         || packet.getType().equals(IN_POSITION)
         || packet.getType().equals(IN_LOOK)
         || packet.getType().equals(IN_POSLOOK);
   }

   public static Vector getVelocity(PacketContainer packet) {
      if (packet != null && packet.getType().equals(OUT_ENTITYVELOCITY)) {
         int x = readInteger(packet, 1);
         int y = readInteger(packet, 2);
         int z = readInteger(packet, 3);
         return new Vector((double)x / 8000.0, (double)y / 8000.0, (double)z / 8000.0);
      } else {
         return null;
      }
   }

   public static void sendPacket(Player player, PacketContainer packet) {
      try {
         ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
         PacketDebugger.addEntry(player, packet, false);
      } catch (Exception ex) {
         throw new RuntimeException(
            "Failed to send packet " + packet.toString() + " to " + player.getName() + " from current thread " + Thread.currentThread().getName(), ex
         );
      }
   }

   public static void sendPacketSync(Player player, PacketContainer packet) {
      try {
         if (Bukkit.isPrimaryThread()) {
            sendPacket(player, packet);
         } else {
            Bukkit.getScheduler().runTask(Reflex.getReflex(), () -> sendPacket(player, packet));
         }
      } catch (Exception e) {
         throw new RuntimeException("Failed to send packet " + packet.toString() + " to " + player.getName() + " from server's primary thread", e);
      }
   }

   public static void sendPacketAsync(Player player, PacketContainer packet) {
      try {
         ProtocolLibrary.getProtocolManager().getAsynchronousManager().getPacketStream().sendServerPacket(player, packet);
         PacketDebugger.addEntry(player, packet, true);
      } catch (Exception ex) {
         throw new RuntimeException("Failed to send packet " + packet.toString() + " to " + player.getName() + " asynchronously", ex);
      }
   }

   public static Map<PacketContainer, PacketCache> getCaches() {
      return caches;
   }
}
