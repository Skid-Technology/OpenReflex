package rip.reflex.util.mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import rip.reflex.Reflex;

public class DependencyChecker {
   private static final Map<String, DependencyChecker.Checker> entries = new HashMap<>();

   public static void put(String s, DependencyChecker.Checker c) {
      entries.put(s, c);
   }

   public static void checkDependencies() {
      List<String> missing = new ArrayList<>();
      entries.forEach((name, checker) -> {
         try {
            checker.check();
         } catch (Exception ignored) {
            missing.add(name);
         }
      });
      if (!missing.isEmpty()) {
         System.err.println();
         System.err.println("***************** ERROR *****************");
         System.err.println();
         System.err.println(" The following dependencies are missing:");
         System.err.println(" >> " + String.join(", ", missing));
         System.err.println();
         System.err.println("*****************************************");
         System.err.println();
         Bukkit.getPluginManager().disablePlugin(Reflex.getReflex());
         throw new DependencyChecker.EmptyException();
      }
   }

   public interface Checker {
      boolean check() throws Exception;
   }

   private static class EmptyException extends RuntimeException {
      private EmptyException() {
      }

      @Override
      public StackTraceElement[] getStackTrace() {
         return new StackTraceElement[0];
      }

      @Override
      public synchronized Throwable fillInStackTrace() {
         return null;
      }
   }
}
