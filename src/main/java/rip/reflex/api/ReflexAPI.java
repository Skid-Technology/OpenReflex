package rip.reflex.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.flo.flogger.impl.DailyLogFilePublisher;
import me.flo.flogger.types.FlogLevel;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.api.db.BanImpl;
import rip.reflex.api.db.DataImpl;
import rip.reflex.api.db.DatabaseClient;
import rip.reflex.util.misc.Validate;

public class ReflexAPI {
   public static final int API_VERSION = 6;
   private static boolean init = false;
   public static Map<String, String> theme = new ConcurrentHashMap<>();
   private boolean usable = true;
   private final Reflex reflex;

   ReflexAPI(Reflex reflex) {
      this.reflex = Validate.notNull(reflex, "ReflexAPI cannot be instantiated without Reflex");
      if (init) {
         this.usable = false;
      } else {
         init = true;
      }
   }

   private void checkUsable() {
      if (!this.usable) {
         throw new IllegalStateException("ReflexAPI cannot be instantiated twice! Please use ReflexAPIProvider#getAPI to obtain the working API instance");
      } else if (Reflex.getApiWrapper() == null) {
         throw new IllegalStateException("ReflexAPI was not initialized yet (has Reflex successfully started?)");
      }
   }

   public String memoryDump() {
      this.checkUsable();
      return Reflex.getApiWrapper().memoryDump();
   }

   public long[] getMemory() {
      this.checkUsable();
      return Reflex.getApiWrapper().getMemory();
   }

   public long getMemoryUsedEstimate() {
      this.checkUsable();
      return Reflex.getApiWrapper().getMemoryUsedEstimate();
   }

   public ReflexAPI allowHacking(Player p, Cheat cheat, boolean allow) {
      this.checkUsable();
      return Reflex.getApiWrapper().allowHacking(p, cheat, allow);
   }

   public boolean canBypass(Player p, Cheat cheat) {
      this.checkUsable();
      return Reflex.getApiWrapper().canBypass(p, cheat);
   }

   public ReflexAPI transferViolations(Player from, Player to) {
      this.checkUsable();
      return Reflex.getApiWrapper().transferViolations(from, to);
   }

   public ReflexAPI transferHeuristicsData(Player from, Player to) {
      this.checkUsable();
      return Reflex.getApiWrapper().transferHeuristicsData(from, to);
   }

   public ReflexAPI transfer(Player from, Player to) {
      this.transferViolations(from, to);
      this.transferHeuristicsData(from, to);
      return this;
   }

   public ReflexPlayerData getPlayerData(Player p) {
      this.checkUsable();
      return Reflex.getApiWrapper().getPlayerData(p);
   }

   public ReflexAPI setPlayerData(Player p, ReflexPlayerData data) {
      this.checkUsable();
      return Reflex.getApiWrapper().setPlayerData(p, data);
   }

   public double getTps() {
      this.checkUsable();
      return Reflex.getApiWrapper().getTps();
   }

   public int getPing(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getPing(player);
   }

   public String getMCBrand(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getMCBrand(player);
   }

   public String getClientLocale(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getClientLocale(player);
   }

   public int getProtocolVersion(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getProtocolVersion(player);
   }

   public int getLagPoints(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getLagPoints(player);
   }

   public String getLagStatus(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getLagStatus(player);
   }

   public int getViolations(Player player, Cheat cheat) {
      this.checkUsable();
      return Reflex.getApiWrapper().getViolations(player, cheat);
   }

   public ReflexAPI setViolations(Player player, Cheat cheat, int violations) {
      this.checkUsable();
      return Reflex.getApiWrapper().setViolations(player, cheat, violations);
   }

   public BanInfo getBanInfo(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().getBanInfo(player);
   }

   public ReflexAPI unbanPlayer(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().unbanPlayer(player);
   }

   public ReflexAPI banPlayer(Player player, String message, long banTime, TimeUnit timeUnit) {
      this.checkUsable();
      return Reflex.getApiWrapper().banPlayer(player, message, banTime, timeUnit);
   }

   public boolean isPlayerBanned(Player player) {
      this.checkUsable();
      return Reflex.getApiWrapper().isPlayerBanned(player);
   }

   public ReflexAPI log(FlogLevel lvl, String message) {
      this.checkUsable();
      return Reflex.getApiWrapper().log(lvl, message);
   }

