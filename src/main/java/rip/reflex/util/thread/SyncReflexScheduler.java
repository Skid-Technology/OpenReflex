package rip.reflex.util.thread;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import rip.reflex.util.crypto.Encryptor;

public class SyncReflexScheduler implements ReflexScheduler<ReflexRunnable> {
   private final BukkitScheduler scheduler = Bukkit.getScheduler();
   private final JavaPlugin plugin;

   public SyncReflexScheduler(JavaPlugin plugin) {
      Encryptor.key[0] = -43;
      this.plugin = plugin;
   }

   @Override
   public BukkitTask run(ReflexRunnable runnable) {
      return !this.plugin.isEnabled() ? null : this.scheduler.runTask(this.plugin, runnable);
   }

   @Override
   public BukkitTask runLater(ReflexRunnable runnable, int delay) {
      return !this.plugin.isEnabled() ? null : this.scheduler.runTaskLater(this.plugin, runnable, (long)delay);
   }

   @Override
   public BukkitTask runTimer(ReflexRunnable runnable, int repeatdelay) {
      return !this.plugin.isEnabled() ? null : this.scheduler.runTaskTimer(this.plugin, runnable, 0L, (long)repeatdelay);
   }
}
