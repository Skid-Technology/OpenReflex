package rip.reflex.util.protocol.mcaccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class ReflectionCache<A> {
   public static final ReflectionCache<World> worldCache = new ReflectionCache<>();
   public static final ReflectionCache<Player> playerCache = new ReflectionCache<>();
   public static final Class<?> craftTaskClass = MCAccess.getCraftClass("scheduler.CraftTask");
   public static final Class<?> boundingBoxClass = MCAccess.getNMSClass("AxisAlignedBB");
   public static final Class<?> entityClass = MCAccess.getNMSClass("Entity");
   public static final Class<?> nmsWorldClass = MCAccess.getNMSClass("World");
   public static final Class<?> craftWorldClass = MCAccess.getCraftClass("CraftWorld");
   public static final Class<?> minecraftServerClass = MCAccess.getNMSClass("MinecraftServer");
   public static final Class<?> nmsBlockClass = MCAccess.getNMSClass("Block");
   public static final Class<?> craftEntityClass = MCAccess.getCraftClass("entity.CraftEntity");
   public static final Class<?> craftPlayerClass = MCAccess.getCraftClass("entity.CraftPlayer");
   public static final Class<?> nmsPlayerClass = MCAccess.getNMSClass("EntityPlayer");
   public static final Class<?> playerConnectionClass = MCAccess.getNMSClass("PlayerConnection");
   public static final Class<?> packetDataSerializerClass = MCAccess.getNMSClass("PacketDataSerializer");
   public static final Class<?> byteBufClass = MCAccess.getClass("io.netty.buffer.ByteBuf");
   public static final Class<?> unpooledClass = MCAccess.getClass("io.netty.buffer.Unpooled");
   public static final Field playerConnectionField = MCAccess.getField(nmsPlayerClass, "playerConnection");
   public static final Field boxField_a = MCAccess.getFieldWithFallback(boundingBoxClass, "a", "minX");
   public static final Field boxField_b = MCAccess.getFieldWithFallback(boundingBoxClass, "b", "minY");
   public static final Field boxField_c = MCAccess.getFieldWithFallback(boundingBoxClass, "c", "minZ");
   public static final Field boxField_d = MCAccess.getFieldWithFallback(boundingBoxClass, "d", "maxX");
   public static final Field boxField_e = MCAccess.getFieldWithFallback(boundingBoxClass, "e", "maxY");
   public static final Field boxField_f = MCAccess.getFieldWithFallback(boundingBoxClass, "f", "maxZ");
   public static final Field blockMinX = null;
   public static final Field blockMinY = null;
   public static final Field blockMinZ = null;
   public static final Field blockMaxX = null;
   public static final Field blockMaxY = null;
   public static final Field blockMaxZ = null;
   public static final Field ticksField = MCAccess.getField(minecraftServerClass, "ticks");
   public static final Method getBBMethod = MCAccess.getMethod("getBoundingBox", entityClass);
   public static final Method getEntityHandle = MCAccess.getMethod("getHandle", craftEntityClass);
   public static final Method getPlayerHandle = MCAccess.getMethod("getHandle", craftPlayerClass);
   public static final Method getCubesMethod = MCAccess.getMethod("getCubes", nmsWorldClass, entityClass, boundingBoxClass);
   public static final Method getWorldHandle = MCAccess.getMethod("getHandle", craftWorldClass);
   public static final Method getNMSBlock = null;
   public static final Method sendPacketMethod = MCAccess.getMethod("sendPacket", playerConnectionClass);
   public static final Method wrappedBufferMethod = MCAccess.getMethod("wrappedBuffer", unpooledClass, byte[].class);
   public static final Constructor<?> boxConstructor = MCAccess.getConstructor(
      boundingBoxClass, double.class, double.class, double.class, double.class, double.class, double.class
   );
   public static final Constructor<?> packetDataSerializerConstructor = MCAccess.getConstructor(packetDataSerializerClass, byteBufClass);
   public static final Object mcServerInstance = MCAccess.getMinecraftServer();

   public static class Packets {
      public static final Class<?> PacketPlayOutCustomPayload = MCAccess.getNMSClass("PacketPlayOutCustomPayload");
      public static final Constructor<?> PacketPlayOutCustomPayload_Constructor = MCAccess.getConstructor(
         PacketPlayOutCustomPayload, String.class, ReflectionCache.packetDataSerializerClass
      );
   }
}
