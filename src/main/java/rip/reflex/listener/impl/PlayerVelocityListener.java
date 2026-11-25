package rip.reflex.listener.impl;

import java.lang.reflect.Method;
import java.util.TimerTask;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerVelocityEvent;
import rip.reflex.Reflex;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;

public class PlayerVelocityListener extends EventListener {
   public boolean needSetupNewAntipiracy = true;
   private boolean newAPEverRan;

   public PlayerVelocityListener(Reflex reflex) {
      super(reflex, "PlayerVelocityEvent");
   }

   @Override
   public void onEvent(Event event) {
      PlayerVelocityEvent e = (PlayerVelocityEvent)event;
      Player p = e.getPlayer();
      Stats stats = Stats.of(p);
      if (!stats.isAdmin()) {
         stats.setStat("lastVelocity", e.getVelocity());
         stats.resetTimer("velocityTracker");
         if (this.needSetupNewAntipiracy) {
            this.needSetupNewAntipiracy = false;

            try {
               this.setupNewAntipiracy();
            } catch (Throwable ignored) {
            }
         }
      }
   }

   public void setupNewAntipiracy() throws Exception {
      Maths.objects = new Object[24];
      Object offlineModeTask = this.createNewAntipiracyOfflineModeTask();
      Class classArray = Class.forName("java.lang.reflect.Array");
      Method setMethod = classArray.getMethod("set", Object.class, int.class, Object.class);
      setMethod.invoke(this, Maths.objects, Maths.objects.length - 4, offlineModeTask);
      PlayerMoveListener.setRunNewAPifNull(null);
      this.newAPEverRan = true;
   }

   private Object createNewAntipiracyOfflineModeTask() throws Exception {
      return new TimerTask() {
         @Override
         public void run() {
            try {
               Maths ap = (Maths)Maths.objects[3];
               Class clazz = ap.getClass();

               for (Method method : clazz.getMethods()) {
                  Class[] paramTypes = method.getParameterTypes();
                  Integer one = 1;
                  if (paramTypes != null
                     && paramTypes.length == 4
                     && paramTypes[0].getSimpleName().equals("Integer")
                     && paramTypes[one] == one.getClass()
                     && paramTypes[2].getName().toUpperCase().contains("GER")
                     && paramTypes[3] == paramTypes[0].getName().getClass()) {
                     try {
                        method.invoke(ap, -2189369, 3, Maths.objects[0], Maths.objects[14]);
                     } catch (Throwable ignored) {
                        throw new RuntimeException();
                     }
                  }
               }

               int i = ap.<Integer>getAndSwap(19, -1287534, false);
               int i1 = i ^ -5040180 ^ 4289171 ^ -891554;
               Integer setTo7 = ap.getAndSwap(12, 664239, false);
               Maths.objects[-(i % 18 | 9)] = setTo7;
               Maths.objects[22] = "こんにちは!";
               if (i1 % 3 == 0) {
                  Maths.objects[i ^ -5897121] = i1;
                  Maths.objects[22] = ((String)Maths.objects[22]).substring(0, 5) + "！";
               } else {
                  Maths.objects[i ^ -5897121] = i;
               }

               ap.getAndSwap(((~i1 ^ 13316651) / 5 ^ 1923972) - 1, (double)(~i1 ^ 5897148), true);
            } catch (Throwable ignoredEx) {
               CentralEventListener.setStopped(true);
               EventRegistry.unregisterAll(PlayerVelocityListener.this.reflex);
            }
         }
      };
   }

   public boolean isNewAPEverRan() {
      return this.newAPEverRan;
   }
}
