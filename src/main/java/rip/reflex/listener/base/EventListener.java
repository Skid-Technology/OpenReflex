package rip.reflex.listener.base;

import org.bukkit.event.Event;
import rip.reflex.Reflex;

public abstract class EventListener {
   protected final Reflex reflex;
   private final String event;

   public abstract void onEvent(Event event);

   public EventListener(Reflex reflex, String event) {
      this.reflex = reflex;
      this.event = event;
   }

   public String getEvent() {
      return this.event;
   }
}
