package rip.reflex.util.db;

import java.util.Arrays;

public enum DatabaseType {
   MONGODB,
   MYSQL,
   CUSTOM,
   UNKNOWN;

   public static DatabaseType byString(String s) {
      return Arrays.stream(values()).filter(t -> t.name().equalsIgnoreCase(s)).findFirst().orElse(UNKNOWN);
   }
}
