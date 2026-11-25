package rip.reflex.util.protocol.mcaccess;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Handle<T> {
   private final T bukkitObject;
   private final Object handle;
   private final Class<?> handleClass;

   public Handle(T bukkitObject, Object handle, Class<?> c) {
      this.bukkitObject = bukkitObject;
      this.handle = handle;
      this.handleClass = c;
   }

   public Handle(T bukkitObject, Object handle) {
      this(bukkitObject, handle, handle.getClass());
   }

   public static Handle<ItemStack> fromItemStack(ItemStack item) {
      try {
         Method asNMSCopyMethod = MCAccess.getCraftClass("inventory.CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class);
         Object nmsItem = asNMSCopyMethod.invoke(null, item);
         return new Handle<>(item, nmsItem);
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
         return handleError(ex);
      }
   }

   public static Handle<Entity> fromEntity(Entity e) {
      try {
         return new Handle<>(e, e.getClass().getDeclaredMethod("getHandle").invoke(e));
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException err) {
         return handleError(err);
      }
   }

   public static Handle<Player> fromEntity(Player e) {
      try {
         return new Handle<>(e, e.getClass().getDeclaredMethod("getHandle").invoke(e));
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException cause) {
         return handleError(cause);
      }
   }

   private static <T> T handleError(Exception e) {
      System.err.println("ERROR IN REFLECTION -> Handle");
      e.printStackTrace();
      return null;
   }

   public T getBukkitObject() {
      return this.bukkitObject;
   }

   public Object getHandle() {
      return this.handle;
   }

   public Class<?> getHandleClass() {
      return this.handleClass;
   }
}
