package rip.reflex.util.thread;

public class Threads {
   public static void sleepMillis(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception ex) {
         System.err.println("Can't sleep millis :`(");
         ex.printStackTrace();
      }
   }

   public static void sleepSeconds(long seconds) {
      try {
         Thread.sleep(seconds * 1000L);
      } catch (Exception e) {
         System.err.println("Can't sleep seconds :`(");
         e.printStackTrace();
      }
   }
}
