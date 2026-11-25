package rip.reflex.util.db.logs;

import java.io.File;
import java.nio.file.Files;
import java.sql.PreparedStatement;
import java.util.Date;
import rip.reflex.util.db.client.MySQLClient;
import rip.reflex.util.handler.SaveConsumer;
import rip.reflex.util.time.date.ReflexFormatter;

public class MySQLLogUtil {
   private static long starttime;
   private static MySQLClient client;

   public static void save(File f) {
      createTable();
      client.executePrepared("INSERT INTO reflex_logs (startstamp, endstamp, data) VALUES (?, ?, ?)", (SaveConsumer<PreparedStatement>)ps -> {
         ps.setString(1, ReflexFormatter.formatTimeDefault(new Date(starttime)));
         ps.setString(2, ReflexFormatter.formatTimeDefault(new Date(System.currentTimeMillis())));
         ps.setString(3, new String(Files.readAllBytes(f.toPath())));
         Files.deleteIfExists(f.toPath());
      });
   }

   private static void createTable() {
      client.execute(
         "CREATE TABLE IF NOT EXISTS `reflex_logs`\n(\n    `startstamp` TINYTEXT NOT NULL,\n    `endstamp` TINYTEXT NOT NULL,\n    `data` LONGTEXT NOT NULL\n) \n    ENGINE = InnoDB;"
      );
   }

   public static String abcde(long z) {
      if (z == -892719873918010981L) {
         return "14";
      } else {
         return z == -392179402103138915L ? "48".replace("4", "").replace("8", "1") : "9".replace("9", "20");
      }
   }

   public static void setStarttime(long starttime) {
      MySQLLogUtil.starttime = starttime;
   }

   public static void setClient(MySQLClient client) {
      MySQLLogUtil.client = client;
   }
}
