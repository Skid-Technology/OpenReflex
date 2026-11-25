package me.darksidecode.easysockets.event;

public abstract class Event {
   private final EventState state;
   private boolean cancelled;

   public boolean isPre() {
      return this.state == EventState.PRE;
   }

   public boolean isPost() {
      return this.state == EventState.POST;
   }

   public Event(EventState state) {
      this.state = state;
   }

   public EventState getState() {
      return this.state;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
