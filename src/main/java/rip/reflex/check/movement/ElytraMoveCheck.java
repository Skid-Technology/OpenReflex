package rip.reflex.check.movement;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.util.data.Stats;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Items;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;

public class ElytraMoveCheck extends PacketCheck {
   private boolean optUnequipElytra = this.getLocalSetting("unequip_elytra", Boolean.valueOf(true));
   private int optSetbacksToUnequip;
   private int optSetbackLifespan;

   public ElytraMoveCheck() {
      super("ElytraMove", "eltr.mv", Cheat.valueOf("ElytraMove"));
      this.setupVlSubtractionThread(3, 1);
   }

   @Override
   public void loadAdvancedLocalSettings() {
      this.optSetbacksToUnequip = this.getAdvancedLocalSetting("setbacks_to_unequip", Integer.valueOf(12));
      this.optSetbackLifespan = this.getAdvancedLocalSetting("setback_lifespan", Integer.valueOf(3000));
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void entityToggleGlide(EntityToggleGlideEvent e) {
      if (!e.isCancelled() && e.getEntity() instanceof Player) {
         MethodProfiler mp = new MethodProfiler("IQ-795792183");
         Player p = (Player)e.getEntity();
         Stats stats = Stats.of(p);
         if (p.getGameMode() == GameMode.SURVIVAL || p.getGameMode() == GameMode.ADVENTURE) {
            Location loc = p.getLocation().clone();
            boolean gliding = e.isGliding();
            if (gliding) {
               stats.setStat("elytraGlidingBeginLoc", loc);
            } else {
               stats.setStat("ticksElytraGliding", 0);
               stats.removeStat("eltrMvLastRatio");
               stats.removeStat("eltrMvLastFallenDownBlocks");
               Block blockIn = loc.getBlock();
               Block blockBeneath = blockIn.getRelative(BlockFace.DOWN);
               Block blockFarBelow = blockBeneath.getRelative(BlockFace.DOWN);
               long chestChangePassed = stats.getTimerPassed("eltrMvChestplateClickTracker");
               boolean inMidair = Items.allOfType(Material.AIR, blockIn, blockBeneath, blockFarBelow);
               boolean didNotChangeChestplateRecently = chestChangePassed > 2750L;
               if (inMidair && didNotChangeChestplateRecently) {
                  CheckResult result = new CheckResult()
                     .setMod(3)
                     .addTag("moved unexpectedly")
                     .addTag("ccps: " + (chestChangePassed >= 10000L ? "-1.0" : Maths.round((double)chestChangePassed / 1000.0, 3)))
                     .cancel(this.shouldCancel(stats, 5));
                  if (result.shouldCancel()) {
                     this.setbackOrTakeOffElytra(p, stats, result);
                  }

                  Check.addVl(stats, this.cheat, result);
                  Check.check(p, stats, null, this, result);
               }
            }

            stats.setStat("elytraGliding", gliding);
            mp.addToTimings();
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerInvClick(InventoryClickEvent e) {
      boolean clickedChestplate = e.getRawSlot() == 6;
      if (e.getWhoClicked() instanceof Player && clickedChestplate) {
         Player p = (Player)e.getWhoClicked();
         Stats stats = Stats.of(p);
         stats.resetTimer("eltrMvChestplateClickTracker");
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void playerInteract(PlayerInteractEvent e) {
      if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getMaterial().name().contains("FIREWORK")) {
         Player p = e.getPlayer();
         Stats stats = Stats.of(p);
         stats.resetTimer("eltrMvFireworkUseTracker");
      }
   }

   @Override
   public CheckResult checkIn(Player p, Stats stats, PacketContainer packet, SharedPacketData data) {
      if (stats.hasStat("eltrMvFireworkUseTracker") && !stats.hasTimerPassed("eltrMvFireworkUseTracker", 3000L)) {
         return null;
      } else if (stats.getBoolean("elytraGliding") && stats.hasTimerPassed("teleportTracker", 1000L)) {
         if (!packet.getType().equals(Packets.IN_POSITION) && !packet.getType().equals(Packets.IN_POSLOOK)) {
            return null;
         } else {
            MethodProfiler mp = new MethodProfiler("EQ-2183798");
            CheckResult result = new CheckResult();
            int vl = 0;
            if (stats.hasSlowFallingEffect()) {
               return null;
            } else {
               double movedX = data.getMovedX();
               double movedY = data.getMovedY();
               double movedZ = data.getMovedZ();
               double yaw;
               double pitch;
               if (packet.getType().equals(Packets.IN_POSLOOK)) {
                  yaw = (double)data.getYaw();
                  pitch = (double)data.getPitch();
               } else {
                  yaw = stats.getDouble("pckOldYaw");
                  pitch = stats.getDouble("pckOldPitch");
               }

               if (yaw == 0.0 && pitch == 0.0) {
                  return null;
               } else {
                  Block blockIn = p.getLocation().clone().getBlock();
                  Block blockBeneath = blockIn.getRelative(BlockFace.DOWN);
                  Block blockFarBelow = blockBeneath.getRelative(BlockFace.DOWN);
                  boolean inMidair = Items.allOfType(Material.AIR, blockIn, blockBeneath, blockFarBelow);
                  if (inMidair) {
                     double movedTotal = Math.sqrt(Maths.square(movedX) + Maths.square(movedY) + Maths.square(movedZ));
                     if (movedY == 0.0) {
                        if (stats.hasTimerPassed("eltrMvSetbackTracker", 2000L)) {
                           vl += 2;
                           result.addTag("moved too accurately");
                        }
                     } else if (movedY < 0.0) {
                        double risenUp = stats.getDouble("eltrMvRisenUpBlocks");
                        stats.addNumber("eltrMvFallenDownBlocks", -movedY);
                        stats.setStat("eltrMvRisenUpBlocks", 0.0);
                        if (risenUp != 0.0) {
                           double lastFallenDown = stats.getDouble("eltrMvLastFallenDownBlocks");
                           double ratio = Maths.safeFloatingPoint(risenUp / lastFallenDown, 0.0);
                           double lastRatio = stats.getDouble("eltrMvLastRatio");
                           double relativeRatio = Maths.safeFloatingPoint(ratio / lastRatio, 0.0);
                           stats.setStat("eltrMvLastRatio", ratio);
                           if (ratio > 0.64) {
                              vl += 2;
                              result.addTag("fall/rise ratio: " + ratio);
                           }

                           if (relativeRatio > 1.78) {
                              vl += 3;
                              result.addTag("relative: " + relativeRatio);
                           }
                        }
                     } else {
                        double risenUp = stats.addNumber("eltrMvRisenUpBlocks", movedY);
                        double fallenDown = stats.getDouble("eltrMvFallenDownBlocks");
                        if (fallenDown != 0.0) {
                           stats.setStat("eltrMvFallenDownBlocks", 0.0);
                           stats.setStat("eltrMvLastFallenDownBlocks", fallenDown);
                        } else {
                           double lastFallenDownx = stats.getDouble("eltrMvLastFallenDownBlocks");
                           double ratiox = Maths.safeFloatingPoint(risenUp / lastFallenDownx, 1.0);
                           if (ratiox > 0.8) {
                              vl += 2;
                              result.addTag("tried to fly too high").addTag("r: " + ratiox).addTag("lfd: " + lastFallenDownx).addTag("ru: " + risenUp);
                           }
                        }
                     }

                     double rotY = Maths.to180angle(pitch);
                     double fallenDownBlocks = stats.getDouble("eltrMvFallenDownBlocks");
                     double vRelative = Math.abs(Maths.safeFloatingPoint(movedTotal / (rotY * fallenDownBlocks), 0.0));
                     if (vRelative > 0.01) {
                        int u = stats.updateCreatedCounter("eltrMvSpeedLimitExceessCounter", 2250L);
                        if (u > 27) {
                           vl += 4;
                           stats.removeStat("eltrMvSpeedLimitExceessCounter");
                           result.addTag("relative velocity: " + vRelative).addTag("ry: " + rotY).addTag("fdb: " + fallenDownBlocks);
                        }
                     }

                     if (vl > 0) {
                        result.setMod(vl);
                        if (this.shouldCancel(stats, 5)) {
                           this.setbackOrTakeOffElytra(p, stats, result);
                        }
                     }
                  }

                  mp.addToTimings();
                  return result;
               }
            }
         }
      } else {
         return null;
      }
   }

   private void setbackOrTakeOffElytra(Player p, Stats stats, CheckResult result) {
      Location where = stats.getLocation("elytraGlidingBeginLoc");
      if (where != null) {
         stats.resetTimer("eltrMvSetbackTracker");
         stats.syncTp(where);
      }

      if (this.optUnequipElytra) {
         int recentSetbacks = stats.updateCreatedCounter("eltrMvSetbacksCounter", (long)this.optSetbackLifespan);
         result.addTag("recent setbacks: " + recentSetbacks);
         if (recentSetbacks > this.optSetbacksToUnequip) {
            ItemStack chestplate = p.getInventory().getChestplate();
            if (chestplate != null && chestplate.getType() == Material.ELYTRA) {
               p.getInventory().setChestplate(null);
               p.getInventory().addItem(new ItemStack[]{chestplate.clone()});
               result.addTag("unequipped their elytra");
            }
         }
      }
   }

   @Override
   public CheckResult checkOut(Player p, Stats stats, PacketContainer packet) {
      return null;
   }
}
