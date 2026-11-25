package rip.reflex.util.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.util.placeholders.PlaceholderAPIHook;

public class Strings {
   private static final Random random = new Random();
   private static final String[] charlist = new String[]{
      "a",
      "b",
      "c",
      "d",
      "e",
      "f",
      "g",
      "h",
      "i",
      "j",
      "k",
      "l",
      "m",
      "n",
      "o",
      "p",
      "q",
      "r",
      "s",
      "t",
      "u",
      "v",
      "w",
      "x",
      "y",
      "z",
      "0",
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "+",
      "-",
      "=",
      ":",
      "%",
      "_",
      "/",
      "\\",
      "|",
      "*",
      ".",
      "@",
      "#",
      "$"
   };
   private static final List<byte[]> xorKeys = new ArrayList<>();

   public static String color(String s) {
      return ChatColor.translateAlternateColorCodes('&', s);
   }

   public static String random(String... source) {
      return source[random.nextInt(source.length)];
   }

   public static String randomComp(int length, String source) {
      String[] s = new String[source.length()];

      for (int i = 0; i < source.length(); i++) {
         s[i] = String.valueOf(source.charAt(i));
      }

      return random(length, s);
   }

   public static String random(int length, String... source) {
      StringBuilder s = new StringBuilder();

      while (s.length() < length) {
         s.append(random(source));
      }

      return s.toString();
   }

   public static String random(int length) {
      StringBuilder s = new StringBuilder();

      while (s.length() < length) {
         String c = charlist[random.nextInt(charlist.length)];
         if (random.nextBoolean()) {
            c = c.toUpperCase();
         }

         s.append(c);
      }

      return s.toString();
   }

   public static String randomAbc(int length) {
      StringBuilder s = new StringBuilder();

      while (s.length() < length) {
         String c = charlist[random.nextInt(charlist.length - 24)];
         if (random.nextBoolean()) {
            c = c.toUpperCase();
         }

         s.append(c);
      }

      return s.toString();
   }

   public static boolean containsAny(String s, String... containers) {
      return Arrays.stream(containers).anyMatch(container -> container.contains(s));
   }

   public static boolean equalsAny(String s, String... s0) {
      return Arrays.stream(s0).anyMatch(s::equals);
   }

   public static boolean isLowerCase(String s) {
      return s.equals(s.toLowerCase());
   }

   public static boolean isUpperCase(String s) {
      return s.equals(s.toUpperCase());
   }

   public static boolean containsIgnoreCase(String s, String container) {
      return s.toLowerCase().contains(container.toLowerCase());
   }

   public static String rformat(CommandSender cs, String s) {
      return wrap(Reflex.getReflex().getTheme(cs).getPrefix() + s).replace("%break%", "\n").replace("\\n", "\n").colourizeAndGet();
   }

   public static String rformatNoPrefix(String s) {
      return wrap(s).replace("%break%", "\n").replace("\\n", "\n").colourizeAndGet();
   }

   public static String encode(String s) {
      char[] chars = s.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         if (!String.valueOf(chars[i]).equals(" ") && !String.valueOf(chars[i]).equals("'")) {
            chars[i] = charlist[Math.abs(charlist.length - getCharIndex(chars[i])) - 1].toLowerCase().charAt(0);
         }
      }

