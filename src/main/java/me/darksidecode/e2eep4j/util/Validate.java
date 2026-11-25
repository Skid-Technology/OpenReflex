package me.darksidecode.e2eep4j.util;

public class Validate {
   public static <T> T notNull(T obj, String msg) {
      if (obj == null) {
         throw new NullPointerException(msg);
      } else {
         return obj;
      }
   }

   public static byte[] notEmpty(byte[] arr, String msg) {
      if (arr.length == 0) {
         throw new IllegalArgumentException(msg);
      } else {
         return arr;
      }
   }

   public static <T> T[] notEmpty(T[] arr, String msg) {
      if (arr.length == 0) {
         throw new IllegalArgumentException(msg);
      } else {
         return arr;
      }
   }

   public static int multipleOf(int i, int m, String msg) {
      if (i % m == 0 && i != 0) {
         return i;
      } else {
         throw new ArithmeticException(msg + " (" + i + "%" + m + "!=0)");
      }
   }

   public static byte[] ivLength(byte[] n, String msg) {
      if (n.length != 16) {
         throw new IllegalArgumentException(msg + " (" + n.length + "!=" + 16 + ")");
      } else {
         return n;
      }
   }

   public static byte[] aesLength(byte[] n, String msg) {
      if (n.length != 16) {
         throw new IllegalArgumentException(msg + " (" + n.length + "!=" + 16 + ")");
      } else {
         return n;
      }
   }

   public static byte[] baseLength(byte[] n, String msg) {
      if (n.length != 32) {
         throw new IllegalArgumentException(msg + " (" + n.length + "!=" + 32 + ")");
      } else {
         return n;
      }
   }

   public static Object[] cvgLength(Object[] n, String msg) {
      if (n.length != 6) {
         throw new IllegalArgumentException(msg + " (" + n.length + "!=" + 6 + ")");
      } else {
         return n;
      }
   }
}
