package rip.reflex.util.thread;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import rip.reflex.Reflex;

public class AsyncReflexScheduler implements ReflexScheduler<ReflexRunnable> {
   private final BukkitScheduler scheduler = Bukkit.getScheduler();
   private final JavaPlugin plugin;

   @Override
   public BukkitTask run(ReflexRunnable runnable) {
      if (!this.plugin.isEnabled()) {
         return null;
      } else {
         Reflex.getReflex().getExecutor().execute(runnable);
         return null;
      }
   }

   @Override
   public BukkitTask runLater(ReflexRunnable runnable, int delay) {
      if (!this.plugin.isEnabled()) {
         return null;
      } else {
         Reflex.getReflex().getExecutor().execute(() -> {
            try {
               Thread.sleep((long)(delay * 50));
            } catch (Exception ex) {
               System.err.println("I can't sleep :`(");
               ex.printStackTrace();
            }

            runnable.run();
         });
         return null;
      }
   }

   @Override
   public BukkitTask runTimer(ReflexRunnable runnable, int repeatdelay) {
      return !this.plugin.isEnabled() ? null : this.scheduler.runTaskTimerAsynchronously(this.plugin, runnable, 0L, (long)repeatdelay);
   }

   public AsyncReflexScheduler(JavaPlugin plugin) {
      this.plugin = plugin;
   }
}
