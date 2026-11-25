package rip.reflex.listener.base;

import com.comphenix.protocol.ProtocolLibrary;
import java.util.HashSet;
import java.util.Set;
import rip.reflex.Reflex;

public class EventRegistry {
   private static EventListener playerMoveListener = null;
   private static EventListener entityDamageListener = null;
   private static EventListener entityDamageByEntityListener = null;
   private static EventListener playerJoinListener = null;
   private static EventListener playerQuitListener = null;
   private static EventListener blockPlaceListener = null;
   private static EventListener playerVelocityListener = null;
   private static EventListener playerLoginListener = null;
   private static EventListener inventoryOpenListener = null;
   private static EventListener inventoryCloseListener = null;
   private static EventListener playerDeathListener = null;
   private static EventListener playerTeleportListener = null;
   private static PacketListener packetListener = null;
   private static final Set<EventListener> eventListeners = new HashSet<>();
   private static final Set<PacketListener> packetListeners = new HashSet<>();

   public static void registerEventListener(EventListener listener) {
      eventListeners.add(listener);
   }

   public static void registerPacketListener(PacketListener listener) {
      packetListeners.add(listener);
   }

   public static Set<EventListener> getListenersOf(String event) {
      Set<EventListener> list = new HashSet<>();
      eventListeners.stream().filter(l -> l.getEvent().equals(event)).forEach(list::add);
      return list;
   }

   public static Set<PacketListener> getPacketListeners() {
      return packetListeners;
   }

   public static void unregisterAll(Reflex reflex) {
      try {
         ProtocolLibrary.getProtocolManager().removePacketListeners(reflex);
      } catch (Exception ignored) {
      }

      eventListeners.clear();
      packetListeners.clear();
      playerMoveListener = null;
      entityDamageListener = null;
      entityDamageByEntityListener = null;
      playerJoinListener = null;
      playerQuitListener = null;
      blockPlaceListener = null;
      playerVelocityListener = null;
      playerLoginListener = null;
      inventoryOpenListener = null;
      inventoryCloseListener = null;
      playerDeathListener = null;
      playerTeleportListener = null;
      packetListener = null;
   }

   public static EventListener getPlayerMoveListener() {
      return playerMoveListener;
   }

   public static void setPlayerMoveListener(EventListener playerMoveListener) {
      EventRegistry.playerMoveListener = playerMoveListener;
   }

   public static EventListener getEntityDamageListener() {
      return entityDamageListener;
   }

   public static void setEntityDamageListener(EventListener entityDamageListener) {
      EventRegistry.entityDamageListener = entityDamageListener;
   }

   public static EventListener getEntityDamageByEntityListener() {
      return entityDamageByEntityListener;
   }

   public static void setEntityDamageByEntityListener(EventListener entityDamageByEntityListener) {
      EventRegistry.entityDamageByEntityListener = entityDamageByEntityListener;
   }

   public static EventListener getPlayerJoinListener() {
      return playerJoinListener;
   }

   public static void setPlayerJoinListener(EventListener playerJoinListener) {
      EventRegistry.playerJoinListener = playerJoinListener;
   }

   public static EventListener getPlayerQuitListener() {
      return playerQuitListener;
   }

   public static void setPlayerQuitListener(EventListener playerQuitListener) {
      EventRegistry.playerQuitListener = playerQuitListener;
   }

   public static EventListener getBlockPlaceListener() {
      return blockPlaceListener;
   }

   public static void setBlockPlaceListener(EventListener blockPlaceListener) {
      EventRegistry.blockPlaceListener = blockPlaceListener;
   }

   public static EventListener getPlayerVelocityListener() {
      return playerVelocityListener;
   }

   public static void setPlayerVelocityListener(EventListener playerVelocityListener) {
      EventRegistry.playerVelocityListener = playerVelocityListener;
   }

   public static EventListener getPlayerLoginListener() {
      return playerLoginListener;
   }

   public static void setPlayerLoginListener(EventListener playerLoginListener) {
      EventRegistry.playerLoginListener = playerLoginListener;
   }

   public static EventListener getInventoryOpenListener() {
      return inventoryOpenListener;
   }

   public static void setInventoryOpenListener(EventListener inventoryOpenListener) {
      EventRegistry.inventoryOpenListener = inventoryOpenListener;
   }

   public static EventListener getInventoryCloseListener() {
      return inventoryCloseListener;
   }

   public static void setInventoryCloseListener(EventListener inventoryCloseListener) {
      EventRegistry.inventoryCloseListener = inventoryCloseListener;
   }

   public static EventListener getPlayerDeathListener() {
      return playerDeathListener;
   }

   public static void setPlayerDeathListener(EventListener playerDeathListener) {
      EventRegistry.playerDeathListener = playerDeathListener;
   }

   public static EventListener getPlayerTeleportListener() {
      return playerTeleportListener;
   }

   public static void setPlayerTeleportListener(EventListener playerTeleportListener) {
      EventRegistry.playerTeleportListener = playerTeleportListener;
   }

   public static PacketListener getPacketListener() {
      return packetListener;
   }

   public static void setPacketListener(PacketListener packetListener) {
      EventRegistry.packetListener = packetListener;
   }
}
