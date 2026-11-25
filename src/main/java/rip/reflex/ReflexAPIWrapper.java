package rip.reflex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.flo.flogger.impl.DailyLogFilePublisher;
import me.flo.flogger.types.FlogLevel;
import org.bukkit.entity.Player;
import rip.reflex.api.BanInfo;
import rip.reflex.api.Cheat;
import rip.reflex.api.ReflexAPI;
import rip.reflex.api.ReflexPlayerData;
import rip.reflex.api.db.BanImpl;
import rip.reflex.api.db.DataImpl;
import rip.reflex.api.db.DatabaseClient;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.misc.Validate;
import rip.reflex.util.performance.Memory;
import rip.reflex.util.protocol.VersionUtils;

public class ReflexAPIWrapper {
   public static Map<String, String> theme = new ConcurrentHashMap<>();
   private final Reflex reflex;
   private final ReflexAPI api;

   public String memoryDump() {
      return Memory.dump0();
   }

   public long[] getMemory() {
      return Memory.getMemory();
   }

   public long getMemoryUsedEstimate() {
      return Memory.getMemoryUsed();
   }

   public ReflexAPI allowHacking(Player p, Cheat cheat, boolean allow) {
      Stats.of(validatePlayer(p)).setStat("canBypass" + validateCheat(cheat).toString(), allow);
      return this.api;
   }

   public boolean canBypass(Player p, Cheat cheat) {
      return Stats.of(validatePlayer(p)).getBoolean("canBypass" + validateCheat(cheat).toString());
   }

   public ReflexAPI transferViolations(Player from, Player to) {
      if (validatePlayer(from).equals(validatePlayer(to))) {
         throw new IllegalArgumentException("Pointless (self) violations transfer");
      } else {
         this.reflex.transferViolations(from, to);
         return this.api;
      }
   }

   public ReflexAPI transferHeuristicsData(Player from, Player to) {
      if (validatePlayer(from).equals(validatePlayer(to))) {
         throw new IllegalArgumentException("Pointless (self) heuristics data transfer");
      } else {
         this.reflex.transferHeuristicsData(from, to);
         return this.api;
      }
   }

   public ReflexAPI transfer(Player from, Player to) {
      this.transferViolations(from, to);
      this.transferHeuristicsData(from, to);
      return this.api;
   }

   public ReflexPlayerData getPlayerData(Player p) {
      return this.reflex.getTransferable(validatePlayer(p));
   }

   public ReflexAPI setPlayerData(Player p, ReflexPlayerData data) {
      this.reflex.transferPlayerData(validatePlayer(p), Validate.notNull(data, "ReflexPlayerData cannot be null"));
      return this.api;
   }

   public double getTps() {
      return this.reflex.getTpsMeter().getTps();
   }

   public int getPing(Player player) {
      return Stats.of(validatePlayer(player)).getPing();
   }

   public String getMCBrand(Player player) {
      return Stats.of(validatePlayer(player)).getString("mcClientBrand");
   }

   public String getClientLocale(Player player) {
      return Stats.of(validatePlayer(player)).getString("mcClientLocale");
   }

   public int getProtocolVersion(Player player) {
      return VersionUtils.getClientVersion(validatePlayer(player)).getVerId();
   }

   public int getLagPoints(Player player) {
      return Stats.of(validatePlayer(player)).getLagPoints();
   }

   public String getLagStatus(Player player) {
      return Stats.of(validatePlayer(player)).getLagStatus().fancyName();
   }

   public int getViolations(Player player, Cheat cheat) {
      return Stats.of(player).getViolations(cheat);
   }

   public ReflexAPI setViolations(Player player, Cheat cheat, int violations) {
      Stats.of(validatePlayer(player)).setViolations(validateCheat(cheat), Maths.limit(violations, 0, 1000000));
      return this.api;
   }

   public BanInfo getBanInfo(Player player) {
      return this.reflex.getBanInfo(validatePlayer(player));
   }

   public ReflexAPI unbanPlayer(Player player) {
      this.reflex.unbanPlayer(validatePlayer(player));
      return this.api;
   }

   public ReflexAPI banPlayer(Player player, String message, long banTime, TimeUnit timeUnit) {
      this.reflex
         .banPlayer(
            validatePlayer(player), Validate.notNull(message, "Message cannot be null"), banTime, Validate.notNull(timeUnit, "Time unit cannot be null")
         );
      return this.api;
   }

   public boolean isPlayerBanned(Player player) {
      return this.getBanInfo(validatePlayer(player)).getBanTime() != 0L;
   }

   public ReflexAPI log(FlogLevel lvl, String message) {
      this.reflex.log(Validate.notNull(lvl, "Level cannot be null"), Validate.notNull(message, "Message cannot be null"));
      return this.api;
   }

   public int getBanCount() {
      return this.reflex.getBanCount();
   }

