package rip.reflex.util.thread;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public interface ReflexScheduler<T extends ReflexRunnable> {
   static ReflexScheduler<ReflexRunnable> sync() {
      return ReflexScheduler.Schedulers.sync;
   }

   static ReflexScheduler<ReflexRunnable> async() {
      return ReflexScheduler.Schedulers.async;
   }

   BukkitTask run(T task);

   BukkitTask runLater(T task, int delayTicks);

   BukkitTask runTimer(T task, int periodTicks);

   public static class Schedulers {
      private static ReflexScheduler<ReflexRunnable> sync;
      private static ReflexScheduler<ReflexRunnable> async;

      public static void init(JavaPlugin plugin) {
         sync = new SyncReflexScheduler(plugin);
         async = new AsyncReflexScheduler(plugin);
      }
   }
}
