package rip.reflex.listener.base;

import rip.reflex.Reflex;
import rip.reflex.listener.impl.BlockPlaceListener;
import rip.reflex.listener.impl.EntityDamageByEntityListener;
import rip.reflex.listener.impl.EntityDamageListener;
import rip.reflex.listener.impl.InventoryCloseListener;
import rip.reflex.listener.impl.InventoryOpenListener;
import rip.reflex.listener.impl.PacketListenerImpl;
import rip.reflex.listener.impl.PlayerDeathListener;
import rip.reflex.listener.impl.PlayerJoinListener;
import rip.reflex.listener.impl.PlayerLoginListener;
import rip.reflex.listener.impl.PlayerMoveListener;
import rip.reflex.listener.impl.PlayerQuitListener;
import rip.reflex.listener.impl.PlayerTeleportListener;
import rip.reflex.listener.impl.PlayerVelocityListener;

public class EventLoader {
   private static boolean loaded = false;

   public static void loadAll(Reflex reflex) {
      if (!loaded) {
         EventRegistry.setPlayerMoveListener(new PlayerMoveListener(reflex));
         EventRegistry.setEntityDamageListener(new EntityDamageListener(reflex));
         EventRegistry.setEntityDamageByEntityListener(new EntityDamageByEntityListener(reflex));
         EventRegistry.setPlayerJoinListener(new PlayerJoinListener(reflex));
         EventRegistry.setPlayerQuitListener(new PlayerQuitListener(reflex));
         EventRegistry.setBlockPlaceListener(new BlockPlaceListener(reflex));
         EventRegistry.setPlayerVelocityListener(new PlayerVelocityListener(reflex));
         EventRegistry.setPlayerLoginListener(new PlayerLoginListener(reflex));
         EventRegistry.setInventoryOpenListener(new InventoryOpenListener(reflex));
         EventRegistry.setInventoryCloseListener(new InventoryCloseListener(reflex));
         EventRegistry.setPlayerDeathListener(new PlayerDeathListener(reflex));
         EventRegistry.setPlayerTeleportListener(new PlayerTeleportListener(reflex));
         EventRegistry.setPacketListener(new PacketListenerImpl(reflex));
         loaded = true;
      }
   }
}
