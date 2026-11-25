package rip.reflex.util.stat;

import org.bstats.Metrics;
import org.bstats.charts.SingleLineChart;
import rip.reflex.Reflex;
import rip.reflex.security.AntiPiracy;
import rip.reflex.util.misc.Misc;

public class StatsCollector {
   private static int kicked = 0;
   private static int banned = 0;
   private static boolean enabled = false;

   public static void collectKick() {
      kicked++;
   }

   public static void collectBan() {
      banned++;
   }

   public static void loadMetrics() {
      if (Misc.isNumeric(AntiPiracy.getResource())) {
         enabled = true;
         Metrics metrics = new Metrics(Reflex.getReflex());
         metrics.addCustomChart(new SingleLineChart("total_kicked_players") {
            @Override
            public int getValue() {
               int tempKicked = StatsCollector.kicked;
               StatsCollector.kicked = 0;
               return tempKicked;
            }
         });
         metrics.addCustomChart(new SingleLineChart("total_banned_players") {
            @Override
            public int getValue() {
               int tempBanned = StatsCollector.banned;
               StatsCollector.banned = 0;
               return tempBanned;
            }
         });
      }
   }

   public static boolean isEnabled() {
      return enabled;
   }
}
