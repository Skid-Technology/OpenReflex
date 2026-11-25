package rip.reflex.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import rip.reflex.api.Cheat;

public class ReflexCommandEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Cheat cheat;
   private final String violationId;
   private String command;
   private boolean cancelled;

   public ReflexCommandEvent(Player player, Cheat cheat, String command, String violationId) {
      this.player = player;
      this.cheat = cheat;
      this.command = command;
      this.violationId = violationId;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Cheat getCheat() {
      return this.cheat;
   }

   public String getCommand() {
      return this.command;
   }

   public void setCommand(String command) {
      this.command = command;
   }

   public String getViolationId() {
      return this.violationId;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }
}
