package rip.reflex.listener.impl;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.ClientCommand;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.check.base.CheckType;
import rip.reflex.check.base.impl.PacketCheck;
import rip.reflex.check.base.impl.WorldCheck;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.listener.base.PacketListener;
import rip.reflex.util.data.Stats;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.location.LocationUtils;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.compatibility.Compatibility;
import rip.reflex.util.protocol.compatibility.CompatibilityProvider;
import rip.reflex.util.protocol.comptypes.CompPlayerAction;
import rip.reflex.util.protocol.mcaccess.BoundingBox;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.packet.SharedPacketData;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class PacketListenerImpl extends PacketListener {
   public PacketListenerImpl(Reflex reflex) {
      super(reflex);
   }

   @Override
   public void onPacketReceiving(final PacketEvent e) {
      if (!e.isCancelled()) {
         final Player p;
         final Stats stats;
         World w;
         try {
            p = e.getPlayer();
            stats = Stats.of(p);
            w = p.getWorld();
         } catch (NullPointerException ignored) {
            return;
         }

         if (!stats.isAdmin()) {
            CompatibilityProvider cp = VersionUtils.getCompatibilityProvider();
            MethodProfiler mpLpCalc = new MethodProfiler("IO-94721984");
            if (stats.initLPcalc() && Packets.isUpdatePacket(e.getPacket())) {
               stats.addInCreatedNumberStorage("lp_tickDelays", 20, stats.getTimerPassed("lp_tickTracker"));
               stats.resetTimer("lp_tickTracker");
            }

            mpLpCalc.addToTimings();
            MethodProfiler mpSprintSneak = new MethodProfiler("YQ-6581203");
            if (e.getPacketType().equals(Packets.IN_ENTITYACTION)) {
               CompPlayerAction action = CompPlayerAction.byPacket(e.getPacket());
               if (action == CompPlayerAction.START_SPRINTING) {
                  stats.setStat("sprinting", true);
               } else if (action == CompPlayerAction.STOP_SPRINTING) {
                  stats.setStat("sprinting", false);
               }

               if (action == CompPlayerAction.START_SNEAKING) {
                  stats.setStat("sneaking", true);
               } else if (action == CompPlayerAction.STOP_SNEAKING) {
                  stats.setStat("sneaking", false);
               }

               if (Packets.isStartSneakingPacket(e.getPacket())) {
                  stats.resetTimer("sneakStartTracker");
               }
            }

            mpSprintSneak.addToTimings();
            MethodProfiler mpTickCount = new MethodProfiler("ZK-48921951");
            if (Packets.isUpdatePacket(e.getPacket())) {
               stats.setStat("clientTickCounter", stats.getStat("clientTickCounter", int.class) + 1);
               stats.setStat("totalTickCounter", stats.getStat("totalTickCounter", int.class) + 1);
               if (stats.getStat("clientTickCounter", int.class) == 20) {
                  stats.setStat("clientTickCounter", 0);
                  stats.setStat("hasSecondTicked", true);
               } else {
                  stats.setStat("hasSecondTicked", false);
               }

               stats.addNumber("ticksSincePlace", 1);
            }

            mpTickCount.addToTimings();
            if (e.getPacket().getType().equals(Packets.IN_HELDITEMCHANGE)) {
               int slot = (Integer)e.getPacket().getIntegers().read(0);
               ItemStack item = p.getInventory().getItem(slot);
               stats.setStat("lastHeldItem", item == null ? Material.AIR : item.getType());
            }

            MethodProfiler mpBlockPlace = new MethodProfiler("PB-5871923");
            Cheat blockActions = Cheat.valueOf("BlockActions");
            Check blckActCheck = CheckRegistry.getCheck(blockActions);
            if (cp.isBlockPlacePacket(e.getPacket())
               && blckActCheck != null
               && blckActCheck.isEnabled()
               && !blckActCheck.isDisabledInWorld(w)
               && stats.canBeChecked(blckActCheck)
               && stats.getLagStatus().ordinal() < LagStatus.EXTREME_LAG.ordinal()) {
               ItemStack block;
               if (VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X)) {
                  ItemStack mainItem = p.getInventory().getItemInMainHand();
                  ItemStack offItem = p.getInventory().getItemInOffHand();
                  if (mainItem.getType() == Material.AIR) {
                     block = offItem;
                  } else if (mainItem.getType().isBlock()) {
                     block = mainItem;
                  } else {
                     block = offItem;
                  }
               } else {
                  block = (ItemStack)e.getPacket().getItemModifier().readSafely(0);
               }

               if (block != null) {
                  Material type = block.getType();
                  BlockPosition pos = (BlockPosition)e.getPacket().getBlockPositionModifier().readSafely(0);
                  String typeName = type.name().toLowerCase();
                  if (pos != null) {
                     if (stats.getBoolean("scfExpectUpdateItem")) {
                        stats.setStat("scfExpectUpdateItem", false);
                        if ((pos.getX() != -1 || pos.getY() != -1 || pos.getZ() != -1) && stats.getLagStatus() != LagStatus.EXTREME_LAG) {
                           boolean cancel = !this.reflex.getSetting("blockactions.silent", true);
                           CheckResult result = new CheckResult().setMod(5).addTag("type L").cancel(cancel);
                           Check.addVl(stats, blockActions, result);
                           Check.check(p, stats, null, blckActCheck, result);
                           if (cancel) {
                              stats.resetTimer("buildPenaltyTimer");
                           }
                        }
                     } else if (type.isBlock()
                        || typeName.contains("skull")
                        || typeName.contains("shield")
                        || typeName.contains("sign")
                        || typeName.contains("paint")
                        || typeName.contains("frame")
                        || typeName.contains("sign")
                        || typeName.contains("flint")
                        || typeName.contains("egg")
                        || typeName.contains("string")
                        || typeName.contains("banner")
                        || typeName.contains("bed")
                        || typeName.contains("fire")
                        || typeName.contains("craft")
                        || typeName.contains("stand")
                        || typeName.contains("compar")
                        || typeName.contains("repeat")
                        || typeName.contains("diode")
                        || typeName.contains("redstone")
                        || typeName.contains("door")
                        || typeName.contains("cake")
                        || typeName.contains("cauldron")
                        || typeName.contains("bucket")
                        || typeName.contains("flower")
                        || typeName.contains("plant")
                        || typeName.contains("hoe")
                        || typeName.contains("seeds")
                        || typeName.contains("carrot")
                        || typeName.contains("potato")
                        || typeName.contains("beetroot")
                        || typeName.contains("sugar")
                        || typeName.contains("brewing")
                        || pos.getX() == -1 && pos.getY() == -1 && pos.getZ() == -1) {
                        if (LocationUtils.isNormalBlock(type)
                           || type == Material.AIR
                              && stats.hasStat("lastHeldItem")
                              && LocationUtils.isNormalBlock(stats.getStat("lastHeldItem", Material.class))) {
                           if (pos.getX() == -1 && pos.getY() == -1 && pos.getZ() == -1) {
                              stats.addNumber("missesSincePlace", 1);
                           } else {
                              Location pLoc = p.getLocation().clone();
                              Location againstLoc = new Location(pLoc.getWorld(), (double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
                              Block against = againstLoc.getBlock();
                              if ((againstLoc.getY() == (double)(pLoc.getBlockY() - 1) || againstLoc.getY() == (double)(pLoc.getBlockY() - 2))
                                 && Maths.distance(pLoc, againstLoc) < 6.0
                                 && !Compatibility.isAny(type, "hoe", "banner", "brewing")
                                 && !Compatibility.isNearby(p, 4, "vine", "ladder", "banner", "wall", "fence")
                                 && LocationUtils.getDirectNeighbors(against).size() < 2) {
                                 Storage recentPlaced = stats.getOrCreateStorage("recentPacketPlaced", 5).add(pos).copy();
                                 int missesSincePlace = stats.getInt("missesSincePlace");
                                 int ticksSincePlace = stats.getInt("ticksSincePlace");
                                 List<Check> activeWorldChecks = CheckRegistry.getActiveChecksOfType(CheckType.WORLD);
                                 ReflexScheduler.async()
                                    .run(
                                       ReflexRunnable.simple(
                                          "async-bapb-check",
                                          () -> {
                                             boolean shouldCancel = false;

                                             for (Check check : activeWorldChecks) {
                                                if (!check.isDisabledInWorld(w) && stats.canBeChecked(check)) {
                                                   MethodProfiler mpCheck = new MethodProfiler("checkBAPB" + check.getName() + ":" + check.comp() + "(async)");
                                                   CheckResult resultx = ((WorldCheck)check)
                                                      .packetBlockPlace(p, stats, pLoc, pos, block, type, ticksSincePlace, missesSincePlace, recentPlaced);
                                                   if (resultx != null) {
                                                      if (resultx.shouldCancel()) {
                                                         shouldCancel = true;
                                                      }

                                                      Check.addVl(stats, check.getCheat(), resultx);
                                                      Check.check(p, stats, e, check, resultx);
                                                   }

                                                   if (shouldCancel && recentPlaced.size() > 2) {
                                                      MethodProfiler mpCancel = new MethodProfiler("ZK-47981298");
                                                      ReflexScheduler.sync()
                                                         .run(
                                                            ReflexRunnable.simple(
                                                               "SR-ZK-47981298",
                                                               () -> {
                                                                  BlockPosition first = (BlockPosition)recentPlaced.get(0);

                                                                  for (int i = 1; i < recentPlaced.size(); i++) {
                                                                     BlockPosition bp = (BlockPosition)recentPlaced.get(i);
                                                                     w.getBlockAt(bp.getX(), bp.getY(), bp.getZ()).setType(Material.AIR);
                                                                  }

                                                                  p.teleport(
                                                                     new Location(
                                                                        w,
                                                                        (double)first.getX() + 0.5,
                                                                        (double)first.getY() + 0.85,
                                                                        (double)first.getZ() + 0.5,
                                                                        pLoc.getYaw() - 180.0F,
                                                                        -90.0F + (float)((0.1F + Math.random()) * 2.0)
                                                                     )
                                                                  );
                                                                  stats.getStorage("recentPacketPlaced").clear();
                                                                  stats.resetTimer("buildPenaltyTimer");
                                                                  mpCancel.addToTimings();
                                                               }
                                                            )
                                                         );
                                                   }

                                                   mpCheck.checkExecutionTime();
                                                   mpCheck.addToTimings();
                                                }
                                             }

                                             stats.setStat("missesSincePlace", 0);
                                             stats.setStat("ticksSincePlace", 0);
                                          }
                                       )
                                    );
                              }
                           }
                        }
                     } else {
                        boolean foodInOneOfHands = p.getItemInHand() != null && p.getItemInHand().getType().isEdible();
                        if (!foodInOneOfHands && VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_9_X)) {
                           foodInOneOfHands = p.getInventory().getItemInOffHand() != null && p.getInventory().getItemInOffHand().getType().isEdible();
                        }

                        if (!foodInOneOfHands) {
                           try {
                              if (LocationUtils.isNormalBlock(w.getBlockAt(pos.getX(), pos.getY(), pos.getZ()))) {
                                 stats.setStat("scfExpectUpdateItem", true);
                              }
                           } catch (IllegalStateException ignoredEx) {
                           }
                        }
                     }
                  }
               }
            }

            mpBlockPlace.addToTimings();
            if (e.getPacketType().equals(Packets.IN_KEEPALIVE) && this.reflex.getRandom().nextBoolean()) {
               ReflexScheduler.async().run(ReflexRunnable.simple("AR-IX-2313104", () -> {
                  try {
                     MethodProfiler mpNewAPIntegrityCheck = new MethodProfiler("IX-2313104");
                     if (Maths.objects[0] != null) {
                        String allNumericSum = Double.toString(Maths.getNewApInstance().sumAllNumeric());
                        int ansHash = allNumericSum.hashCode() ^ -75981273;
                        switch (ansHash) {
                           case -1912050773:
                              if (!Maths.objects[18].getClass().getSimpleName().equals("BufferedReader")) {
                                 CentralEventListener.setStopped(true);
                                 EventRegistry.unregisterAll(this.reflex);
                              }
                              break;
                           case -1737155564:
                              if ((Long)Maths.objects[18] != -8921739127498L) {
                                 e.setCancelled(true);
                                 Bukkit.getScheduler().runTaskLater(this.reflex, () -> p.kickPlayer("Timed out."), 10L);
                              }
                              break;
                           default:
                              EventRegistry.unregisterAll(this.reflex);
                        }
                     }

                     mpNewAPIntegrityCheck.addToTimings();
                  } catch (Throwable ignoredErr) {
                     e.setCancelled(true);
                     CheckRegistry.getActiveChecksOfType(CheckType.PACKET).forEach(Check::disable);
                  }
               }));
            }

            MethodProfiler mpOpenInv = new MethodProfiler("SE-19478210");
            if (e.getPacket().getType().equals(Packets.IN_CLIENTCOMMAND)) {
               ClientCommand command = (ClientCommand)e.getPacket().getClientCommands().read(0);
               if (command == ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                  stats.setStat("inventoryOpened", true);
                  stats.getStat("inventoryOpenTracker", Timer.class).reset();
               }
            }

            mpOpenInv.addToTimings();
            List<Check> activeChecks = CheckRegistry.getActiveChecksOfType(CheckType.PACKET);
            final List<Check> syncChecks = new ArrayList<>();
            final SharedPacketData data = new SharedPacketData();
            final PacketContainer packet = e.getPacket();
            boolean isPosition = packet.getType().equals(Packets.IN_POSITION);
            boolean isLook = packet.getType().equals(Packets.IN_LOOK);
            boolean isPoslook = packet.getType().equals(Packets.IN_POSLOOK);
            boolean isVehicleMove = packet.getType().equals(Packets.IN_VEHICLEMOVE);
            if (isPosition || isPoslook || isVehicleMove) {
               if (isVehicleMove) {
                  data.vehicleMove();
               }

               double newX = Packets.readDouble(packet, 0);
               double newY = Packets.readDouble(packet, 1);
               double newZ = Packets.readDouble(packet, 2);
               data.x(newX);
               data.y(newY);
               data.z(newZ);
               double oldX = stats.getDouble("pckOldX");
               double oldY = stats.getDouble("pckOldY");
               double oldZ = stats.getDouble("pckOldZ");
               data.oldX(oldX);
               data.oldY(oldY);
               data.oldZ(oldZ);
               stats.setStat("pckOldX", newX);
               stats.setStat("pckOldY", newY);
               stats.setStat("pckOldZ", newZ);
               if (oldX != 0.0 || oldY != 0.0 || oldZ != 0.0) {
                  double movedX = newX - oldX;
                  double movedY = newY - oldY;
                  double movedZ = newZ - oldZ;
                  data.movedX(movedX);
                  data.movedY(movedY);
                  data.movedZ(movedZ);
                  boolean hasAnyMove = false;
                  if (movedY != 0.0) {
                     hasAnyMove = true;
                     stats.resetTimer("pckVerMoveTracker");
                  }

                  if (movedX != 0.0 || movedZ != 0.0) {
                     hasAnyMove = true;
                     stats.resetTimer("pckHorMoveTracker");
                  }

                  if (hasAnyMove) {
                     stats.resetTimer("pckMoveTracker");
                  }
               }
            }

            if (isLook || isPoslook || isVehicleMove) {
               if (isVehicleMove) {
                  data.vehicleMove();
               }

               float yaw = Packets.readFloat(packet, 0);
               float pitch = Packets.readFloat(packet, 1);
               double oldYaw = stats.getDouble("pckOldYaw");
               double oldPitch = stats.getDouble("pckOldPitch");
               data.oldYaw(oldYaw);
               data.oldPitch(oldPitch);
               stats.setStat("pckOldYaw", (double)yaw);
               stats.setStat("pckOldPitch", (double)pitch);
               data.yaw(yaw);
               data.pitch(pitch);
               if (oldYaw != 0.0 || oldPitch != 0.0) {
                  double rotatedYaw = Maths.angle((double)yaw, oldYaw);
                  double rotatedPitch = Maths.angle((double)pitch, oldPitch);
                  data.rotatedYaw(rotatedYaw);
                  data.rotatedPitch(rotatedPitch);
               }
            }

            for (Check check : activeChecks) {
               if (!check.isDisabledInWorld(w) && stats.canBeChecked(check)) {
                  if (check.isSync()) {
                     syncChecks.add(check);
                  } else {
                     MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "In(async)");
                     CheckResult result = ((PacketCheck)check).checkIn(p, stats, packet, data);
                     if (result != null) {
                        Check.addVl(stats, check.getCheat(), result);
                        Check.check(p, stats, e, check, result);
                     }

                     mp0.checkExecutionTime();
                     mp0.addToTimings();
                  }
               }
            }

            ReflexScheduler.sync().run(new ReflexRunnable("OF-398127981") {
               @Override
               public void execute() throws Exception {
                  for (Check syncCheck : syncChecks) {
                     MethodProfiler mp0 = new MethodProfiler("check" + syncCheck.getName() + ":" + syncCheck.comp() + "In(Sync)");
                     CheckResult result = ((PacketCheck)syncCheck).checkIn(p, stats, packet, data);
                     if (result != null) {
                        Check.addVl(stats, syncCheck.getCheat(), result);
                        Check.check(p, stats, e, syncCheck, result);
                     }

                     mp0.checkExecutionTime();
                     mp0.addToTimings();
                  }
               }
            });
            if (Packets.isAttackPacket(packet)) {
               stats.resetTimer("useEntityTracker");
            }

            if (Packets.isUpdatePacket(packet)) {
               stats.setStat("lastTickOnGround", p.isOnGround());
            }

            MethodProfiler mpPosCacheUpd = new MethodProfiler("NB-54877219");
            if (Packets.isUpdatePacket(packet)) {
               if (stats.hasStat("currentTarget") && stats.addNumber("posCacheUpdateCounter", 1) > stats.getPingTicks()) {
                  LivingEntity ct = stats.getCurrentTarget();
                  BoundingBox bb = BoundingBox.of((Entity)ct).expand(0.3, 0.0, 0.3).expandUp(1.0).expandDown(2.0);
                  stats.setStat("posCacheUpdateCounter", 0);
                  stats.getMap("posCaches").put(ct, bb);
               }

               stats.resetTimer("updateTracker");
            }

            mpPosCacheUpd.addToTimings();
            stats.resetTimer("packetFlushTracker");
         }
      }
   }

   @Override
   public void onPacketSending(PacketEvent e) {
      if (!e.isCancelled()) {
         Player p = e.getPlayer();
         Stats stats = Stats.of(p);
         if (!stats.isAdmin()) {
            if (e.getPacket().getType().equals(Packets.OUT_ENTITYVELOCITY)) {
               Vector velocity = Packets.getVelocity(e.getPacket());
               stats.setStat("lastPacketVelocity", velocity);
            }

            for (Check check : CheckRegistry.getActiveChecksOfType(CheckType.PACKET)) {
               if (stats.canBeChecked(check)) {
                  if (check.isSync()) {
                     Bukkit.getScheduler().runTask(this.reflex, () -> {
                        MethodProfiler mp0x = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "Out(sync)");
                        CheckResult resultx = ((PacketCheck)check).checkOut(p, stats, e.getPacket());
                        if (resultx != null) {
                           Check.addVl(stats, check.getCheat(), resultx);
                           Check.check(p, stats, e, check, resultx);
                        }

                        mp0x.checkExecutionTime();
                        mp0x.addToTimings();
                     });
                  } else {
                     MethodProfiler mp0 = new MethodProfiler("check" + check.getName() + ":" + check.comp() + "Out(async)");
                     CheckResult result = ((PacketCheck)check).checkOut(p, stats, e.getPacket());
                     if (result != null) {
                        Check.addVl(stats, check.getCheat(), result);
                        Check.check(p, stats, e, check, result);
                     }

                     mp0.checkExecutionTime();
                     mp0.addToTimings();
                  }
               }
            }
         }
      }
   }
}
