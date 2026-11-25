package rip.reflex.util.debugging;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.misc.Strings;

public class Debugger {
   private static final PrintStream orig = System.out;

   public static void debugIfUsed(Reflex r, String s) {
      if (r.getSetting("general.debugStart", false)) {
         System.out.println(s);
      }
   }

   public static void debugIf(String debugMessage, String configEntry) {
      if (Reflex.getReflex().getSetting(configEntry, false)) {
         System.out.println("DEBUG | " + debugMessage);
      }
   }

   public static void debugBooleans(Player p, boolean... booleen) {
      if (p.isOp() && booleen.length >= 2) {
         StringBuilder s = new StringBuilder();

         for (boolean b : booleen) {
            s.append(b ? "&a" : "&c").append(b).append("&7/");
         }

         p.sendMessage(ChatColor.translateAlternateColorCodes('&', s.deleteCharAt(s.length() - 1).toString()));
      }
   }

   public static void debugNums(Player p, Number... nums) {
      if (p.isOp() && nums.length >= 2) {
         StringBuilder s = new StringBuilder();

         for (Number num : nums) {
            s.append("&f").append(num).append("&7/");
         }

         p.sendMessage(ChatColor.translateAlternateColorCodes('&', s.deleteCharAt(s.length() - 1).toString()));
      }
   }

   public static void debugNumberStorage(Player p, String label, NumberStorage ns, int precision) {
      if (p.isOp()) {
         StringBuilder s = new StringBuilder(label + ": ");
         ns.forEach(
            n -> {
               String nStr = n instanceof Double
                  ? String.format("%." + precision + "f", (Double)n)
                  : (n instanceof Float ? String.format("%." + precision + "f", (Float)n) : String.valueOf(n));
               s.append("§r, §c").append(nStr);
            }
         );
         String debug = Strings.replace(s.toString(), ": §r,", "");
         p.sendMessage(debug);
      }
   }

   public static void debugDoubleList(Player p, String label, List<Double> list, int precision) {
      if (p.isOp()) {
         StringBuilder s = new StringBuilder(label + ": ");
         list.forEach(n -> s.append("§r, §c").append(String.format("%." + precision + "f", n)));
         String debug = Strings.replace(s.toString(), ": §r,", "");
         p.sendMessage(debug);
      }
   }

   public static void debugSysOut() {
      System.setOut(new PrintStream(System.out) {
         @Override
         public void println(boolean x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(char x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(int x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(long x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(float x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(double x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(char[] x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + Arrays.toString(x));
         }

         @Override
         public void println(String x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }

         @Override
         public void println(Object x) {
            super.println("Called from: " + Debugger.getCallerClass() + "\n" + x);
         }
      });
   }

   public static void disableSysOutDebug() {
      System.setOut(orig);
   }

   public static String getCallerClass() {
      return Thread.currentThread().getStackTrace()[3].toString();
   }
}
