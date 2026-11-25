package rip.reflex.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReflexLoadEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final String version;
   private final int apiVersion;

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public ReflexLoadEvent(String version, int apiVersion) {
      this.version = version;
      this.apiVersion = apiVersion;
   }

   public String getVersion() {
      return this.version;
   }

   public int getApiVersion() {
      return this.apiVersion;
   }
}
