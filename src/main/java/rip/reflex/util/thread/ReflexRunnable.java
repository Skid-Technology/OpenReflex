package rip.reflex.util.thread;

import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import rip.reflex.Reflex;
import rip.reflex.util.performance.timings.ReflexTimings;
import rip.reflex.util.performance.timings.TimingsEntry;

public abstract class ReflexRunnable implements Runnable, AutoCloseable {
   private static final SplittableRandom idGen = new SplittableRandom(System.currentTimeMillis());
   private String id;
   private String name;
   private long lastTakenTime = -1L;
   private Boolean isPrimaryThread;

   public ReflexRunnable(String name) {
      this.name = name;
      this.id = name + " | " + idGen.nextInt();
   }

   public static ReflexRunnable simple(String name, final Runnable r) {
      return new ReflexRunnable(name) {
         @Override
         public void execute() throws Exception {
            r.run();
         }
      };
   }

   @Override
   public void run() {
      this.newExecutionID();
      long beginTime = System.currentTimeMillis();

      try {
         this.execute();
      } catch (Exception ex) {
         Reflex.getReflex().warn("An error occurred executing Reflex runnable with id '" + this.id + "'");
         this.onException(ex);
      }

      long endTime = System.currentTimeMillis();
      long took = endTime - beginTime;
      ReflexTimings.addEntry(new TimingsEntry(this.name + " (" + (this.isAsync() ? "A" : "S") + "RR)", took));
   }

   public void onException(Exception e) {
      e.printStackTrace();
   }

   private void newExecutionID() {
      this.id = this.name + " | " + idGen.nextInt();
   }

   public boolean isAsync() {
      if (this.isPrimaryThread == null) {
         this.isPrimaryThread = Bukkit.isPrimaryThread();
      }

      return !this.isPrimaryThread;
   }

   public abstract void execute() throws Exception;

   @Override
   public void close() throws Exception {
   }

   public String getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public long getLastTakenTime() {
      return this.lastTakenTime;
   }

   public Boolean getIsPrimaryThread() {
      return this.isPrimaryThread;
   }
}