      return new String(chars);
   }

   public static int getCharIndex(char c) {
      String s = String.valueOf(c).toLowerCase();

      for (int index = 0; index < charlist.length; index++) {
         if (s.equals(charlist[index])) {
            return index;
         }
      }

      return -1;
   }

   public static String join(int start, String[] arr, String separator) {
      if (arr.length == 0) {
         return "";
      } else if (arr.length == 1) {
         return arr[0];
      } else {
         StringBuilder builder = new StringBuilder();

         for (int i = start; i < arr.length; i++) {
            builder.append(arr[i]).append(separator);
         }

         return builder.deleteCharAt(builder.length() - 1).toString();
      }
   }

   public static String firstUppercase(String s) {
      return Character.toUpperCase(s.charAt(0)) + s.substring(1);
   }

   public static boolean startsWithAbc(String s) {
      return isStringValid(s) && Character.isAlphabetic(s.toLowerCase().charAt(0));
   }

   public static boolean isStringValid(String s) {
      return s != null || !s.isEmpty() || !s.contains("\u0000");
   }

   public static String removeColorCodes(String s) {
      char[] chars = s.toCharArray();

      for (int i = 0; i < chars.length; i++) {
         if ("&§".contains(String.valueOf(chars[i]))) {
            if (i < chars.length - 1) {
               chars[i + 1] = 0;
            }

            chars[i] = 0;
         }
      }

      return replace(new String(chars), "\u0000", "");
   }

   public static String tab(String s, int tabLoc) {
      if (s.length() >= tabLoc) {
         throw new IllegalArgumentException("String must be shorter than tab length");
      } else {
         return s + String.join("", Collections.nCopies(tabLoc - s.length(), " "));
      }
   }

   public static String randomUsername() {
      String prefix = random.nextInt(5) == 0 ? random("ITz", "Neron_", "Vildman_", "MLG", "Its", "xX", "Vag", "Mini", "kon", "md_5", "md_") : "";
      String suffix = prefix.isEmpty()
         ? random("LP", "YT", "Code", "Plays", "HD", "007", "69", "123", "Trump", "Xx", "dedes", "Digger", "solas", "md_5", "5", "_5")
         : "";
      String body = randomAbc(16 - (prefix.length() + suffix.length()) - random.nextInt((int)((double)(16 - (prefix.length() + suffix.length())) * 0.5)));
      return prefix + body + suffix;
   }

   public static int xorHash(String s) {
      return xor(s == null ? "null" : s, 38634).hashCode();
   }

   public static String xor(String s, Object key) {
      try {
         byte[] k = key instanceof byte[] ? (byte[])key : (byte[])getXorKey(key);
         byte[] bytes = s.getBytes("UTF-8");

         for (int i = 0; i < bytes.length; i++) {
            for (byte b : k) {
               bytes[i] ^= b;
            }

            bytes[i] = (byte)(24 - (bytes[i] | 1));
         }

         return new String((byte[])bytes.clone(), "UTF-8");
      } catch (Exception ignored) {
         return s;
      }
   }

   public static String replace(String str, String target, String replacement) {
      int targetLength = target.length();
      if (targetLength == 0) {
         return str;
      } else {
         int idx2 = str.indexOf(target);
         int cursor = 0;
         if (idx2 < 0) {
            return str;
         } else {
            StringBuilder buffer;
            for (buffer = new StringBuilder(targetLength > replacement.length() ? str.length() : str.length() * 2);
               idx2 > -1;
               idx2 = str.indexOf(target, cursor)
            ) {
               buffer.append(str, cursor, idx2);
               buffer.append(replacement);
               cursor = idx2 + targetLength;
            }

            buffer.append(str, cursor, str.length());
            return buffer.toString();
         }
      }
   }

   public static String removeIntegers(String s) {
      return s.replaceAll("[0-9]", "");
   }

   public static String cut(String s, int start, int end) {
      StringBuilder sb = new StringBuilder();

      for (int i = start; i < end; i++) {
         sb.append(s.charAt(i));
      }

      return sb.toString();
   }

   public static String asciiOnly(String s) {
      char[] chars = (char[])s.replaceAll("[^\\x00-\\x7F]", "").toCharArray().clone();

      for (int i = 0; i < chars.length; i++) {
         if (getCharIndex(chars[i]) == -1) {
            chars[i] = 0;
         }
      }

      return new String(chars).replace("\u0000", "");
   }

   public static Strings.WrappedString wrap(String s) {
      return new Strings.WrappedString(s);
   }

   public static Strings.WrappedString wrap(StringBuilder s) {
      return new Strings.WrappedString(s.toString());
   }

   private static Object getXorKey(Object o) {
      if (o == null) {
         return xorKeys.get(4);
      } else {
         char c;
         if (o instanceof Character) {
            c = (Character)o;
         } else if (o instanceof Integer) {
            c = (char)((Integer)o).intValue();
         } else {
            c = 22290;
         }

         if (c == 24859) {
            return xorKeys.get(3);
         } else {
            switch (c) {
               case '歳':
                  return xorKeys.get(2);
               case '雪':
                  return xorKeys.get(1);
               default:
                  return xorKeys.get(0);
            }
         }
      }
   }

   static {
      xorKeys.add(new byte[]{60, -14, 5, 9, -52, 2, 31, -6});
      xorKeys.add(new byte[]{3, 12, -39, -4, -10, 74, -9, 2});
      xorKeys.add(new byte[]{-11, 50, 17, -1, 9, 61, 6, -3});
      xorKeys.add(new byte[]{33, -76, 1, 15, 102, 4, -41, 19});
   }

   public static class WrappedString {
      private String value;

      public Strings.WrappedString replace(String target, Object replace) {
         this.value = Strings.replace(this.value, target, replace instanceof String ? (String)replace : String.valueOf(replace));
         return this;
      }

      public Strings.WrappedString replace(String target, Supplier<Object> supp) {
         return !this.value.contains(target) ? this : this.replace(target, supp.get());
      }

      public Strings.WrappedString colourize() {
         this.value = Strings.color(this.value);
         return this;
      }

      public Strings.WrappedString noColors() {
         this.value = Strings.removeColorCodes(this.value);
         return this;
      }

      public String colourizeAndGet() {
         this.colourize();
         return this.get();
      }

      public Strings.WrappedString placeholderApi(Player player) {
         this.value = PlaceholderAPIHook.replace(this.value, player);
         return this;
      }

      public String get() {
         return this.value;
      }

      @Override
      public String toString() {
         return this.value;
      }

      public WrappedString(String value) {
         this.value = value;
      }
   }
}
