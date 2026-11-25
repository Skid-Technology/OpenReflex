package me.darksidecode.easysockets.event;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
   private final Object registryLock = new Object();
   private final Object eventLock = new Object();
   private final List<EventListener> listeners = new ArrayList<>();

   public void registerListener(EventListener listener) {
      synchronized (this.registryLock) {
         if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
         }
      }
   }

   public void unregisterListener(EventListener listener) {
      synchronized (this.registryLock) {
         this.listeners.remove(listener);
      }
   }

   public boolean callEvent(Event e) {
      try {
         ((ArrayList<EventListener>)((ArrayList<EventListener>)this.listeners).clone()).forEach(listener -> listener.onEvent(e));
         return e.isCancelled();
      } catch (Exception ex) {
         throw new EventException("Error processing event " + e.getClass().getSimpleName() + "/" + e.getState().name(), ex);
      }
   }
}
