package rip.reflex.util.misc;

import org.bukkit.entity.Player;

public class Validate {
   public static <T> T notNull(T o, String message) {
      if (o == null) {
         throw new NullPointerException(message);
      } else {
         return o;
      }
   }

   public static <T> T instanceOf(T o, Class<?> c, String message) {
      if (!c.isInstance(o)) {
         throw new IllegalArgumentException(message);
      } else {
         return o;
      }
   }

   public static boolean argumentCheck(boolean b, String message) {
      if (b) {
         return true;
      } else {
         throw new IllegalArgumentException(message);
      }
   }

   public static Player playerOnline(Player p, String message) {
      if (!p.isOnline()) {
         throw new IllegalStateException(message);
      } else {
         return p;
      }
   }

   public static String strNotEmpty(String s, String message) {
      if (s.replace(" ", "").length() < 1) {
         throw new IllegalArgumentException(message);
      } else {
         return s;
      }
   }

   public static int notZero(int n, String message) {
      if (n == 0) {
         throw new ArithmeticException(message);
      } else {
         return n;
      }
   }

   public static long notZero(long n, String message) {
      if (n == 0L) {
         throw new ArithmeticException(message);
      } else {
         return n;
      }
   }
}
