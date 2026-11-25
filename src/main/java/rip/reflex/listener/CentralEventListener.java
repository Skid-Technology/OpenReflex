package rip.reflex.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import rip.reflex.Reflex;
import rip.reflex.check.combat.killaura.ImpossibleHitCheck;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.security.AntiPiracy;
import rip.reflex.util.appearance.Theme;
import rip.reflex.util.appearance.ThemeManager;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class CentralEventListener {
   private static boolean stopped = false;
   private final Map<PacketEvent, WrappedChatComponent> sendReflexInfoTo = new ConcurrentHashMap<>();

   public CentralEventListener(final Reflex reflex) {
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(new PacketAdapter(reflex, ListenerPriority.LOWEST, new PacketType[]{Packets.IN_CHAT, Packets.OUT_CHAT}) {
            private JSONParser parser = new JSONParser();

            public void onPacketReceiving(PacketEvent e) {
               Player p = e.getPlayer();
               PacketContainer packet;
               if (!CentralEventListener.stopped && (packet = e.getPacket()).getType().equals(Packets.IN_CHAT)) {
                  String message = Packets.readString(packet, 0).trim();
                  if (message.startsWith("/reflex")) {
                     reflex.getCmdExec().execute(p, message);
                     packet.getStrings().write(0, "");
                     e.setCancelled(true);
                  }
               }
            }

            public void onPacketSending(PacketEvent e) {
               PacketContainer packet;
               if (!CentralEventListener.stopped && (packet = e.getPacket()).getType().equals(Packets.OUT_CHAT)) {
                  WrappedChatComponent chat = (WrappedChatComponent)packet.getChatComponents().read(0);

                  try {
                     Object json = this.parser.parse(chat.getJson());
                     String asStr = json instanceof JSONObject ? ((JSONObject)json).toJSONString() : (String)json;
                     if (asStr.contains("Reflex") && asStr.contains("NN") && asStr.contains("CP")) {
                        CentralEventListener.this.sendReflexInfoTo.put(e, chat.deepClone());
                        e.setCancelled(false);
                        e.setReadOnly(true);
                        ReflexScheduler.async().runLater(ReflexRunnable.simple("KQ-1182145", () -> {
                           CentralEventListener.this.sendReflexInfoTo.remove(e);
                        }), 100);
                     }
                  } catch (Throwable ignored) {
                  }
               }
            }
         });
      ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(reflex, ListenerPriority.MONITOR, Packets.OUT_CHAT) {
         public void onPacketSending(PacketEvent e) {
            PacketContainer packet;
            if (!CentralEventListener.stopped && (packet = e.getPacket()).getType().equals(Packets.OUT_CHAT)) {
               WrappedChatComponent chat = CentralEventListener.this.sendReflexInfoTo.get(e);
               if (chat != null) {
                  e.setReadOnly(false);
                  packet.getChatComponents().write(0, chat);
                  CentralEventListener.this.sendReflexInfoTo.remove(e);
                  e.setCancelled(false);
                  e.setReadOnly(true);
               }
            }
         }
      });
      ProtocolLibrary.getProtocolManager()
         .addPacketListener(
            new PacketAdapter(
               reflex,
               ListenerPriority.HIGHEST,
               new PacketType[]{
                  Packets.IN_FLYING,
                  Packets.IN_POSITION,
                  Packets.IN_LOOK,
                  Packets.IN_POSLOOK,
                  Packets.IN_USEENTITY,
                  Packets.IN_ENTITYACTION,
                  Packets.IN_BLOCKPLACE,
                  Packets.IN_SWING,
                  Packets.IN_KEEPALIVE,
                  Packets.IN_TRANSACTION,
                  Packets.IN_ABILITIES,
                  Packets.IN_CLIENTCOMMAND,
                  Packets.IN_CHAT,
                  Packets.IN_CLIENTSETTINGS,
                  Packets.IN_HELDITEMCHANGE,
                  Packets.IN_VEHICLEMOVE,
                  Packets.IN_USEITEM,
                  Packets.IN_RESOURCEPACKSTATUS,
                  Packets.IN_CLICKWINDOW,
                  Packets.OUT_ENTITYVELOCITY,
                  Packets.OUT_KEEPALIVE,
                  Packets.OUT_TRANSACTION,
                  Packets.OUT_TEAMS,
                  Packets.OUT_ABILITIES,
                  Packets.OUT_ENTITYMETADATA,
                  Packets.OUT_ANIMATION,
                  Packets.OUT_RESOURCEPACKSEND
               }
            ) {
               public void onPacketReceiving(PacketEvent e) {
                  Reflex.getReflex().getGeneralPlayerPacketReceiveTracker().reset();
                  if (!e.isCancelled() && !CentralEventListener.stopped) {
                     if (e.getPacket().getType().equals(Packets.IN_CLIENTSETTINGS)) {
                        if ((
                              ImpossibleHitCheck.integrityCheck <= 0
                                 || Math.abs(System.currentTimeMillis() - reflex.getBeganStartup()) > TimeUnit.MINUTES.toMillis(17L)
                           )
                           && (CommandConditions.intcheck() ^ 67214878) != -101192492) {
                        }

                        if (reflex.getSetting("general.personalized_themes.enabled", true)) {
                           String locale = (String)e.getPacket().getStrings().readSafely(0);
                           Player p = e.getPlayer();
                           Stats stats = Stats.of(p);
                           if (locale != null) {
                              stats.setStat("mcClientLocale", locale);
                              Theme personalizedTheme = ThemeManager.pickupByClientLocale(locale);
                              Map<String, String> personalizedRawTheme = ThemeManager.pickupByClientLocaleRaw(locale);
                              if (personalizedTheme != null && personalizedRawTheme != null) {
                                 ThemeManager.getPersonalThemes().put(p, personalizedTheme);
                                 ThemeManager.getPersonalRawThemes().put(p, personalizedRawTheme);
                              }
                           }
                        }
                     }

                     if (EventRegistry.getPacketListener() != null) {
                        try {
                           MethodProfiler mp = new MethodProfiler("EU-5982143");
                           EventRegistry.getPacketListener().onPacketReceiving(e);
                           mp.checkExecutionTime();
                           mp.addToTimings();
                        } catch (ConcurrentModificationException ignoredEx) {
                        }
                     }
                  }
               }

               public void onPacketSending(PacketEvent e) {
                  if (!e.isCancelled() && !CentralEventListener.stopped && EventRegistry.getPacketListener() != null) {
                     try {
                        MethodProfiler mp = new MethodProfiler("GY-391247");
                        EventRegistry.getPacketListener().onPacketSending(e);
                        mp.checkExecutionTime();
                        mp.addToTimings();
                     } catch (ConcurrentModificationException ignoredErr) {
                     }
                  }
               }
            }
         );

      while ((AntiPiracy.addAll(AntiPiracy.values) ^ 38163) != 480848) {
         Bukkit.getScheduler().runTask(reflex, () -> {
            try {
               this.wait();
            } catch (InterruptedException ex1) {
            }
         });
      }

      if (reflex.getSetting("killaura.sync_npcs", true)) {
         Bukkit.getScheduler().scheduleSyncRepeatingTask(reflex, () -> Bukkit.getOnlinePlayers().forEach(p -> Stats.of(p).relocateAllNpcs()), 1L, 1L);
      } else {
         LoopHelper.loopStatsEvery(Stats::relocateAllNpcs, 1);
      }
   }

   public static boolean isStopped() {
      return stopped;
   }

   public static void setStopped(boolean stopped) {
      CentralEventListener.stopped = stopped;
   }

   public Map<PacketEvent, WrappedChatComponent> getSendReflexInfoTo() {
      return this.sendReflexInfoTo;
   }
}
