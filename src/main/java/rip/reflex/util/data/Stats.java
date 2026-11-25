package rip.reflex.util.data;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.AsyncCatcher;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.ReflexPlayerData;
import rip.reflex.check.base.Check;
import rip.reflex.util.appearance.Theme;
import rip.reflex.util.appearance.ThemeManager;
import rip.reflex.util.data.storage.NumberStorage;
import rip.reflex.util.data.storage.Storage;
import rip.reflex.util.data.storage.TimerStorage;
import rip.reflex.util.math.Counter;
import rip.reflex.util.math.Maths;
import rip.reflex.util.math.SafeLong;
import rip.reflex.util.misc.Items;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.protocol.MCVersion;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.npc.EquipSlot;
import rip.reflex.util.protocol.npc.NPC;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class Stats {
   public static final Stats ADMIN = new Stats(null);
   public static Stats trigger = new Stats(null);
   private static Boolean enableLagLeniency;
   private static final ConcurrentHashMap<Player, Stats> statlist = new ConcurrentHashMap<>();
   private static final List<Player> temporaryPlayers = new ArrayList<>();
   private final HashMap<String, Object> stats = new HashMap<>();
   private final ConcurrentHashMap<Cheat, Integer> violations = new ConcurrentHashMap<>();
   private final Map<String, Boolean> permissionCache = new ConcurrentHashMap<>();
   private final Player player;
   private final List<NPC> frontEntities = new ArrayList<>();
   private final List<NPC> frontNpcs = new ArrayList<>();
   private final Timer accessTracker = new Timer();
   private final Object heuristicsDataLock = new Object();
   private final Object statsLock = new Object();
   public HashMap<String, ArrayList<Double>> heuristicsData = new HashMap<>();
   private NPC mainNpc;
   private final Object npcsLock = new Object();
   private boolean initLPcalcDone;
   private Boolean temporaryPlayer = null;
   private boolean admin;

   public Stats(Player player) {
      this.player = player;
      this.heuristicsData.clear();
      this.setStat("mcClientBrand", "vanilla");
      this.setStat("mcClientLocale", "en_US");
   }

   public static Stats of(Player p) {
      if (p == null) {
         return ADMIN;
      } else {
         try {
            p.getLocation();
            Stats s = statlist.get(p);
            if (s == null) {
               statlist.put(p, s = new Stats(p));
            }

            return s;
         } catch (UnsupportedOperationException ignored) {
            if (!temporaryPlayers.contains(p)) {
               temporaryPlayers.add(p);
            }

            return ADMIN;
         }
      }
   }

   public static void doCleanup() {
      List<Stats> toRem = new ArrayList<>();

      for (Stats stats : statlist.values()) {
         Player p = stats.player;
         if (p != null && (!p.isOnline() || stats.accessTracker.hasPassed(60000L)) && !toRem.contains(stats)) {
            toRem.add(stats);
         }
      }

      toRem.forEach(Stats::reset);
      List<Player> rm = new ArrayList<>();
      VersionUtils.clientVersions.keySet().stream().filter(px -> !px.isOnline()).forEach(rm::add);
      rm.forEach(VersionUtils.clientVersions::remove);
      temporaryPlayers.stream().filter(statlist::containsKey).forEach(statlist::remove);
      temporaryPlayers.clear();
   }

   public Theme getPersonalTheme() {
      return ThemeManager.getPersonalThemes().get(this.player);
   }

   public Map<String, String> getPersonalThemeRaw() {
      return ThemeManager.getPersonalRawThemes().get(this.player);
   }

   public void reloadPermissionCache() {
      this.permissionCache.clear();
   }

   public void syncTp(Location l) {
      if (l != null) {
         Location l0 = l.clone();
         if (Bukkit.isPrimaryThread()) {
            this.player.teleport(l0);
         }

         ReflexScheduler.sync().run(ReflexRunnable.simple("SyncTPThread", () -> this.player.teleport(l0)));
      }
   }

   public Timer getTimer(String key) {
      return this.getStat(key, Timer.class);
   }

   public Timer getTimerWithOffset(String key, long offset) {
      return !this.hasStat(key) ? this.setStat(key, new Timer().offset(offset)) : this.getTimer(key);
   }

   public static void reset(Stats stats) {
      if (stats.player.isOnline()) {
         try {
            stats.despawnFrontEntities();
            stats.despawnNpcs();
         } catch (Exception ignored) {
         }
      }

      statlist.remove(stats.player);
   }

   public long getTimerPassed(String key) {
      return this.getTimer(key).getTimePassed();
   }

   public long getTimerTicked(String key) {
      return this.getTimer(key).getTimeTicked();
   }

   public long getTimerPassedAndReset(String key) {
      Timer t = this.getTimer(key);
      long l = t.getTimePassed();
      t.reset();
      return l;
   }

   public long getTimerTickedAndReset(String key) {
      Timer t = this.getTimer(key);
      long l = t.getTimeTicked();
      t.reset();
      return l;
   }

   public boolean hasTimerPassed(String key, long millis) {
      try {
         return this.getTimer(key).hasPassed(millis);
      } catch (NullPointerException ignored) {
         return false;
      }
   }

   public boolean hasTimerPassed(String key, long millis, long initialTime, boolean initialPassed) {
      if (this.hasStat(key)) {
         return this.hasTimerPassed(key, millis);
      } else {
         this.setStat(key, new Timer(initialTime));
         return initialPassed;
      }
   }

   public boolean hasTimerTicked(String key, long ticks) {
      return this.getTimer(key).hasTicked(ticks);
   }

   public NumberStorage addInCreatedNumberStorage(String key, int cap, Number v) {
      NumberStorage ns = this.getOrCreateNumberStorage(key, cap);
      ns.add(v);
      return ns;
   }

   public NumberStorage addInCreatedNumberStorageIf(String key, int cap, Number v, boolean b) {
      return b ? this.addInCreatedNumberStorage(key, cap, v) : null;
   }

   public boolean addCheckingInCreatedNumberStorage(String key, int cap, Number v) {
      return this.getOrCreateNumberStorage(key, cap).addChecking(v);
   }

   public Timer resetTimer(String key) {
      return this.getTimer(key).reset();
   }

   public NumberStorage getNumberStorage(String key) {
      return this.getStat(key, NumberStorage.class);
   }

   public NumberStorage getOrCreateNumberStorage(String key, int capacity) {
      if (!this.hasStat(key)) {
         NumberStorage r = new NumberStorage(capacity);
         this.setStat(key, r);
         return r;
      } else {
         return this.getNumberStorage(key);
      }
   }

   public Storage getStorage(String key) {
      return this.getStat(key, Storage.class);
   }

   public Storage getOrCreateStorage(String key, int capacity) {
      if (!this.hasStat(key)) {
         Storage r = new Storage(capacity);
         this.setStat(key, r);
         return r;
      } else {
         return this.getStorage(key);
      }
   }

   public TimerStorage getTimerStorage(String key) {
      return this.getStat(key, TimerStorage.class);
   }

   public TimerStorage getOrCreateTimerStorage(String key, int capacity) {
      if (!this.hasStat(key)) {
         TimerStorage r = new TimerStorage(capacity);
         this.setStat(key, r);
         return r;
      } else {
         return this.getTimerStorage(key);
      }
   }

   public TimerStorage addInCreatedTimerStorage(String key, int cap, Timer t) {
      TimerStorage ts = this.getOrCreateTimerStorage(key, cap);
      ts.add(t);
      return ts;
   }

   public TimerStorage resetAllInCreatedTimerStorage(String key, int cap) {
      TimerStorage ts = this.getOrCreateTimerStorage(key, cap);
      ts.resetAll();
      return ts;
   }

   public String getString(String key) {
      return this.getStat(key, String.class);
   }

   public <T> T addNumberIf(String key, T fac, boolean b) {
      return (T)(b ? this.addNumber(key, fac) : 0);
   }

   public <T> T addNumber(String key, T fac) {
      if (fac instanceof Integer) {
         return (T)this.setStat(key, (this.getStat(key, int.class) + (Integer)fac));
      } else if (fac instanceof Double) {
         return (T)this.setStat(key, (this.getStat(key, double.class) + (Double)fac));
      } else if (fac instanceof Float) {
         return (T)this.setStat(key, (this.getStat(key, float.class) + (Float)fac));
      } else if (fac instanceof Long) {
         return (T)this.setStat(key, (this.getStat(key, long.class) + (Long)fac));
      } else if (fac instanceof Short) {
         return (T)this.setStat(key, (this.getStat(key, short.class) + (Short)fac));
      } else if (fac instanceof Byte) {
         return (T)this.setStat(key, (this.getStat(key, byte.class) + (Byte)fac));
      } else {
         throw new IllegalArgumentException("Unexpected factor type " + fac.toString() + " (" + fac.getClass().getSimpleName() + ")");
      }
   }

   public <T> T subtractNumberIf(String key, T fac, boolean b) {
      return (T)(b ? this.subtractNumber(key, fac) : 0);
   }

   public <T> T subtractNumberTilPositive(String key, T fac) {
      if (fac instanceof Integer) {
         return (T)this.setStat(key, Math.max(0, this.getStat(key, int.class) - (Integer)fac));
      } else if (fac instanceof Double) {
         return (T)this.setStat(key, Math.max(0.0, this.getStat(key, double.class) - (Double)fac));
      } else if (fac instanceof Float) {
         return (T)this.setStat(key, Math.max(0.0F, this.getStat(key, float.class) - (Float)fac));
      } else if (fac instanceof Long) {
         return (T)this.setStat(key, Math.max(0L, this.getStat(key, long.class) - (Long)fac));
      } else if (fac instanceof Short) {
         return (T)this.setStat(key, Math.max(0, this.getStat(key, short.class) - (Short)fac));
      } else if (fac instanceof Byte) {
         return (T)this.setStat(key, Math.max(0, this.getStat(key, byte.class) - (Byte)fac));
      } else {
         throw new IllegalArgumentException("Unexpected factor type " + fac.toString() + " (" + fac.getClass().getSimpleName() + ")");
      }
   }

   public <T> T subtractNumber(String key, T fac) {
      if (fac instanceof Integer) {
         return (T)this.setStat(key, (this.getStat(key, int.class) - (Integer)fac));
      } else if (fac instanceof Double) {
         return (T)this.setStat(key, (this.getStat(key, double.class) - (Double)fac));
      } else if (fac instanceof Float) {
         return (T)this.setStat(key, (this.getStat(key, float.class) - (Float)fac));
      } else if (fac instanceof Long) {
         return (T)this.setStat(key, (this.getStat(key, long.class) - (Long)fac));
      } else if (fac instanceof Short) {
         return (T)this.setStat(key, (this.getStat(key, short.class) - (Short)fac));
      } else if (fac instanceof Byte) {
         return (T)this.setStat(key, (this.getStat(key, byte.class) - (Byte)fac));
      } else {
         throw new IllegalArgumentException("Unexpected factor type " + fac.toString() + " (" + fac.getClass().getSimpleName() + ")");
      }
   }

   public LivingEntity getCurrentTarget() {
      return this.getStat("currentTarget", LivingEntity.class);
   }

   public boolean getBoolean(String key) {
      return this.getStat(key, boolean.class);
   }

   public int getInt(String key) {
      return this.getStat(key, int.class);
   }

   public int getLastVl(Cheat c) {
      return this.getInt("last" + c.name() + "Vl");
   }

   public short getShort(String key) {
      return this.getStat(key, short.class);
   }

   public long getLong(String key) {
      return this.getStat(key, long.class);
   }

   public double getDouble(String key) {
      return this.getStat(key, double.class);
   }

   public float getFloat(String key) {
      return this.getStat(key, float.class);
   }

   public byte getByte(String key) {
      return this.getStat(key, byte.class);
   }

   public SafeLong getSafeLong(String key) {
      return this.getStat(key, SafeLong.class);
   }

   public HashMap getMap(String key) {
      return this.getStat(key, HashMap.class);
   }

   public Location getLocation(String key) {
      return this.getStat(key, Location.class);
   }

   public Counter getCounter(String key) {
      return this.getStat(key, Counter.class);
   }

   public Counter getOrCreateCounter(String key, long maxDelay) {
      if (this.hasStat(key)) {
         return this.getCounter(key);
      } else {
         return this.hasStat(key) ? this.getCounter(key) : this.setStat(key, new Counter(maxDelay));
      }
   }

   public int updateCounter(String key) {
      return this.getCounter(key).update();
   }

   public int updateCreatedCounter(String key, long maxDelay) {
      return this.getOrCreateCounter(key, maxDelay).update();
   }

   public <T> boolean compareAndSet(String key, T value) {
      T old = this.getStat(key, this.toPrimitiveType(value));
      this.setStat(key, value);
      return old == value;
   }

   public <T> Class<T> toPrimitiveType(T t) {
      if (t instanceof Integer) {
         return (Class<T>)Integer.class;
      } else if (t instanceof Short) {
         return (Class<T>)Short.class;
      } else if (t instanceof Long) {
         return (Class<T>)Long.class;
      } else if (t instanceof Float) {
         return (Class<T>)Float.class;
      } else if (t instanceof Double) {
         return (Class<T>)Double.class;
      } else if (t instanceof Byte) {
         return (Class<T>)Byte.class;
      } else if (t instanceof Boolean) {
         return (Class<T>)Boolean.class;
      } else {
         throw new ClassCastException(t.getClass().getSimpleName() + " might not be an instance of a primitive type");
      }
   }

   public int getViolations(Cheat cheat) {
      if (cheat == null) {
         return 0;
      } else {
         if (!this.violations.containsKey(cheat)) {
            this.violations.put(cheat, 0);
         }

         if (this.violations.get(cheat) < 0) {
            this.resetViolations(cheat);
         }

         return this.violations.get(cheat);
      }
   }

   public void addViolations(Cheat cheat, int count) {
      if (cheat != null) {
         if (!this.violations.containsKey(cheat)) {
            this.violations.put(cheat, 0);
         }

         this.setStat("last" + cheat.name() + "Vl", this.getViolations(cheat));
         this.violations.put(cheat, this.violations.get(cheat) + count);
         if (this.violations.get(cheat) < 0) {
            this.resetViolations(cheat);
         }
      }
   }

   public void setViolations(Cheat cheat, int count) {
      if (cheat != null) {
         this.setStat("last" + cheat.name() + "Vl", this.getViolations(cheat));
         this.violations.put(cheat, count);
         if (this.violations.get(cheat) < 0) {
            this.resetViolations(cheat);
         }
      }
   }

   public void setViolationsIf(Cheat cheat, Supplier<Integer> count, Supplier<Boolean> check) {
      if (cheat != null && check.get()) {
         this.setStat("last" + cheat.name() + "Vl", this.getViolations(cheat));
         this.violations.put(cheat, count.get());
         if (this.violations.get(cheat) < 0) {
            this.resetViolations(cheat);
         }
      }
   }

   public void setViolations(ConcurrentHashMap<Cheat, Integer> newMap) {
      this.violations.clear();

      for (Cheat c : newMap.keySet()) {
         this.violations.put(c, newMap.get(c));
      }
   }

   public void resetViolations(Cheat cheat) {
      if (cheat != null) {
         this.violations.put(cheat, 0);
      }
   }

   public static void reset(Player p) {
      statlist.remove(p);
   }

   public boolean isTemporaryPlayer() {
      if (this.temporaryPlayer != null) {
         return this.temporaryPlayer;
      } else if (temporaryPlayers.contains(this.player)) {
         return this.temporaryPlayer = true;
      } else {
         try {
            this.player.getLocation();
            return this.temporaryPlayer = false;
         } catch (UnsupportedOperationException ignored) {
            if (!temporaryPlayers.contains(this.player)) {
               temporaryPlayers.add(this.player);
            }

            return this.temporaryPlayer = true;
         }
      }
   }

   public List<NPC> getFrontEntities() {
      synchronized (this.npcsLock) {
         return this.frontEntities;
      }
   }

   public <T> T getStat(String name, Class<T> type) {
      this.accessTracker.reset();
      synchronized (this.statsLock) {
         T stat;
         if (this.hasStat(name)) {
            stat = (T)this.stats.get(name);
         } else {
            stat = this.setStat(name, this.getDefaultValue(type));
         }

         return stat;
      }
   }

   public boolean isSprinting() {
      return this.getStat("sprinting", boolean.class);
   }

   public boolean isSneaking() {
      return this.getStat("sneaking", boolean.class);
   }

   public boolean isInventoryOpened() {
      return this.getStat("inventoryOpened", boolean.class);
   }

   public boolean isAnyInvOpen() {
      return this.player.getOpenInventory() != null || this.isInventoryOpened();
   }

   public boolean hasNpcs() {
      synchronized (this.npcsLock) {
         return this.mainNpc != null && this.frontNpcs.size() >= 4;
      }
   }

   public NPC getNpc(int entityId) {
      synchronized (this.npcsLock) {
         if (!this.hasNpcs()) {
            return null;
         } else {
            for (NPC npc : this.frontNpcs) {
               if (npc.getID() == entityId) {
                  return npc;
               }
            }

            return this.mainNpc.getID() == entityId ? this.mainNpc : null;
         }
      }
   }

   public NPC getFrontEntityById(int entityId) {
      synchronized (this.npcsLock) {
         return this.frontEntities.stream().filter(npc -> npc.getID() == entityId).findFirst().orElse(null);
      }
   }

   public void spawnNpcs() {
      if (!this.hasNoStat("currentTarget") && this.player.getGameMode() != GameMode.SPECTATOR) {
         ReflexScheduler.sync()
            .run(
               ReflexRunnable.simple(
                  "npc-spawner-thread",
                  () -> {
                     synchronized (this.npcsLock) {
                        Random random = new Random();
                        Player randomPlayer = Bukkit.getOnlinePlayers().size() > 1 && Reflex.getReflex().getSetting("killaura.realistic_playerdata", true)
                           ? (Player)Bukkit.getOnlinePlayers().toArray()[random.nextInt(Bukkit.getOnlinePlayers().size())]
                           : null;

                        try {
                           this.despawnNpcs();
                           this.mainNpc = new NPC(this.player);
                           this.mainNpc.username = randomPlayer != null && this.player.canSee(randomPlayer) ? randomPlayer.getName() : Strings.randomUsername();
                           this.mainNpc.uuid = randomPlayer != null ? randomPlayer.getUniqueId() : UUID.randomUUID();
                           this.mainNpc.showInTab = true;
                           this.mainNpc.setVisible(true);
                           this.mainNpc.setLocation(Maths.calcMainNpcPosition(this, this.player, 0.0));
                           this.mainNpc.spawn();
                           this.mainNpc.setEquipmentInSlot(EquipSlot.MAIN_HAND, randomPlayer != null ? randomPlayer.getItemInHand() : Items.randomWeapon());
                           this.mainNpc.setEquipmentInSlot(EquipSlot.HELMET, randomPlayer != null ? Items.getHelmet(randomPlayer) : Items.randomHelmet());
                           this.mainNpc
                              .setEquipmentInSlot(EquipSlot.CHESTPLATE, randomPlayer != null ? Items.getChestplate(randomPlayer) : Items.randomChestplate());
                           this.mainNpc.setEquipmentInSlot(EquipSlot.LEGGINGS, randomPlayer != null ? Items.getLeggings(randomPlayer) : Items.randomLeggings());
                           this.mainNpc.setEquipmentInSlot(EquipSlot.BOOTS, randomPlayer != null ? Items.getBoots(randomPlayer) : Items.randomBoots());

                           for (int i = 0; i < 4; i++) {
                              NPC npc = new NPC(this.player);
                              npc.username = Strings.randomUsername();
                              npc.uuid = UUID.randomUUID();
                              npc.showInTab = false;
                              npc.setVisible(false);
                              npc.setLocation(this.getStat("currentTarget", LivingEntity.class).getLocation().clone().add(0.0, 1.5, 0.0));
                              npc.setHideNametag(true);
                              npc.spawn();
                              this.frontNpcs.add(npc);
                           }
                        } catch (Exception ex) {
                           ex.printStackTrace();
                        }

                        this.getStat("npcSpawnTracker", Timer.class).reset();
                     }
                  }
               )
            );
      }
   }

   public void despawnNpcs() {
      synchronized (this.npcsLock) {
         try {
            if (this.mainNpc != null) {
               try {
                  this.mainNpc.despawn();
                  this.mainNpc = null;
               } catch (Exception e) {
                  System.err.println("NPCs despawn error");
                  e.printStackTrace();
               }
            }

            if (this.frontNpcs != null) {
               for (NPC npc : (ArrayList<NPC>)((ArrayList<NPC>)this.frontNpcs).clone()) {
                  if (npc != null) {
                     npc.despawn();
                  }
               }

               this.frontNpcs.clear();
            }
         } catch (Exception cause) {
            cause.printStackTrace();
         }
      }
   }

   public void despawnFrontEntities() {
      synchronized (this.npcsLock) {
         for (NPC npc : this.frontEntities) {
            try {
               npc.despawn();
            } catch (InvocationTargetException ex) {
               ex.printStackTrace();
            }
         }

         this.frontEntities.clear();
         this.getStat("frontEntitiesDespawnTracker", Timer.class).reset();
      }
   }

   public boolean isFrontEntity(int entityId) {
      synchronized (this.npcsLock) {
         return this.frontEntities.stream().anyMatch(npc -> npc.getID() == entityId);
      }
   }

   public void spawnFrontEntities() {
      if (!this.hasNoStat("currentTarget") && this.player.getGameMode() != GameMode.SPECTATOR) {
         ReflexScheduler.sync().run(ReflexRunnable.simple("fe-spawner-thread", () -> {
            synchronized (this.npcsLock) {
               try {
                  this.despawnFrontEntities();
                  LivingEntity target = this.getStat("currentTarget", LivingEntity.class);
                  if (target == null) {
                     return;
                  }

                  Location[] positions = Maths.calcFrontEntityPositions(this.player, target);

                  for (int i = 0; i < 13; i++) {
                     NPC npc = new NPC(this.player);
                     npc.username = Strings.randomUsername();
                     npc.uuid = UUID.randomUUID();
                     npc.showInTab = false;
                     npc.setVisible(false);
                     npc.setLocation(positions[i]);
                     npc.setHideNametag(true);
                     npc.spawn();
                     this.frontEntities.add(npc);
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }));
      }
   }

   public boolean hasFrontEntities() {
      synchronized (this.npcsLock) {
         return this.frontEntities.size() >= 12;
      }
   }

   public boolean isNpc(int entityId) {
      synchronized (this.npcsLock) {
         return this.hasNpcs() && (this.frontNpcs.stream().anyMatch(npc -> entityId == npc.getID()) || this.isMainNpc(entityId));
      }
   }

   public boolean isMainNpc(int entityId) {
      synchronized (this.npcsLock) {
         return this.mainNpc != null && this.mainNpc.getID() == entityId;
      }
   }

   public void relocateAllNpcs() {
      synchronized (this.npcsLock) {
         this.updateNpcPositions();
         this.updateFrontEntityPositions();
      }
   }

   public void updateNpcPositions() {
      synchronized (this.npcsLock) {
         if (this.hasNpcs()) {
            if (this.player.getGameMode() == GameMode.SPECTATOR) {
               this.despawnNpcs();
            } else {
               Random random = new Random();
               if (this.getStat("npcSpawnTracker", Timer.class).hasPassed(60000L + (long)random.nextInt(10000))) {
                  this.despawnNpcs();
                  this.spawnNpcs();
               } else if (this.hasNpcs()) {
                  try {
                     double dist = Maths.distance(this.player.getLocation(), this.getStat("currentTarget", LivingEntity.class).getLocation());
                     Location base = this.hasStat("currentTarget") && dist < 5.5
                        ? this.getStat("currentTarget", LivingEntity.class).getLocation().clone()
                        : this.player.getLocation().clone();
                     Player randomPlayer = Bukkit.getOnlinePlayers().size() > 1
                        ? (Player)Bukkit.getOnlinePlayers().toArray()[random.nextInt(Bukkit.getOnlinePlayers().size())]
                        : null;
                     Location l0 = new Location(
                        this.player.getLocation().getWorld(),
                        this.player.getLocation().getX(),
                        this.player.getLocation().getY(),
                        this.player.getLocation().getZ(),
                        this.player.getLocation().getYaw() + 90.0F,
                        this.player.getLocation().getPitch()
                     );
                     Location l1 = new Location(
                        this.player.getLocation().getWorld(),
                        this.player.getLocation().getX(),
                        this.player.getLocation().getY(),
                        this.player.getLocation().getZ(),
                        this.player.getLocation().getYaw() - 90.0F,
                        this.player.getLocation().getPitch()
                     );
                     int maxNoCombat = Reflex.getReflex().getSetting("killaura.npc_max_nocombat", -1);
                     boolean outOfCombat = maxNoCombat > 0 && this.hasTimerPassed("attackTracker0", (long)maxNoCombat);
                     double fnpcOffset = Math.random() / 100.0
                        + (outOfCombat ? 7.3 : (Maths.matchIn(this.player.getLocation().getPitch(), -65.0F, 65.0F) ? 0.05 : 3.8));
                     this.frontNpcs
                        .get(0)
                        .setLocation(
                           base.clone()
                              .add(l0.getDirection().clone().multiply(this.getStat("attackTracker", Timer.class).hasPassed(1500L) ? 2.0 : 1.8))
                              .add(fnpcOffset, 0.4, fnpcOffset)
                        );
                     this.frontNpcs
                        .get(1)
                        .setLocation(
                           base.clone()
                              .add(l1.getDirection().clone().multiply(this.getStat("attackTracker", Timer.class).hasPassed(1500L) ? 2.0 : 1.6))
                              .add(fnpcOffset, 0.4, fnpcOffset)
                        );
                     this.frontNpcs
                        .get(2)
                        .setLocation(
                           l0.clone()
                              .add(l0.getDirection().clone().multiply(this.getStat("attackTracker", Timer.class).hasPassed(1500L) ? 2.0 : 1.6))
                              .add(fnpcOffset, 0.4, fnpcOffset)
                        );
                     this.frontNpcs
                        .get(3)
                        .setLocation(
                           l1.clone()
                              .add(l1.getDirection().clone().multiply(this.getStat("attackTracker", Timer.class).hasPassed(1500L) ? 2.0 : 1.6))
                              .add(fnpcOffset, 0.4, fnpcOffset)
                        );
                     this.mainNpc.setLocation(Maths.calcMainNpcPosition(this, this.player, fnpcOffset));
                     if (this.getStat("mainNpcEquipTracker", Timer.class).hasPassed(10000L) || !this.mainNpc.isVisible()) {
                        if (this.mainNpc.isVisible()) {
                           this.getStat("mainNpcEquipTracker", Timer.class).reset();
                        }

                        this.mainNpc
                           .setEquipmentInSlot(
                              EquipSlot.MAIN_HAND,
                              this.mainNpc.isVisible() ? (randomPlayer != null ? randomPlayer.getItemInHand() : Items.randomWeapon()) : null
                           );
                        this.mainNpc
                           .setEquipmentInSlot(
                              EquipSlot.HELMET, this.mainNpc.isVisible() ? (randomPlayer != null ? Items.getHelmet(randomPlayer) : Items.randomHelmet()) : null
                           );
                        this.mainNpc
                           .setEquipmentInSlot(
                              EquipSlot.CHESTPLATE,
                              this.mainNpc.isVisible() ? (randomPlayer != null ? Items.getChestplate(randomPlayer) : Items.randomChestplate()) : null
                           );
                        this.mainNpc
                           .setEquipmentInSlot(
                              EquipSlot.LEGGINGS,
                              this.mainNpc.isVisible() ? (randomPlayer != null ? Items.getLeggings(randomPlayer) : Items.randomLeggings()) : null
                           );
                        this.mainNpc
                           .setEquipmentInSlot(
                              EquipSlot.BOOTS, this.mainNpc.isVisible() ? (randomPlayer != null ? Items.getBoots(randomPlayer) : Items.randomBoots()) : null
                           );
                     }

                     for (NPC npc : this.frontNpcs) {
                        if (random.nextInt(5) == 0) {
                           npc.swingArm();
                        }
                     }

                     if (random.nextBoolean()) {
                        this.mainNpc.swingArm();
                     }

                     this.mainNpc.setHealth(Math.max(1.0F, (float)this.getStat("currentTarget", LivingEntity.class).getHealth()));

                     for (NPC npcx : this.frontNpcs) {
                        npcx.setHealth(Math.max(1.0F, (float)this.getStat("currentTarget", LivingEntity.class).getHealth()));
                     }

                     if (Reflex.getReflex().getSetting("killaura.npc_visibility_time", 2500) > 0
                        && !this.getStat("attackTracker", Timer.class)
                           .hasPassed((long)Reflex.getReflex().getSetting("killaura.npc_visibility_time", 2500).intValue())) {
                        this.mainNpc.setVisible(true);
                     } else {
                        this.mainNpc.setVisible(false);
                        this.getStat("mainNpcEquipTracker", Timer.class).setStartTime(System.currentTimeMillis() - 10000L);
                     }
                  } catch (Exception ex) {
                     if (ex instanceof NullPointerException) {
                        return;
                     }

                     if (Reflex.getReflex().getSetting("killaura.debug_exceptions", false)) {
                        ex.printStackTrace();
                     }

                     this.despawnNpcs();
                  }
               }
            }
         }
      }
   }

   public void updateFrontEntityPositions() {
      synchronized (this.npcsLock) {
         if (this.hasNpcs()) {
            if (this.player.getGameMode() == GameMode.SPECTATOR) {
               this.despawnNpcs();
            } else {
               this.accessTracker.reset();
               if (this.hasStat("currentTarget") && this.hasFrontEntities()) {
                  try {
                     Location[] positions = Maths.calcFrontEntityPositions(this.player, (Entity)this.getStat("currentTarget", LivingEntity.class));

                     for (int i = 0; i < 13; i++) {
                        this.frontEntities.get(i).setLocation(positions[i]);
                     }
                  } catch (Exception ignored) {
                  }
               }
            }
         }
      }
   }

   public static boolean isLagLenient() {
      return enableLagLeniency == null ? (enableLagLeniency = Reflex.getReflex().getSetting("general.lag_leniency", true)) : enableLagLeniency;
   }

   public boolean initLPcalc() {
      if (!isLagLenient()) {
         return false;
      } else if (this.initLPcalcDone) {
         return true;
      } else {
         this.setStat("lp_tickTracker", new Timer(0L));
         this.setStat("lp_tickDelays", new NumberStorage(20));
         this.initLPcalcDone = true;
         return false;
      }
   }

   public int getLagPoints() {
      MethodProfiler mp = new MethodProfiler("WJ-2139582");
      if (!isLagLenient()) {
         return 1;
      } else if (this.hasTimerTicked("lp_calcTracker", 5L) && !this.player.getLocation().equals(this.getLocation("lp_lastLoc"))) {
         Location loc = this.player.getLocation().clone();
         NumberStorage tickDelays;
         if (this.initLPcalc() && (tickDelays = this.getNumberStorage("lp_tickDelays")).isFull()) {
            List<Double> offsets = new ArrayList<>();

            for (int i = 0; i < tickDelays.size(); i++) {
               offsets.add((double)tickDelays.getLong(i).longValue() / 50.0);
            }

            Misc.sortDescending(offsets);
            double lp = 0.0;

            for (int i = 0; i < offsets.size(); i++) {
               lp += offsets.get(i) * Math.pow(0.965, (double)(i - 1));
            }

            double blc;
            lp = Math.max(1.0, Maths.validAndNz(blc = Maths.balance(offsets)) ? lp / blc : lp + 1000.0);
            int result = Maths.floor(lp);
            this.setStat("lagStatus", LagStatus.get(result));
            this.setStat("lagPoints", result);
            this.setStat("lp_lastLoc", loc);
            this.resetTimer("lp_calcTracker");
            mp.addToTimings();
            return result;
         } else {
            return 1;
         }
      } else {
         return this.getInt("lagPoints");
      }
   }

   public LagStatus getLagStatus() {
      if (!isLagLenient()) {
         return LagStatus.OK;
      } else if (!this.hasNoStat("lagStatus") && !this.hasTimerTicked("lagStatusUpdateTracker", 10L)) {
         return this.getStat("lagStatus", LagStatus.class);
      } else {
         this.resetTimer("lagStatusUpdateTracker");
         return this.setStat("lagStatus", LagStatus.get(this.getLagPoints()));
      }
   }

   private <T> T getDefaultValue(Class<T> type) {
      if (type == Integer.class || type == int.class) {
         return (T)Integer.valueOf(0);
      } else if (type == Long.class || type == long.class) {
         return (T)Long.valueOf(0L);
      } else if (type == Short.class || type == short.class) {
         return (T)Short.valueOf((short)0);
      } else if (type == Double.class || type == double.class) {
         return (T)Double.valueOf(0.0);
      } else if (type == Float.class || type == float.class) {
         return (T)Float.valueOf(0.0F);
      } else if (type == Byte.class || type == byte.class) {
         return (T)Byte.valueOf((byte)0);
      } else if (type == Boolean.class || type == boolean.class) {
         return (T)Boolean.valueOf(false);
      } else if (type == Timer.class) {
         return (T)new Timer().subtractTicks(5L);
      } else if (type == HashMap.class) {
         return (T)(new HashMap());
      } else if (type == String.class) {
         return (T)"";
      } else if (type == SafeLong.class) {
         return (T)(new SafeLong());
      } else {
         return (T)(type == LagStatus.class ? LagStatus.NOT_ESTIMATED : null);
      }
   }

   public int size(String key) {
      this.fill(key);
      return this.heuristicsData.get(key).size();
   }

   public void fill(String key) {
      if (!this.heuristicsData.containsKey(key) || this.heuristicsData.get(key).isEmpty()) {
         if (!this.heuristicsData.containsKey(key)) {
            this.heuristicsData.put(key, new ArrayList<>());
         }

         this.heuristicsData.get(key).add(0.0);
      }
   }

   public void writeHeuristicsData(String key, double value) {
      synchronized (this.heuristicsDataLock) {
         int heuristicsLength = 100;
         if (this.size(key) < 100) {
            this.heuristicsData.get(key).add(value);
            this.accessTracker.reset();
         }
      }
   }

   public HashMap<String, ArrayList<Double>> getHeuristicsData() {
      synchronized (this.heuristicsDataLock) {
         return this.heuristicsData;
      }
   }

   public void setHeuristicsData(HashMap<String, ArrayList<Double>> newData) {
      synchronized (this.heuristicsDataLock) {
         this.heuristicsData = (HashMap<String, ArrayList<Double>>)Misc.<String, ArrayList<Double>>copyMap(newData, false);
      }
   }

   public boolean hasStat(String name) {
      return this.stats.get(name) != null;
   }

   public boolean hasNoStat(String name) {
      return !this.hasStat(name);
   }

   public Object removeStat(String name) {
      return this.stats.remove(name);
   }

   public <T> T setStat(String name, T value) {
      synchronized (this.statsLock) {
         this.stats.put(name, value);
         this.accessTracker.reset();
         return value;
      }
   }

   public void setStatIf(String name, Supplier<Object> value, boolean b) {
      synchronized (this.statsLock) {
         if (b) {
            this.stats.put(name, value.get());
         }

         this.accessTracker.reset();
      }
   }

   public void copyStat(String from, String to) {
      synchronized (this.statsLock) {
         this.stats.put(to, this.stats.get(from));
         this.accessTracker.reset();
      }
   }

   public boolean isInWorld() {
      this.accessTracker.reset();
      int y = this.player.getLocation().getBlockY();
      return Maths.matchEx(y, 0, 256);
   }

   public boolean canBeChecked(Check check) {
      this.accessTracker.reset();
      if (this.isAdmin()) {
         return false;
      } else if (trigger == null) {
         return false;
      } else {
         try {
            return check.isEnabled()
               && this.isInWorld()
               && this.hasTimerPassed("joinTracker", 3000L)
               && !this.player.isFlying()
               && (this.player.getGameMode().equals(GameMode.SURVIVAL) || this.player.getGameMode().equals(GameMode.ADVENTURE))
               && !this.player.isDead()
               && !this.hasPermission("reflex.bypass." + check.getName().toLowerCase())
               && !this.hasPermission("reflex.bypass")
               && (!check.isLagSensetive() || !this.isLagging())
               && !this.getBoolean("canBypass" + check.getName())
               && this.hasTimerPassed("deathTracker", 800L);
         } catch (NullPointerException ignored) {
            return false;
         }
      }
   }

   public boolean isLagging() {
      return isLagLenient() && this.getLagStatus() == LagStatus.NOT_ESTIMATED;
   }

   public void setback() {
      this.setback(this.hasStat("lastGroundLoc") ? this.getStat("lastGroundLoc", Location.class) : this.player.getLocation().clone());
   }

   public void setback(Location loc) {
      if (!this.hasStat("setbackTracker")) {
         this.setStat("setbackTracker", new Timer(System.currentTimeMillis() - 1000L));
      }

      if (this.getStat("setbackTracker", Timer.class).hasPassed(750L)) {
         if (loc == null) {
            loc = this.player.getLocation().clone();
         }

         Location l = loc;
         this.syncTp(l);
         this.getStat("setbackTracker", Timer.class).reset();
         ReflexScheduler.sync().runLater(ReflexRunnable.simple("setback", () -> {
            this.player.teleport(l);
            ReflexScheduler.sync().runLater(ReflexRunnable.simple("setback-int", () -> {
               l.setYaw(l.getYaw() / 2.0F * (float)Math.random() + 3.141592F);
               l.setPitch(l.getPitch() * 2.0F / (float)Math.random() + 3.141592F);
               this.player.teleport(l);
            }), 5);
         }), 5);
      }
   }

   public boolean canExec(String command) {
      return this.hasPermission("reflex.command." + command.toLowerCase());
   }

   public boolean canReceiveVerbose() {
      return this.hasPermission("reflex.verbose") && this.getBoolean("isVerboseActive");
   }

   public boolean hasPermission(String s) {
      if (Reflex.getReflex().isCachingPermissions()) {
         if (!this.permissionCache.containsKey(s)) {
            this.permissionCache.put(s, this.player.hasPermission(s));
         }

         return this.permissionCache.get(s);
      } else {
         return this.player.hasPermission(s);
      }
   }

   public int getPing() {
      if (!this.getTimerWithOffset("pingTracker", 3000L).hasPassed(2500L)) {
         return this.getInt("lastPing");
      } else {
         try {
            Object handle = this.player.getClass().getMethod("getHandle").invoke(this.player);
            this.getTimer("pingTracker").reset();
            return (Integer)this.setStat("lastPing", (Integer)handle.getClass().getDeclaredField("ping").get(handle));
         } catch (Exception ignored) {
            this.getTimer("pingTracker").reset();
            return this.setStat("lastPing", -1);
         }
      }
   }

   public void kick(String message) {
      boolean kill = Reflex.getReflex().getSetting("general.reflex_kick.kill", true);
      boolean strikeLightning = Reflex.getReflex().getSetting("general.reflex_kick.strike_lightning", true);
      if (kill) {
         ReflexScheduler.sync().run(ReflexRunnable.simple("KillerThread", () -> this.player.setHealth(0.0)));
      }

      if (strikeLightning) {
         this.player.getWorld().strikeLightningEffect(this.player.getLocation().clone().add(0.0, 1.0, 0.0));
      }

      try {
         AsyncCatcher.catchOp("");
         this.player.kickPlayer("\n" + message + "\n");
      } catch (IllegalStateException ignoredEx) {
         ReflexScheduler.sync().run(ReflexRunnable.simple("synced-kick", () -> this.player.kickPlayer("\n" + message + "\n")));
      }
   }

   public void ban(TimeUnit unit, long duration, String message) {
      try {
         AsyncCatcher.catchOp("");
         this.kick(message);
         Reflex.getReflex().banPlayer(this.player, message, duration, unit);
      } catch (IllegalStateException ignored) {
         ReflexScheduler.sync().run(ReflexRunnable.simple("synced-ban", () -> {
            this.kick(message);
            Reflex.getReflex().banPlayer(this.player, message, duration, unit);
         }));
      }
   }

   public boolean hasSpeedEffect() {
      return this.hasEffect(PotionEffectType.SPEED);
   }

   public float getSpeedAmpl() {
      return this.getEffectAmpl(PotionEffectType.SPEED);
   }

   public boolean hasJumpEffect() {
      return this.hasEffect(PotionEffectType.JUMP);
   }

   public float getJumpAmpl() {
      return this.getEffectAmpl(PotionEffectType.JUMP);
   }

   public boolean hasSlowFallingEffect() {
      return VersionUtils.getServerVersion().hasMinimum(MCVersion.V1_13_X) && this.player.hasPotionEffect(PotionEffectType.SLOW_FALLING);
   }

   public boolean hasEffect(PotionEffectType pet) {
      return this.getEffectAmpl(pet) > 0.0F;
   }

   public float getEffectAmpl(PotionEffectType pet) {
      return this.player
         .getActivePotionEffects()
         .stream()
         .filter(effect -> effect.getType().equals(pet))
         .findFirst()
         .map(effect -> (float)effect.getAmplifier() + 1.0F)
         .orElse(0.0F);
   }

   public int getPingTicks() {
      return Maths.floor((double)this.getPing() / 50.0);
   }

   @Override
   public Object clone() {
      return null;
   }

   public ReflexPlayerData getTransferableAPI() {
      return new ReflexPlayerData(
         new TransferablePlayerData(
            new TransferablePlayerData.InnerObj(Misc.copyMap(this.violations, true)),
            new TransferablePlayerData.InnerObj(Misc.copyMap(this.heuristicsData, false))
         )
      );
   }

   public void transfer(Object o) {
      TransferablePlayerData tpd = Misc.castSafely(o);
      this.setViolations((ConcurrentHashMap<Cheat, Integer>)Misc.<Cheat, Integer>copyMap((ConcurrentHashMap)tpd.getViolations().getInner(), true));
      this.setHeuristicsData((HashMap<String, ArrayList<Double>>)Misc.<String, ArrayList<Double>>copyMap((HashMap)tpd.getHeuristicsData().getInner(), false));
   }

   public static ConcurrentHashMap<Player, Stats> getStatlist() {
      return statlist;
   }

   public HashMap<String, Object> getStats() {
      return this.stats;
   }

   public ConcurrentHashMap<Cheat, Integer> getViolations() {
      return this.violations;
   }

   public Map<String, Boolean> getPermissionCache() {
      return this.permissionCache;
   }

   public Player getPlayer() {
      return this.player;
   }

   public NPC getMainNpc() {
      return this.mainNpc;
   }

   public boolean isAdmin() {
      return this.admin;
   }

   static {
      ADMIN.admin = true;
   }
}
