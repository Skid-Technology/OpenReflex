package rip.reflex.check.base;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;

public class CheckRegistry {
   private static final ConcurrentHashMap<String, Boolean> status = new ConcurrentHashMap<>();
   private static final List<Check> allChecks = new ArrayList<>();
   private static final List<Check>[] checkArray = new List[CheckType.values().length];

   public static List<Check> loadedChecksList() {
      return allChecks;
   }

   public static List<Check> getChecksOfType(CheckType type) {
      return checkArray[type.ordinal()];
   }

   public static List<Check> getActiveChecksOfType(CheckType type) {
      List<Check> checks = new ArrayList<>();

      for (Check check : allChecks) {
         if (check.isEnabled() && check.getType() == type && check.isCompEnabled()) {
            checks.add(check);
         }
      }

      return checks;
   }

   public static void registerCheck(Check check) {
      if (check.isEnabledInConfig()) {
         CheckType type = check.getType();
         registerCheck0(check, type);
      }
   }

   public static Check getCheck(Cheat cheat) {
      return allChecks.stream().filter(c -> c.getCheat() == cheat).findFirst().orElse(null);
   }

   public static void updateStatus(Check check) {
      status.put(check.getName(), check.isEnabled0());
   }

   public static boolean getStatus(Check check) {
      if (!status.containsKey(check.getName())) {
         updateStatus(check);
      }

      return status.get(check.getName());
   }

   private static void registerCheck0(Check check, CheckType type) {
      List<Check> registered = getChecksOfType(type);
      registered.add(check);
      allChecks.add(check);
      if (!status.containsKey(check.getName())) {
         Reflex.getReflex().info("Enabled check " + check.getName());
      }

      updateStatus(check);
      checkArray[type.ordinal()].add(check);
   }

   public static ConcurrentHashMap<String, Boolean> getStatus() {
      return status;
   }

   static {
      for (int i = 0; i < checkArray.length; i++) {
         checkArray[i] = new ArrayList<>();
      }
   }
}
