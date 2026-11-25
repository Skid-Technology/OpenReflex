package rip.reflex.java9;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import sun.misc.Unsafe;

public class J9Util {
   private static int pluginYmlVerify = 587089213;
   private static final Unsafe unsafe;
   private static final long fieldOffset;
   private static final double javaVersion;

   public static int getYml(int r) {
      if (r % 2 != 0) {
         throw new IllegalArgumentException();
      } else {
         return pluginYmlVerify - r + 6;
      }
   }

   public static void setAccessible(AccessibleObject accessibleObject) {
      unsafe.putBoolean(accessibleObject, fieldOffset, true);
   }

   public static boolean isJava9() {
      return javaVersion > 8.0;
   }

   public static byte m3(byte b) {
      if (b == 42) {
         throw new NullPointerException();
      } else {
         int v;
         if (b < 0) {
            v = b * 3;
         } else {
            v = b - 2;
            v *= 3;
         }

         pluginYmlVerify = v;
         return v % 2 == 0 ? (byte)(pluginYmlVerify /= 2) : (byte)(v - 3);
      }
   }

   static {
      try {
         javaVersion = Double.parseDouble(System.getProperty("java.specification.version", "0"));
         unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>)(() -> {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe)field.get(null);
         }));
         fieldOffset = unsafe.objectFieldOffset(AccessibleObject.class.getDeclaredField("override"));
      } catch (NoSuchFieldException | PrivilegedActionException ex) {
         throw new RuntimeException(ex);
      }
   }
}