   public int getBanCountIn(TimeUnit unit, long time) {
      return this.reflex.getBanCountIn(Validate.notNull(unit, "Time unit cannot be null"), time);
   }

   public BanImpl getBanImpl() {
      return this.reflex.getBanImpl();
   }

   public DatabaseClient getDBClient() {
      return this.reflex.getClient();
   }

   public DailyLogFilePublisher getLogImpl() {
      return this.reflex.getDlogp();
   }

   public DataImpl getDataImpl() {
      return this.reflex.getDataImpl();
   }

   public boolean isCheckEnabled(Cheat c) {
      try {
         return CheckRegistry.getCheck(validateCheat(c)).isEnabled();
      } catch (Exception ex) {
         throw new RuntimeException("Failed to find check " + c.toString() + ". Was it loaded? o_O", ex);
      }
   }

   public boolean isCheckDisabled(Cheat c) {
      try {
         return CheckRegistry.getCheck(validateCheat(c)).isDisabled();
      } catch (Exception e) {
         throw new RuntimeException("Failed to find check " + c.toString() + ". Was it loaded? o_O", e);
      }
   }

   public ReflexAPI enableCheck(Cheat c) {
      try {
         CheckRegistry.getCheck(validateCheat(c)).enable();
      } catch (Exception err) {
         throw new RuntimeException("Failed to find check " + c.toString() + ". Was it loaded? o_O", err);
      }

      return this.api;
   }

   public ReflexAPI disableCheck(Cheat c) {
      try {
         CheckRegistry.getCheck(validateCheat(c)).disable();
      } catch (Exception ignored) {
         throw new RuntimeException("Failed to find check " + c.toString() + ". Was it loaded? o_O");
      }

      return this.api;
   }

   public ReflexAPI toggleCheck(Cheat c) {
      try {
         CheckRegistry.getCheck(validateCheat(c)).toggle();
      } catch (Exception ex) {
         throw new RuntimeException("Failed to find check " + c.toString() + ". Was it loaded? o_O", ex);
      }

      return this.api;
   }

   public ReflexAPI enableComponent(String name) {
      Check.enableComponent(Validate.notNull(name, "Component name cannot be null"));
      return this.api;
   }

   public ReflexAPI disableComponent(String name) {
      Check.disableComponent(Validate.notNull(name, "Component name cannot be null"));
      return this.api;
   }

   public boolean isComponentEnabled(String name) {
      return Check.isComponentEnabled(Validate.notNull(name, "Component name cannot be null"));
   }

   public boolean isComponentDisabled(String name) {
      return Check.isComponentDisabled(Validate.notNull(name, "Component name cannot be null"));
   }

   public ReflexAPI reset(Player p) {
      this.reflex.reset(validatePlayer(p));
      return this.api;
   }

   public ReflexAPI reloadConfig() {
      this.reflex.reloadConfig();
      return this.api;
   }

   public ReflexAPI reloadPermissionCaches() {
      LoopHelper.loopStats(Stats::reloadPermissionCache);
      return this.api;
   }

   public ReflexAPI reloadTheme() {
      this.reflex.reloadTheme(false);
      return this.api;
   }

   public ReflexAPI reloadAll() {
      this.reloadPermissionCaches();
      this.reloadConfig();
      return this.api;
   }

   public String translateThemeString(String key) {
      return theme.getOrDefault(key, key);
   }

   public String translateAndFormat(String key) {
      return this.translateAndFormat(this.getReflexPrefix(), key);
   }

   public String translateAndFormat(String prefix, String key) {
      return Strings.color(prefix + this.translateThemeString(key));
   }

   public boolean isConnectedToCloud() {
      return this.reflex.getCloudService().isConnected();
   }

   public ReflexAPI connectToCloud(String key) {
      this.reflex.getCloudService().connect(Validate.strNotEmpty(Validate.notNull(key, "Key cannot be null"), "Key must not be empty"));
      return this.api;
   }

   public int getPingToCloud() {
      return this.reflex.getCloudService().getPing();
   }

   public String reflexVersion() {
      return this.reflex.getDescription().getVersion();
   }

   public int nnVersion() {
      return this.reflex.getDataVersion();
   }

   public int apiVersion() {
      return 6;
   }

   public int cloudProtocolVersion() {
      return 3;
   }

   public String getReflexPrefix() {
      return Reflex.getTheme().getPrefix();
   }

   private static Player validatePlayer(Player p) {
      return Validate.playerOnline(Validate.notNull(p, "Player cannot be null"), "Player must be online");
   }

   private static Cheat validateCheat(Cheat c) {
      return Validate.notNull(c, "Cheat cannot be null");
   }

   public int _i(String s) {
      return Strings.xorHash(s);
   }

   public ReflexAPIWrapper(Reflex reflex, ReflexAPI api) {
      this.reflex = reflex;
      this.api = api;
   }

   public static Map<String, String> getTheme() {
      return theme;
   }
}
