package rip.reflex.util.protocol.npc;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rip.reflex.Reflex;
import rip.reflex.util.math.Maths;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.NotCompatibleException;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.mcaccess.MCAccess;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class NPC {
   private static Boolean syncNpcs = null;
   private Timer spawnTracker = null;
   private final Player player;
   public boolean randomIDeachSpawn = false;
   public String username;
   public UUID uuid;
   public boolean sprinting;
   public boolean sneaking;
   public boolean using;
   public boolean burning;
   public boolean flying;
   public boolean glowing;
   public boolean silent;
   public boolean showInTab;
   public boolean hidden;
   public boolean onGround = true;
   public boolean spawned;
   public boolean inTab;
   public boolean autoAnimations = true;
   public boolean autoRespawn = true;
   WrappedDataWatcher dataWatcher;
   private Location location;
   private float headYaw;
   private float health = 20.0F;
   private int moveCounter;
   private int entityId = -1;
   private long lastMove;
   private Location oldLocation;
   private boolean hideNametag;

   public NPC(Player player) throws NotCompatibleException {
      this.player = player;
      this.entityId = EntityUtils.getFreeEntityID();
      if (NPCCommons.getMCVersion() == MCVersion.WTF) {
         throw new NotCompatibleException();
      }
   }

   public float getHealth() {
      return this.health;
   }

   public void setHealth(float health) throws InvocationTargetException, NotCompatibleException {
      MethodProfiler mp = new MethodProfiler("VE-21839101");
      boolean changed = this.health != health;
      boolean isLower = health < this.health;
      boolean rip = health <= 0.0F && this.health > 0.0F;
      this.health = health;
      if (changed) {
         if (VersionUtils.getServerVersion().getId() >= MCVersion.V1_10_X.getId()) {
            this.dataWatcher.getWatchableObject(7).setValue(health);
         } else {
            this.dataWatcher.setObject(6, health);
         }

         if (isLower && this.autoAnimations) {
            this.sendAnimation(Animations.TAKE_DAMAGE);
         }

         this.sendMetaUpdate();
         if (rip) {
            this.unequip();
            this.despawn();
            if (this.autoRespawn) {
               this.spawn();
            }
         }
      }

      mp.addToTimings();
   }

   private void unequip() throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-1982303");

      for (EquipSlot slot : EquipSlot.values()) {
         if (slot != EquipSlot.OFF_HAND || this.is19()) {
            try {
               this.setEquipmentInSlot(slot, null);
            } catch (NotCompatibleException ignored) {
            }
         }
      }

      mp.addToTimings();
   }

   public void setEquipmentInSlot(EquipSlot slot, ItemStack stack) throws NotCompatibleException, InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-750124014");
      if (slot == EquipSlot.OFF_HAND && !this.is19()) {
         throw new NotCompatibleException();
      } else {
         Objects.requireNonNull(slot, "Equipment slot cannot be null!");
         if (stack == null) {
            stack = new ItemStack(Material.AIR);
         }

         if (slot == EquipSlot.OFF_HAND && VersionUtils.getServerVersion().getId() < MCVersion.V1_9_X.getId()) {
            throw new NotCompatibleException();
         } else if (slot != EquipSlot.MAIN_HAND || !this.is17()) {
            PacketContainer packet = new PacketContainer(Server.ENTITY_EQUIPMENT);
            packet.getIntegers().write(0, this.entityId);
            if (this.is19()) {
               packet.getItemSlots().write(0, slot.toWrapped());
            } else {
               packet.getIntegers().write(1, slot.ordinal() - (!this.is19() && slot != EquipSlot.MAIN_HAND ? 1 : 0));
            }

            if (VersionUtils.getServerVersion().getId() >= MCVersion.V1_11_X.getId() && stack.getType() == Material.AIR) {
               MCAccess.setField(packet.getHandle(), "c", null);
            } else {
               packet.getItemModifier().write(0, stack);
            }

            this.sendPacket(this.player, packet);
            mp.addToTimings();
         }
      }
   }

   public long getSpawnedTicks() {
      if (this.spawnTracker == null) {
         return 1L;
      } else {
         if (this.spawnTracker.getTimeTicked() >= 500L) {
            this.spawnTracker.reset();
         }

         return Maths.notZero(this.spawnTracker.getTimeTicked(), 1L);
      }
   }

   public void swingArm() throws InvocationTargetException {
      this.sendAnimation(Animations.SWING_ARM);
   }

   private void sendAnimation(Animations animations) throws InvocationTargetException {
      PacketContainer animation = new PacketContainer(Server.ANIMATION);
      animation.getIntegers().write(0, this.entityId);
      animation.getIntegers().write(1, animations.ordinal());
      this.sendPacket(this.player, animation);
   }

   public Location getLocation() {
      return this.location;
   }

   public void setLocation(Location location) throws NotCompatibleException, InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-89472183791");
      this.oldLocation = this.location;
      this.lastMove = System.currentTimeMillis();
      if (this.location == null) {
         this.location = location;
         this.sendPosTP();
      } else {
         float dist = (float)Maths.distance(this.location, location);
         Location oldLoc = this.location;
         boolean anglesChanged = this.location.getYaw() != location.getYaw() || this.location.getPitch() != location.getPitch();
         this.location = location;
         if (dist > this.getMaxRelMoveRange()) {
            this.sendPosTP();
         } else if (dist == 0.0F && anglesChanged) {
            this.sendAngleUpdate();
         } else if (dist != 0.0F && !anglesChanged) {
            this.sendMoveUpdate(oldLoc);
         } else if (dist != 0.0F && anglesChanged) {
            this.sendMoveAngleUpdate(oldLoc);
         }

         if (anglesChanged) {
            this.setHeadYaw(location.getYaw());
         }

         this.moveCounter++;
         if (this.moveCounter > 100) {
            this.sendPosTP();
            this.moveCounter = 0;
         }

         mp.addToTimings();
      }
   }

   public Location pingBasedLocation(int ping) {
      return System.currentTimeMillis() > this.lastMove + (long)(ping * 1000) ? this.location : this.oldLocation;
   }

   private void sendMoveAngleUpdate(Location prev) throws NotCompatibleException, InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-279319889");
      PacketContainer tp = new PacketContainer(Server.ENTITY_MOVE_LOOK);
      tp.getIntegers().write(0, this.entityId);
      switch (NPCCommons.getMCVersion()) {
         case V1_7_X:
            tp.getBytes().write(0, (byte)((int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 1.0)));
            tp.getBytes().write(1, (byte)((int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 1.0)));
            tp.getBytes().write(2, (byte)((int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 1.0)));
            break;
         case V1_8_X:
            tp.getBytes().write(0, (byte)((int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 1.0)));
            tp.getBytes().write(1, (byte)((int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 1.0)));
            tp.getBytes().write(2, (byte)((int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 1.0)));
            break;
         case V1_9_X:
         case V1_10_X:
         case V1_11_X:
         case V1_12_X:
         case V1_13_X:
            tp.getIntegers().write(1, (int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 128.0));
            tp.getIntegers().write(2, (int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 128.0));
            tp.getIntegers().write(3, (int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 128.0));
            break;
         default:
            throw new NotCompatibleException();
      }

      tp.getBooleans().write(0, this.onGround);
      tp.getBytes().write(NPCCommons.getMCVersion().getId() >= MCVersion.V1_9_X.getId() ? 0 : 3, (byte)Maths.compressAngle(this.location.getYaw()));
      tp.getBytes().write(NPCCommons.getMCVersion().getId() >= MCVersion.V1_9_X.getId() ? 1 : 4, (byte)Maths.compressAngle(this.location.getPitch()));
      this.sendPacket(this.player, tp);
      mp.addToTimings();
   }

   private void sendMoveUpdate(Location prev) throws NotCompatibleException, InvocationTargetException {
      if (this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-68974921");
         PacketContainer tp = new PacketContainer(Server.REL_ENTITY_MOVE);
         tp.getIntegers().write(0, this.entityId);
         switch (NPCCommons.getMCVersion()) {
            case V1_7_X:
               tp.getBytes().write(0, (byte)((int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 1.0)));
               tp.getBytes().write(1, (byte)((int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 1.0)));
               tp.getBytes().write(2, (byte)((int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 1.0)));
               break;
            case V1_8_X:
               tp.getBytes().write(0, (byte)((int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 1.0)));
               tp.getBytes().write(1, (byte)((int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 1.0)));
               tp.getBytes().write(2, (byte)((int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 1.0)));
               break;
            case V1_9_X:
               tp.getIntegers().write(1, (int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 128.0));
               tp.getIntegers().write(2, (int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 128.0));
               tp.getIntegers().write(3, (int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 128.0));
               break;
            case V1_10_X:
               tp.getIntegers().write(1, (int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 128.0));
               tp.getIntegers().write(2, (int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 128.0));
               tp.getIntegers().write(3, (int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 128.0));
            case V1_11_X:
            case V1_12_X:
            case V1_13_X:
               tp.getIntegers().write(1, (int)((this.location.getX() * 32.0 - prev.getX() * 32.0) * 128.0));
               tp.getIntegers().write(2, (int)((this.location.getY() * 32.0 - prev.getY() * 32.0) * 128.0));
               tp.getIntegers().write(3, (int)((this.location.getZ() * 32.0 - prev.getZ() * 32.0) * 128.0));
               break;
            default:
               throw new NotCompatibleException();
         }

         tp.getBooleans().write(0, this.onGround);
         this.sendPacket(this.player, tp);
         mp.addToTimings();
      }
   }

   private void sendAngleUpdate() throws InvocationTargetException {
      if (this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-9213791");
         PacketContainer tp = new PacketContainer(Server.ENTITY_LOOK);
         tp.getIntegers().write(0, this.entityId);
         tp.getBytes().write(0, (byte)Maths.compressAngle(this.location.getYaw()));
         tp.getBytes().write(1, (byte)Maths.compressAngle(this.location.getPitch()));
         tp.getBooleans().write(0, this.onGround);
         this.sendPacket(this.player, tp);
         mp.addToTimings();
      }
   }

   private void sendPosTP() throws NotCompatibleException, InvocationTargetException {
      if (this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-598271389");
         PacketContainer tp = new PacketContainer(Server.ENTITY_TELEPORT);
         tp.getIntegers().write(0, this.entityId);
         switch (NPCCommons.getMCVersion()) {
            case V1_7_X:
               tp.getIntegers().write(1, (int)(this.location.getX() * 32.0));
               tp.getIntegers().write(2, (int)(this.location.getY() * 32.0));
               tp.getIntegers().write(3, (int)(this.location.getZ() * 32.0));
               break;
            case V1_8_X:
               tp.getIntegers().write(1, (int)(this.location.getX() * 32.0));
               tp.getIntegers().write(2, (int)(this.location.getY() * 32.0));
               tp.getIntegers().write(3, (int)(this.location.getZ() * 32.0));
               break;
            case V1_9_X:
               tp.getDoubles().write(0, this.location.getX());
               tp.getDoubles().write(1, this.location.getY());
               tp.getDoubles().write(2, this.location.getZ());
               break;
            case V1_10_X:
               tp.getDoubles().write(0, this.location.getX());
               tp.getDoubles().write(1, this.location.getY());
               tp.getDoubles().write(2, this.location.getZ());
            case V1_11_X:
            case V1_12_X:
            case V1_13_X:
               tp.getDoubles().write(0, this.location.getX());
               tp.getDoubles().write(1, this.location.getY());
               tp.getDoubles().write(2, this.location.getZ());
               break;
            default:
               throw new NotCompatibleException();
         }

         tp.getBytes().write(0, (byte)Maths.compressAngle(this.location.getYaw()));
         tp.getBytes().write(1, (byte)Maths.compressAngle(this.location.getPitch()));
         tp.getBooleans().write(0, this.onGround);
         this.sendPacket(this.player, tp);
         mp.addToTimings();
      }
   }

   public float getHeadYaw() {
      return this.headYaw;
   }

   private void setHeadYaw(float headYaw) throws InvocationTargetException {
      boolean changed = this.headYaw != headYaw;
      this.headYaw = headYaw;
      if (changed) {
         this.sendHeadYawUpdate();
      }
   }

   private void sendHeadYawUpdate() throws InvocationTargetException {
      if (this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-827193");
         PacketContainer headYaw = new PacketContainer(Server.ENTITY_HEAD_ROTATION);
         headYaw.getIntegers().write(0, this.entityId);
         headYaw.getBytes().write(0, (byte)Maths.compressAngle(this.headYaw));
         this.sendPacket(this.player, headYaw);
         mp.addToTimings();
      }
   }

   public boolean isVisible() {
      return !this.hidden;
   }

   public void setVisible(boolean visible) throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-63821707");
      boolean changed = visible == this.hidden;
      this.hidden = !visible;
      if (changed && this.dataWatcher != null) {
         if (this.is19()) {
            this.dataWatcher.getWatchableObject(0).setValue(this.generateBitMask());
         } else {
            this.dataWatcher.setObject(0, this.generateBitMask());
         }

         this.sendMetaUpdate();
         mp.addToTimings();
      }
   }

   private void sendMetaUpdate() throws InvocationTargetException {
      if (this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-42193981291");
         PacketContainer update = new PacketContainer(Server.ENTITY_METADATA);
         update.getIntegers().write(0, this.entityId);
         update.getWatchableCollectionModifier().write(0, this.dataWatcher.getWatchableObjects());
         this.sendPacket(this.player, update);
         mp.addToTimings();
      }
   }

   public void hide() throws InvocationTargetException {
      this.setVisible(false);
   }

   public void show() throws InvocationTargetException {
      this.setVisible(true);
   }

   public void spawn() throws InvocationTargetException {
      if (Bukkit.isPrimaryThread()) {
         this.spawn00();
      } else {
         ReflexScheduler.sync().run(new ReflexRunnable("NpcSpawn-" + this.entityId + ":" + this.player.getName()) {
            @Override
            public void execute() throws Exception {
               NPC.this.spawn00();
            }
         });
      }
   }

   private void spawn00() throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-120371288");
      if (!this.spawned) {
         if (this.randomIDeachSpawn) {
            this.entityId = EntityUtils.getFreeEntityID();
         }

         boolean tab;
         try {
            this.sendTabPacket();
            tab = true;
         } catch (RuntimeException ignored) {
            tab = false;
         }

         this.spawn0();
         if (!this.showInTab && tab) {
            this.sendTabRemovePacket();
         }

         this.inTab = this.showInTab && tab;
         this.spawned = true;
         mp.addToTimings();
      }
   }

   private void addPotionEffect(Byte effectId, Byte amplifier, Integer duration, boolean particles) throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-87219837");
      PacketContainer container = new PacketContainer(Server.ENTITY_EFFECT);
      container.getIntegers().write(0, this.entityId);
      container.getBytes().write(0, effectId);
      container.getBytes().write(1, amplifier);
      container.getIntegers().write(1, duration);
      container.getBytes().write(2, Byte.valueOf((byte)(particles ? 1 : 0)));
      this.sendPacket(this.player, container);
      mp.addToTimings();
   }

   public void removePotionEffect(Byte effectId) {
      MethodProfiler mp = new MethodProfiler("VE-298137921");
      PacketContainer container = new PacketContainer(Server.REMOVE_ENTITY_EFFECT);
      container.getIntegers().write(0, this.entityId);
      container.getBytes().write(0, effectId);
      this.sendPacket(this.player, container);
      mp.addToTimings();
   }

   private void sendTabPacket() throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-5897123989");
      Player bukkitPlayer = Bukkit.getPlayer(this.uuid);
      if (bukkitPlayer == null || !bukkitPlayer.isOnline() || !this.player.canSee(bukkitPlayer) && this.player != bukkitPlayer) {
         PacketContainer tab = new PacketContainer(Server.PLAYER_INFO);
         if (NPCCommons.getMCVersion() == MCVersion.V1_7_X) {
            tab.getIntegers().write(0, 0);
            tab.getGameProfiles().write(0, new WrappedGameProfile(this.uuid, this.username));
            tab.getIntegers().write(1, 0);
            tab.getIntegers().write(2, NPCCommons.getRdm().nextInt(500));
            tab.getStrings().write(0, this.username);
         } else {
            tab.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
            List<PlayerInfoData> playerData = new ArrayList<>();
            playerData.add(
               new PlayerInfoData(
                  new WrappedGameProfile(this.uuid, this.username),
                  NPCCommons.getRdm().nextInt(900),
                  NativeGameMode.SURVIVAL,
                  WrappedChatComponent.fromText(this.username)
               )
            );
            tab.getPlayerInfoDataLists().write(0, playerData);
         }

         this.sendPacket(this.player, tab);
         mp.addToTimings();
         this.inTab = true;
      } else {
         throw new RuntimeException();
      }
   }

   private void spawn0() throws InvocationTargetException {
      if (!this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-479812739");

         try {
            switch (NPCCommons.getMCVersion()) {
               case V1_7_X:
                  this.spawnNPC_1_7();
                  break;
               case V1_8_X:
                  this.spawnNPC_1_8();
                  break;
               case V1_9_X:
                  this.spawnNPC_1_9();
                  break;
               case V1_10_X:
                  this.spawnNPC_1_10();
                  break;
               case V1_11_X:
                  this.spawnNPC_1_11();
                  break;
               case V1_12_X:
                  this.spawnNPC_1_12();
                  break;
               case V1_13_X:
                  this.spawnNPC_1_13();
                  break;
               default:
                  throw new NotCompatibleException();
            }
         } catch (Exception ex) {
            ex.printStackTrace();
         }

         this.sendHeadYawUpdate();
         mp.addToTimings();
      }
   }

   private void set(int index, WrappedDataWatcher watcher, Object value) {
      if (this.dataWatcher.hasIndex(index)) {
         watcher.getWatchableObject(index).setValue(value);
      } else {
         watcher.setObject(index, value);
      }
   }

   private byte generateBitMask() {
      return (byte)(
         (this.burning ? 1 : 0)
            + (this.sneaking ? 2 : 0)
            + (this.sprinting ? 8 : 0)
            + (this.using ? 16 : 0)
            + (this.hidden ? 32 : 0)
            + (this.is19() ? (this.glowing ? 64 : 0) + (this.flying ? 128 : 0) : 0)
      );
   }

   private void spawnNPC_1_13() throws Exception {
      this.spawnNPC_1_12();
   }

   private void spawnNPC_1_12() throws Exception {
      this.spawnNPC_1_11();
   }

   private void spawnNPC_1_11() throws Exception {
      this.spawnNPC_1_10();
   }

   private void spawnNPC_1_10() throws Exception {
      MethodProfiler mp = new MethodProfiler("VE-7210938");
      PacketContainer spawn = new PacketContainer(Server.NAMED_ENTITY_SPAWN);
      spawn.getIntegers().write(0, this.entityId);
      spawn.getSpecificModifier(UUID.class).write(0, this.uuid);
      spawn.getDoubles().write(0, this.location.getX());
      spawn.getDoubles().write(1, this.location.getY());
      spawn.getDoubles().write(2, this.location.getZ());
      spawn.getBytes().write(0, (byte)Maths.compressAngle(this.location.getYaw()));
      spawn.getBytes().write(1, (byte)Maths.compressAngle(this.location.getPitch()));
      ImpWrappedDataWatcher improvedDataWatcher = new ImpWrappedDataWatcher();
      improvedDataWatcher.setEntireMap(
         0,
         this.generateBitMask(),
         1,
         20,
         2,
         this.username,
         3,
         true,
         4,
         this.silent,
         5,
         true,
         6,
         (byte)1,
         7,
         this.health,
         8,
         0,
         9,
         false,
         10,
         0,
         11,
         0.0F,
         12,
         NPCCommons.getRdm().nextInt(10000),
         13,
         (byte)0,
         14,
         (byte)1
      );
      this.dataWatcher = improvedDataWatcher;
      spawn.getDataWatcherModifier().write(0, this.dataWatcher);
      this.sendPacket(this.player, spawn);
      this.onSpawn();
      mp.addToTimings();
   }

   private void spawnNPC_1_9() throws Exception {
      MethodProfiler mp = new MethodProfiler("VE-128736181");
      PacketContainer spawn = new PacketContainer(Server.NAMED_ENTITY_SPAWN);
      spawn.getIntegers().write(0, this.entityId);
      spawn.getSpecificModifier(UUID.class).write(0, this.uuid);
      spawn.getDoubles().write(0, this.location.getX());
      spawn.getDoubles().write(1, this.location.getY());
      spawn.getDoubles().write(2, this.location.getZ());
      spawn.getBytes().write(0, (byte)Maths.compressAngle(this.location.getYaw()));
      spawn.getBytes().write(1, (byte)Maths.compressAngle(this.location.getPitch()));
      ImpWrappedDataWatcher improvedDataWatcher = new ImpWrappedDataWatcher();
      improvedDataWatcher.setEntireMap(
         0,
         this.generateBitMask(),
         1,
         20,
         2,
         this.username,
         3,
         true,
         4,
         this.silent,
         5,
         (byte)1,
         6,
         this.health,
         7,
         0,
         8,
         false,
         9,
         0,
         10,
         0.0F,
         11,
         NPCCommons.getRdm().nextInt(10000),
         12,
         (byte)0,
         13,
         (byte)1
      );
      this.dataWatcher = improvedDataWatcher;
      spawn.getDataWatcherModifier().write(0, this.dataWatcher);
      this.sendPacket(this.player, spawn);
      this.onSpawn();
      mp.addToTimings();
   }

   private void spawnNPC_1_8() throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-927131814");
      PacketContainer spawn = new PacketContainer(Server.NAMED_ENTITY_SPAWN);
      spawn.getIntegers().write(0, this.entityId);
      spawn.getSpecificModifier(UUID.class).write(0, this.uuid);
      spawn.getIntegers().write(1, (int)(this.location.getX() * 32.0));
      spawn.getIntegers().write(2, (int)(this.location.getY() * 32.0));
      spawn.getIntegers().write(3, (int)(this.location.getZ() * 32.0));
      spawn.getBytes().write(0, (byte)Maths.compressAngle(this.location.getYaw()));
      spawn.getBytes().write(1, (byte)Maths.compressAngle(this.location.getPitch()));
      spawn.getIntegers().write(4, 0);
      ImpWrappedDataWatcher improvedDataWatcher = new ImpWrappedDataWatcher();
      improvedDataWatcher.setEntireMap(
         0,
         this.generateBitMask(),
         1,
         (short)20,
         2,
         this.username,
         3,
         (byte)1,
         4,
         (byte)(this.silent ? 1 : 0),
         6,
         this.health,
         7,
         0,
         8,
         (byte)0,
         9,
         (byte)0,
         10,
         (byte)127,
         15,
         (byte)1,
         16,
         0,
         17,
         0.0F,
         18,
         NPCCommons.getRdm().nextInt(10000)
      );
      this.dataWatcher = improvedDataWatcher;
      spawn.getDataWatcherModifier().write(0, this.dataWatcher);
      this.sendPacket(this.player, spawn);
      this.onSpawn();
      mp.addToTimings();
   }

   private void spawnNPC_1_7() throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-696412971");
      PacketContainer spawn = new PacketContainer(Server.NAMED_ENTITY_SPAWN);
      spawn.getIntegers().write(0, this.entityId);
      spawn.getGameProfiles().write(0, new WrappedGameProfile(this.uuid, this.username));
      spawn.getIntegers().write(1, (int)(this.location.getX() * 32.0));
      spawn.getIntegers().write(2, (int)(this.location.getY() * 32.0));
      spawn.getIntegers().write(3, (int)(this.location.getZ() * 32.0));
      spawn.getBytes().write(0, (byte)Maths.compressAngle(this.location.getYaw()));
      spawn.getBytes().write(1, (byte)Maths.compressAngle(this.location.getPitch()));
      spawn.getIntegers().write(4, 0);
      this.dataWatcher = new WrappedDataWatcher();
      this.dataWatcher.setObject(0, this.generateBitMask());
      this.dataWatcher.setObject(1, (short)20);
      this.dataWatcher.setObject(2, this.username);
      this.dataWatcher.setObject(3, 1);
      this.dataWatcher.setObject(4, this.silent ? 1 : 0);
      this.dataWatcher.setObject(5, 1);
      this.dataWatcher.setObject(6, this.health);
      this.dataWatcher.setObject(7, 0);
      this.dataWatcher.setObject(8, (byte)0);
      this.dataWatcher.setObject(9, (byte)0);
      this.dataWatcher.setObject(10, (byte)127);
      this.dataWatcher.setObject(15, (byte)1);
      this.dataWatcher.setObject(16, 0);
      this.dataWatcher.setObject(17, 0.0F);
      this.dataWatcher.setObject(18, NPCCommons.getRdm().nextInt(10000));
      spawn.getDataWatcherModifier().write(0, this.dataWatcher);
      this.sendPacket(this.player, spawn);
      this.onSpawn();
      mp.addToTimings();
   }

   private void onSpawn() {
      MethodProfiler mp = new MethodProfiler("VE-591723798");
      NPCTeamTricks.disableCollisions(this.player, this);
      this.spawnTracker = new Timer();
      mp.addToTimings();
   }

   public void despawn() throws InvocationTargetException {
      MethodProfiler mp = new MethodProfiler("VE-8217938188");
      this.despawnNPC();
      if (this.inTab) {
         this.sendTabRemovePacket();
      }

      this.inTab = false;
      this.spawned = false;
      mp.addToTimings();
   }

   private void sendTabRemovePacket() throws InvocationTargetException {
      if (this.inTab) {
         MethodProfiler mp = new MethodProfiler("VE-2791381289");
         PacketContainer tab = new PacketContainer(Server.PLAYER_INFO);
         if (NPCCommons.getMCVersion() == MCVersion.V1_7_X) {
            tab.getIntegers().write(0, 4);
            tab.getGameProfiles().write(0, new WrappedGameProfile(this.uuid, this.username));
            tab.getStrings().write(0, this.username);
         } else {
            tab.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
            List<PlayerInfoData> playerData = new ArrayList<>();
            playerData.add(
               new PlayerInfoData(
                  new WrappedGameProfile(this.uuid, this.username),
                  NPCCommons.getRdm().nextInt(900),
                  NativeGameMode.SURVIVAL,
                  WrappedChatComponent.fromText(this.username)
               )
            );
            tab.getPlayerInfoDataLists().write(0, playerData);
         }

         this.sendPacket(this.player, tab);
         mp.addToTimings();
      }
   }

   private void despawnNPC() throws InvocationTargetException {
      if (this.spawned) {
         MethodProfiler mp = new MethodProfiler("VE-7213914");
         PacketContainer destroy = new PacketContainer(Server.ENTITY_DESTROY);
         destroy.getIntegerArrays().write(0, new int[]{this.entityId});
         this.sendPacket(this.player, destroy);
         mp.addToTimings();
      }
   }

   private boolean is19() {
      return NPCCommons.getMCVersion().getId() >= MCVersion.V1_9_X.getId();
   }

   private float getMaxRelMoveRange() throws NotCompatibleException {
      switch (NPCCommons.getMCVersion()) {
         case V1_7_X:
            return 4.0F;
         case V1_8_X:
            return 4.0F;
         case V1_9_X:
            return 8.0F;
         case V1_10_X:
            return 8.0F;
         case V1_11_X:
         case V1_12_X:
         case V1_13_X:
            return 8.0F;
         default:
            throw new NotCompatibleException();
      }
   }

   public int getID() {
      return this.entityId;
   }

   public void setID(int eid) {
      if (this.spawned) {
         throw new UnsupportedOperationException("Cannot modify entity ID while entity is spawned");
      } else {
         this.entityId = eid;
      }
   }

   public boolean isSpawned() {
      return this.spawned;
   }

   public boolean isDoingAnimations() {
      return this.autoAnimations;
   }

   public void doAnimations(boolean animation) {
      this.autoAnimations = animation;
   }

   public boolean isAutoRespawning() {
      return this.autoRespawn;
   }

   public void shouldAutoRespawn(boolean respawn) {
      this.autoRespawn = respawn;
   }

   private boolean is17() {
      return NPCCommons.getMCVersion() == MCVersion.V1_7_X;
   }

   private void sendPacket(Player player, PacketContainer packet) {
      if (syncNpcs == null) {
         syncNpcs = Reflex.getReflex().getSetting("killaura.sync_npcs", false);
      }

      if (syncNpcs) {
         Packets.sendPacketSync(player, packet);
      } else {
         Packets.sendPacketAsync(player, packet);
      }
   }

   @Override
   public String toString() {
      return "NPC(spawnTracker="
         + this.spawnTracker
         + ", player="
         + this.player
         + ", randomIDeachSpawn="
         + this.randomIDeachSpawn
         + ", username="
         + this.username
         + ", uuid="
         + this.uuid
         + ", sprinting="
         + this.sprinting
         + ", sneaking="
         + this.sneaking
         + ", using="
         + this.using
         + ", burning="
         + this.burning
         + ", flying="
         + this.flying
         + ", glowing="
         + this.glowing
         + ", silent="
         + this.silent
         + ", showInTab="
         + this.showInTab
         + ", hidden="
         + this.hidden
         + ", onGround="
         + this.onGround
         + ", spawned="
         + this.isSpawned()
         + ", inTab="
         + this.inTab
         + ", autoAnimations="
         + this.autoAnimations
         + ", autoRespawn="
         + this.autoRespawn
         + ", dataWatcher="
         + this.dataWatcher
         + ", location="
         + this.getLocation()
         + ", headYaw="
         + this.getHeadYaw()
         + ", health="
         + this.getHealth()
         + ", moveCounter="
         + this.moveCounter
         + ", entityId="
         + this.entityId
         + ", lastMove="
         + this.lastMove
         + ", oldLocation="
         + this.oldLocation
         + ", hideNametag="
         + this.hideNametag
         + ")";
   }

   public void setHideNametag(boolean hideNametag) {
      this.hideNametag = hideNametag;
   }
}
