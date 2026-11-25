package rip.reflex.util.loop;

import org.bukkit.Bukkit;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.impl.PlayerLoop;
import rip.reflex.util.loop.impl.StatsLoop;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class LoopHelper {
   public static void loopPlayers(PlayerLoop loop) {
      Bukkit.getOnlinePlayers().forEach(loop::run);
   }

   public static void loopStats(StatsLoop loop) {
      if (loop != null) {
         Bukkit.getOnlinePlayers().forEach(p -> loop.run(Stats.of(p)));
      }
   }

   public static void loopPermitted(PlayerLoop loop, String permission) {
      if (loop != null && permission != null) {
         Bukkit.getOnlinePlayers().stream().filter(p -> Stats.of(p).hasPermission(permission)).forEach(loop::run);
      }
   }

   public static void loopPlayersEvery(PlayerLoop loop, int ticks) {
      if (loop != null && ticks >= 1) {
         ReflexScheduler.async().runTimer(ReflexRunnable.simple("PlayerLoop" + Strings.random(5), () -> loopPlayers(loop)), ticks);
      }
   }

   public static void loopStatsEvery(StatsLoop loop, int ticks) {
      if (loop != null && ticks >= 1) {
         ReflexScheduler.async().runTimer(ReflexRunnable.simple("StatLoop" + Strings.random(5), () -> loopStats(loop)), ticks);
      }
   }
}