   public int getBanCount() {
      this.checkUsable();
      return Reflex.getApiWrapper().getBanCount();
   }

   public int getBanCountIn(TimeUnit unit, long time) {
      this.checkUsable();
      return Reflex.getApiWrapper().getBanCountIn(unit, time);
   }

   public BanImpl getBanImpl() {
      this.checkUsable();
      return Reflex.getApiWrapper().getBanImpl();
   }

   public DatabaseClient getDBClient() {
      this.checkUsable();
      return Reflex.getApiWrapper().getDBClient();
   }

   public DailyLogFilePublisher getLogImpl() {
      this.checkUsable();
      return Reflex.getApiWrapper().getLogImpl();
   }

   public DataImpl getDataImpl() {
      this.checkUsable();
      return Reflex.getApiWrapper().getDataImpl();
   }

   public boolean isCheckEnabled(Cheat c) {
      this.checkUsable();
      return Reflex.getApiWrapper().isCheckEnabled(c);
   }

   public boolean isCheckDisabled(Cheat c) {
      this.checkUsable();
      return Reflex.getApiWrapper().isCheckDisabled(c);
   }

   public ReflexAPI enableCheck(Cheat c) {
      this.checkUsable();
      return Reflex.getApiWrapper().enableCheck(c);
   }

   public ReflexAPI disableCheck(Cheat c) {
      this.checkUsable();
      return Reflex.getApiWrapper().disableCheck(c);
   }

   public ReflexAPI toggleCheck(Cheat c) {
      this.checkUsable();
      return Reflex.getApiWrapper().toggleCheck(c);
   }

   public ReflexAPI enableComponent(String name) {
      this.checkUsable();
      return Reflex.getApiWrapper().enableComponent(name);
   }

   public ReflexAPI disableComponent(String name) {
      this.checkUsable();
      return Reflex.getApiWrapper().disableComponent(name);
   }

   public boolean isComponentEnabled(String name) {
      this.checkUsable();
      return Reflex.getApiWrapper().isComponentEnabled(name);
   }

   public boolean isComponentDisabled(String name) {
      this.checkUsable();
      return Reflex.getApiWrapper().isComponentDisabled(name);
   }

   public ReflexAPI reset(Player p) {
      this.checkUsable();
      return Reflex.getApiWrapper().reset(p);
   }

   public ReflexAPI reloadConfig() {
      this.checkUsable();
      return Reflex.getApiWrapper().reloadConfig();
   }

   public ReflexAPI reloadPermissionCaches() {
      this.checkUsable();
      return Reflex.getApiWrapper().reloadPermissionCaches();
   }

   public ReflexAPI reloadTheme() {
      this.checkUsable();
      return Reflex.getApiWrapper().reloadTheme();
   }

   public ReflexAPI reloadAll() {
      this.checkUsable();
      return Reflex.getApiWrapper().reloadAll();
   }

   public String translateThemeString(String key) {
      this.checkUsable();
      return Reflex.getApiWrapper().translateThemeString(key);
   }

   public String translateAndFormat(String key) {
      this.checkUsable();
      return Reflex.getApiWrapper().translateAndFormat(key);
   }

   public String translateAndFormat(String prefix, String key) {
      this.checkUsable();
      return Reflex.getApiWrapper().translateAndFormat(prefix, key);
   }

   public Map<String, String> getTheme() {
      this.checkUsable();
      return theme;
   }

   public boolean isConnectedToCloud() {
      this.checkUsable();
      return Reflex.getApiWrapper().isConnectedToCloud();
   }

   public ReflexAPI connectToCloud(String key) {
      this.checkUsable();
      return Reflex.getApiWrapper().connectToCloud(key);
   }

   public int getPingToCloud() {
      this.checkUsable();
      return Reflex.getApiWrapper().getPingToCloud();
   }

   public String reflexVersion() {
      this.checkUsable();
      return Reflex.getApiWrapper().reflexVersion();
   }

   public int nnVersion() {
      this.checkUsable();
      return Reflex.getApiWrapper().nnVersion();
   }

   public int apiVersion() {
      this.checkUsable();
      return Reflex.getApiWrapper().apiVersion();
   }

   public int cloudProtocolVersion() {
      this.checkUsable();
      return Reflex.getApiWrapper().cloudProtocolVersion();
   }

   public String getReflexPrefix() {
      this.checkUsable();
      return Reflex.getApiWrapper().getReflexPrefix();
   }
}
