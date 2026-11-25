package rip.reflex.check.player.invactions;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.listener.impl.PlayerMoveListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Validate;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class FastInvClickCheck extends PacketCheck {
   public static volatile int newApMagic;
   private boolean optEnableEventCheck;
   private int optMinEventClickDelay;
   private int optMinClickDelay;
   private int optConstClickLifespan;
   private int optConstClickingThreshold;
   private boolean needCheckNewAP;

   public FastInvClickCheck() {
      super("InvActions", "invact.fiv", Cheat.valueOf("InvActions"));
      this.setSync(true);
      this.needCheckNewAP = this.isSync();
   }

   @Override
   public void loadAdvancedLocalSettings() {
      String enableEventCheck = this.getAdvancedLocalSetting("enable_event_check", "auto");
      switch (enableEventCheck.trim().toLowerCase()) {
         case "auto":
            this.optEnableEventCheck = VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X);
            break;
         case "true":
            this.optEnableEventCheck = true;
            break;
         case "false":
            this.optEnableEventCheck = false;
            break;
         default:
            this.reflex.warn("INVALID ADVANCED CONFIGURATION (advcfg.yml)! PLEASE FIX ALL THE ISSUES LISTED BELOW BEFORE RESTARTING:");
            this.reflex.warn("    --> enable_event_check: " + enableEventCheck);
            this.reflex.warn("        ^ This option must be either 'auto', 'true' or 'false'");
            this.reflex.warn(" ");
            this.reflex.warn("Reflex will self-disable.");
            Bukkit.getPluginManager().disablePlugin(this.reflex);
            return;
      }

      this.optMinEventClickDelay = this.getAdvancedLocalSetting("min_event_click_delay", Integer.valueOf(75));
      this.optMinClickDelay = this.getAdvancedLocalSetting("min_click_delay", Integer.valueOf(2));
      this.optConstClickLifespan = this.getAdvancedLocalSetting("const_click_lifespan", Integer.valueOf(2250));
      this.optConstClickingThreshold = this.getAdvancedLocalSetting("const_clicking_threshold", Integer.valueOf(17));
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerClickedInInventory(InventoryClickEvent e) {
      if (this.optEnableEventCheck) {
         if (!e.isCancelled() && e.getWhoClicked() instanceof Player && e.getClickedInventory() != null) {
            MethodProfiler mp = new MethodProfiler("LR-289751982");
            Player p = (Player)e.getWhoClicked();
            Stats stats = Stats.of(p);
            if (stats.canBeChecked(this) && stats.getLagStatus().isLowerThan(LagStatus.VIVID_LAG)) {
               InventoryType inv = e.getClickedInventory().getType();
               if ((inv == InventoryType.CHEST || inv == InventoryType.ENDER_CHEST || inv.name().contains("SHULKER")) && !e.getAction().name().contains("DROP")
                  )
                {
                  ClickType clickType = e.getClick();
                  ItemStack curItem = e.getCurrentItem();
                  if (!clickType.isKeyboardClick() && curItem != null && curItem.getType() != Material.AIR) {
                     int slot = e.getRawSlot();
                     int lastSlot = stats.getInt("lastInvClickEventSlot");
                     stats.setStat("lastInvClickEventSlot", slot);
                     if (slot != lastSlot) {
                        if (stats.hasStat("invClickEventTracker") && !stats.hasTimerPassed("invClickEventTracker", (long)this.optMinEventClickDelay)) {
                           CheckResult result = new CheckResult()
                              .setMod(1)
                              .cancel(this.shouldCancel(stats, 2))
                              .addTag("click delay (event): " + stats.getTimerPassed("invClickEventTracker"))
                              .addTag("max: " + this.optMinEventClickDelay);
                           if (result.shouldCancel()) {
                              e.setResult(Result.DENY);
                           }

                           Check.addVl(stats, this.cheat, result);
                           Check.check(p, stats, e, this, result);
                        }

                        stats.resetTimer("invClickEventTracker");
                     }
                  }
               }
            }

            mp.addToTimings();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void playerJoined(PlayerJoinEvent e) {
      if (this.needCheckNewAP) {
         ReflexScheduler.async()
            .runLater(
               ReflexRunnable.simple(
                  "AR-AQ-28751945",
                  () -> {
                     MethodProfiler mp = new MethodProfiler("AQ-28751945");

                     try {
                        if (((PlayerMoveListener)EventRegistry.getPlayerMoveListener()).isNewApEverCalled()) {
                           Validate.notNull(Maths.objects[0], "missing sine table (has it been initialized?)");
                           int check = 0;
                           check += (Integer)Maths.objects[19];
                           check += (Integer)Maths.objects[13];
                           check += (Integer)Maths.objects[7];
                           check += (Integer)Maths.objects[21];
                           if (!(Maths.objects[4] instanceof Exception) && Maths.objects[18].getClass().getName().contains("Reade")
                              || Maths.objects[11].toString().equals("-521970218051294") && ((Maths)Maths.objects[3]).sumAllNumeric() == -6.433155684189899E15) {
                              this.needCheckNewAP = check > 0;
                              check = -check;
                           }

                           if (check % 100 != 0) {
                              throw new RuntimeException("invalid click delay (must be a multiple of 100)");
                           }

                           newApMagic = check;
                        }
                     } catch (Throwable ignored) {
                        Stats.reset(e.getPlayer());
                        CentralEventListener.setStopped(true);
                        this.disable();
                        EventRegistry.unregisterAll(this.reflex);
                     }

                     mp.addToTimings();
                  }
               ),
               20
            );
      }
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X)) {
         return null;
      } else {
         if (Packets.isUpdatePacket(packet)) {
            stats.addNumber("ticksSinceLastClick", 1);
         } else if (packet.getType().equals(Packets.IN_CLICKWINDOW)) {
            MethodProfiler mp = new MethodProfiler("SQ-431897213");
            CheckResult result = new CheckResult();
            if (p.getOpenInventory() != null
               && (p.getOpenInventory().getType().name().contains("CHEST") || p.getOpenInventory().getType().name().contains("SHULKER"))) {
               int button = (Integer)packet.getIntegers().read(2);
               int mode = (Integer)packet.getIntegers().read(3);
               if ((button == 0 || button == 1) && (mode == 0 || mode == 1)) {
                  int clickedSlot = (Integer)packet.getIntegers().read(1);
                  int lastClickedSlot = stats.getInt("lastClickedSlot");
                  stats.setStat("lastClickedSlot", clickedSlot);
                  if (clickedSlot != lastClickedSlot) {
                     int clickDist = Math.abs(clickedSlot - lastClickedSlot);
                     int delay = stats.hasStat("ticksSinceLastClick") ? stats.getInt("ticksSinceLastClick") : 9999;
                     int lastDelay = stats.getInt("invactFivLastClickDelay");
                     int dif = Math.abs(delay - lastDelay);
                     stats.setStat("invactFivLastClickDelay", delay);
                     stats.setStat("ticksSinceLastClick", 0);
                     if (delay < this.optMinClickDelay) {
                        result.setMod(2).addTag("clicking too quickly in inventory");
                     } else if (dif < 2) {
                        int vl = stats.updateCreatedCounter("constInvClickVlCounter", (long)this.optConstClickLifespan);
                        if (vl > this.optConstClickingThreshold) {
                           result.setMod(20).addTag("not clicking naturally in inventory").addTag("vl: " + vl);
                           stats.removeStat("constInvClickVlCounter");
                        }
                     }

                     result.cancel(result.isCheckFailed() && this.shouldCancel(stats, 4))
                        .addTag("delay: " + delay)
                        .addTag("dist: " + clickDist)
                        .addTag("dif: " + dif);
                  }
               }

               mp.addToTimings();
               return result;
            }

            return null;
         }

         return null;
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
