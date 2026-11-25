package rip.reflex.util.protocol.mcaccess;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MCAccess {
   public static final String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
   private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES;
   private static final ConcurrentHashMap<String, Class<?>> cache = new ConcurrentHashMap<>();

   public static Object constructWithDef(String s) {
      try {
         return newInstance(getConstructor(Class.forName(s)));
      } catch (ClassNotFoundException ex) {
         return sneakyThrow(ex);
      }
   }

   public static BoundingBox getEntityBoundingBox(Entity e) {
      return BoundingBox.of(e).expand(0.885, 0.725, 0.885).expandDown(1.35);
   }

   public static Class<?> getNMSClass(String NMSClassName) {
      Class<?> c = cache.get(NMSClassName);
      if (c == null) {
         try {
            c = Class.forName("net.minecraft.server." + VERSION + "." + NMSClassName);
         } catch (ClassNotFoundException err) {
            return (Class<?>)sneakyThrow(err);
         }
      }

      Validate.notNull(c, "NMS Class cannot be null");
      cache.put(NMSClassName, c);
      return c;
   }

   public static Class<?> getClass(String name) {
      Class<?> c = cache.get(name);
      if (c == null) {
         try {
            c = Class.forName(name);
            cache.put(name, c);
         } catch (ClassNotFoundException ignored) {
            throw new RuntimeException("Reflex // Reflection error: no such class \"" + name + "\"");
         }
      }

      return c;
   }

   public static Class<?> getClassSafely(String name) {
      Class<?> c = cache.get(name);
      if (c == null) {
         try {
            c = Class.forName(name);
            cache.put(name, c);
         } catch (ClassNotFoundException ignoredEx) {
         }
      }

      return c;
   }

   public static Class<?> getCraftClass(String OBSClassName) {
      try {
         return Class.forName("org.bukkit.craftbukkit." + VERSION + "." + OBSClassName);
      } catch (Exception ex) {
         return (Class<?>)handleError(ex);
      }
   }

   public static boolean isPacket(Object o, String packetName) {
      Validate.notNull(getNMSClass(packetName), "NMS class cannot be null! (" + packetName + ", ver=" + VERSION + ")");
      return o != null && getNMSClass(packetName).isInstance(o);
   }

   public static void setField(Object o, String name, Object value) {
      setField(o, name, value, o.getClass());
   }

   public static void setField(final Object o, final String name, final Object value, final Class<?> clazz) {
      new MCAccess.NMSTry() {
         @Override
         public Object tryit() throws Exception {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(o, value);
            return null;
         }
      };
   }

   public static <T> Constructor<T> getConstructor(Class<T> targetClass, Class<?>... paraTypes) {
      Constructor<T> c = (Constructor<T>)Arrays.stream(targetClass.getDeclaredConstructors())
         .filter(constructor -> Arrays.equals((Object[])constructor.getParameterTypes(), (Object[])paraTypes))
         .findFirst()
         .orElse(null);
      if (c != null) {
         c.setAccessible(true);
      }

      return c;
   }

   public static <T> T newInstance(Constructor<T> constructor, Object... types) {
      constructor.setAccessible(true);

      try {
         return constructor.newInstance(types);
      } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
         return (T)handleError(ex);
      }
   }

   public static Object getTag(ItemStack item) {
      try {
         Handle<ItemStack> h = Handle.fromItemStack(item);
         Object nmsItem = h.getHandle();
         Method getTagMethod = nmsItem.getClass().getDeclaredMethod("getTag");
         return getTagMethod.invoke(nmsItem);
      } catch (Exception e) {
         return handleError(e);
      }
   }

   public static ItemStack toCraftStack(Object nmsItem) {
      try {
         Class<?> nmsItemStackClass = getNMSClass("ItemStack");
         Class<?> craftItemStackClass = getCraftClass("inventory.CraftItemStack");
         Constructor constructor = craftItemStackClass.getDeclaredConstructor(nmsItemStackClass);
         constructor.setAccessible(true);
         Object craftItem = constructor.newInstance(nmsItemStackClass.cast(nmsItem));
         return (ItemStack)craftItem;
      } catch (Exception ex) {
         return (ItemStack)handleError(ex);
      }
   }

   public static Object getTag(Object nmsItem) {
      try {
         Method getTagMethod = nmsItem.getClass().getDeclaredMethod("getTag");
         return getTagMethod.invoke(nmsItem);
      } catch (Exception e) {
         return handleError(e);
      }
   }

   public static Field getField(Class<?> clazz, String name) {
      try {
         Field field = clazz.getDeclaredField(name);
         field.setAccessible(true);
         return field;
      } catch (Exception err) {
         err.printStackTrace();
         return null;
      }
   }

   public static Field getFieldWithFallback(Class<?> clazz, String primaryName, String fallbackName) {
      try {
         Field field = clazz.getDeclaredField(primaryName);
         field.setAccessible(true);
         return field;
      } catch (Exception ignored) {
         return getField(clazz, fallbackName);
      }
   }

   public static Object getField(Object o, String name, Class<?> clazz) {
      try {
         Field field = clazz.getDeclaredField(name);
         field.setAccessible(true);
         return field.get(o);
      } catch (NoSuchFieldException | IllegalAccessException ex) {
         throw new RuntimeException("Cannot get field", ex);
      }
   }

   public static Object getField(Object o, String name) {
      return getField(o, name, o.getClass());
   }

   public static Object getFromField(Field field, Object instance) {
      try {
         if (!field.isAccessible()) {
            field.setAccessible(true);
         }

         return field.get(instance);
      } catch (IllegalAccessException e) {
         return sneakyThrow(e);
      }
   }

   public static <T> T getFromField(Field f, Object instance, Class<T> cast) {
      return (T)getFromField(f, instance);
   }

   public static void sendPluginMessage(Player player, String channel, byte[] message) {
      if (message == null) {
         throw new NullPointerException("Cannot send null plugin message");
      } else if (message.length == 0) {
         throw new IllegalArgumentException("Cannot send empty plugin message");
      } else {
         try {
            Object byteBuf = ReflectionCache.wrappedBufferMethod.invoke(null, message);
            Object packetDataSerializer = ReflectionCache.packetDataSerializerConstructor.newInstance(byteBuf);
            Object packet = ReflectionCache.Packets.PacketPlayOutCustomPayload_Constructor.newInstance(channel, packetDataSerializer);
            Object nmsPlayer = ReflectionCache.getPlayerHandle.invoke(player);
            Object playerConnection = ReflectionCache.playerConnectionField.get(nmsPlayer);
            ReflectionCache.sendPacketMethod.invoke(playerConnection, packet);
         } catch (Exception ex) {
            throw new RuntimeException("Error sending plugin message through \"" + channel + "\" (" + message.length + " bytes)", ex);
         }
      }
   }

   public static void secureSetFinalStatic(Field field, Object newValue) throws Exception {
      boolean preAccessible = field.isAccessible();
      field.setAccessible(true);
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      boolean preModsAccessible = modifiersField.isAccessible();
      modifiersField.setAccessible(true);
      int preMods = modifiersField.getInt(field);
      modifiersField.setInt(field, field.getModifiers() & -17);
      field.set(null, newValue);
      modifiersField.setInt(field, preMods);
      modifiersField.setAccessible(preModsAccessible);
      field.setAccessible(preAccessible);
   }

   private static Object handleError(Exception e) {
      System.err.println("REFLEX // REFLECTION ERROR: " + e.toString());
      e.printStackTrace();
      return null;
   }

   public static Object invokeSneaky(Method method, Object instance, Object... args) {
      try {
         return method.invoke(instance, args);
      } catch (InvocationTargetException | IllegalAccessException ex) {
         return sneakyThrow(ex);
      }
   }

   public static Object sneakyThrow(Throwable t) {
      sneakyThrow0(t);
      return null;
   }

   public static void sneakyThrow0(Throwable t) {
      throw new RuntimeException("SneakyThrow -> NMSReflection", t);
   }

   public static Object getHandle(Object obj) {
      try {
         return getMethod("getHandle", obj.getClass()).invoke(obj);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
      Class<?>[] t = toPrimitiveTypeArray(paramTypes);

      for (Method m : clazz.getDeclaredMethods()) {
         if (m.getName().equals(name)) {
            if (paramTypes == null || paramTypes.length == 0) {
               m.setAccessible(true);
               return m;
            }

            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (equalsTypeArray(types, t)) {
               m.setAccessible(true);
               return m;
            }
         }
      }

      return null;
   }

   private static Class<?>[] toPrimitiveTypeArray(Object[] objects) {
      int a = objects != null ? objects.length : 0;
      Class<?>[] types = new Class[a];

      for (int i = 0; i < a; i++) {
         types[i] = getPrimitiveType(objects[i].getClass());
      }

      return types;
   }

   private static Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
      int a = classes != null ? classes.length : 0;
      Class<?>[] types = new Class[a];

      for (int i = 0; i < a; i++) {
         types[i] = getPrimitiveType(classes[i]);
      }

      return types;
   }

   public static Object getMinecraftServer() {
      Class<?> craftServerClass = getCraftClass("CraftServer");
      Method getServerMethod = getMethod("getServer", craftServerClass);
      return invokeSneaky(getServerMethod, Bukkit.getServer());
   }

   private static Class<?> getPrimitiveType(Class<?> clazz) {
      return CORRESPONDING_TYPES.getOrDefault(clazz, clazz);
   }

   private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
      if (a.length != o.length) {
         return false;
      } else {
         for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) {
               return false;
            }
         }

         return true;
      }
   }

   public static int getTicks() {
      return (Integer)getFromField(ReflectionCache.ticksField, ReflectionCache.mcServerInstance);
   }

   static {
      (CORRESPONDING_TYPES = new ConcurrentHashMap<>()).put(Byte.class, byte.class);
      CORRESPONDING_TYPES.put(Short.class, short.class);
      CORRESPONDING_TYPES.put(Integer.class, int.class);
      CORRESPONDING_TYPES.put(Long.class, long.class);
      CORRESPONDING_TYPES.put(Character.class, char.class);
      CORRESPONDING_TYPES.put(Float.class, float.class);
      CORRESPONDING_TYPES.put(Double.class, double.class);
      CORRESPONDING_TYPES.put(Boolean.class, boolean.class);
   }

   public abstract static class NMSTry<T> implements AutoCloseable {
      public abstract T tryit() throws Exception;

      public T doIt() {
         try {
            return this.tryit();
         } catch (Exception ex) {
            System.out.println("ERROR IN REFLECTION!");
            ex.printStackTrace();
            return null;
         }
      }

      @Override
      public void close() throws IOException {
      }
   }
}
