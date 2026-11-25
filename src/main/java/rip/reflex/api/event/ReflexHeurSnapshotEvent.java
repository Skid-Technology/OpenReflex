package rip.reflex.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReflexHeurSnapshotEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final boolean legit;
   private final String snapshotID;
   private final byte[] body;

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public ReflexHeurSnapshotEvent(Player player, boolean legit, String snapshotID, byte[] body) {
      this.player = player;
      this.legit = legit;
      this.snapshotID = snapshotID;
      this.body = body;
   }

   public Player getPlayer() {
      return this.player;
   }

   public boolean isLegit() {
      return this.legit;
   }

   public String getSnapshotID() {
      return this.snapshotID;
   }

   public byte[] getBody() {
      return this.body;
   }
}
