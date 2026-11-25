package rip.reflex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import me.flo.flogger.Flogger4J;
import me.flo.flogger.impl.AsyncLogWriterPublisher;
import me.flo.flogger.impl.DailyLogFilePublisher;
import me.flo.flogger.impl.LogConsolePublisher;
import me.flo.flogger.impl.LogWriterPublisher;
import me.flo.flogger.types.FlogFormatter;
import me.flo.flogger.types.FlogLevel;
import me.flo.flogger.types.FlogRecord;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import rip.reflex.api.BanInfo;
import rip.reflex.api.ReflexAPI;
import rip.reflex.api.ReflexAPIProvider;
import rip.reflex.api.ReflexPlayerData;
import rip.reflex.api.db.BanImpl;
import rip.reflex.api.db.DataImpl;
import rip.reflex.api.db.DatabaseClient;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckLoader;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.cloud.CloudComputer;
import rip.reflex.cloud.CloudService;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexCommand;
import rip.reflex.command.reflex.CommandReflex;
import rip.reflex.java9.J9Util;
import rip.reflex.listener.CentralEventListener;
import rip.reflex.listener.ExtremeLoginListener;
import rip.reflex.listener.FuckingEventListener;
import rip.reflex.listener.base.EventLoader;
import rip.reflex.listener.base.EventRegistry;
import rip.reflex.listener.impl.PlayerVelocityListener;
import rip.reflex.moonlight.ReflexMoonlight;
import rip.reflex.networking.BestServersFile;
import rip.reflex.networking.ReflexNetworking;
import rip.reflex.networking.ReflexServer;
import rip.reflex.util.appearance.Theme;
import rip.reflex.util.appearance.ThemeManager;
import rip.reflex.util.data.Stats;
import rip.reflex.util.db.DatabaseLoader;
import rip.reflex.util.db.DatabaseType;
import rip.reflex.util.db.logs.MySQLLogUtil;
import rip.reflex.util.debugging.Debugger;
import rip.reflex.util.debugging.PacketDebugger;
import rip.reflex.util.location.Movement;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.AdvCfgLoader;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.mod.DependencyChecker;
import rip.reflex.util.mod.badlionclient.BadlionConfigMaker;
import rip.reflex.util.net.ssl.HackedTrustManager;
import rip.reflex.util.performance.PlayerPacketBasedServerPerformanceTracker;
import rip.reflex.util.performance.ServerPerformanceTracker;
import rip.reflex.util.performance.TPSMeter;
import rip.reflex.util.protocol.VersionUtils;
import rip.reflex.util.protocol.mcaccess.MCAccess;
import rip.reflex.util.protocol.npc.NPCCommons;
import rip.reflex.util.protocol.npc.NPCTeamTricks;
import rip.reflex.util.protocol.packet.Packets;
import rip.reflex.util.protocol.protocolsupport.ProtocolSupportHook;
import rip.reflex.util.protocol.viaversion.ViaVersionHook;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;
import sun.net.www.protocol.https.Handler;

public class Reflex extends JavaPlugin {
   private boolean initDone;
   private static Reflex instance;
   private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
   private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
   private final SplittableRandom random = new SplittableRandom();
   private final Map<String, Object> configCache = new ConcurrentHashMap<>();
   private static Theme theme;
   private static Map<String, String> rawTheme;
   private String workingDir;
   private volatile boolean disableClientCheckingDueToBugs;
   private TPSMeter tpsMeter;
   private DatabaseType usedDB;
   private ReflexServer syncServer;
   private ReflexServer cloudServer;
   public static int[] vrf = new int[20];
   private static final String[] _apiAccess = new String[5];
   private DatabaseClient client;
   private DataImpl dataImpl;
   private BanImpl banImpl;
   public int _r_ = 6;
   private int dataVersion;
   private boolean cachingConfig;
   private static Object[] objs;
   private static Class c;
   private boolean cachingPermissions;
   private boolean cachingPackets;
   private CloudService cloudService;
   private ExecutorService executor;
   private int oldThreadCount;
   private Flogger4J flogger;
   private ReflexCommand cmdExec;
   private DailyLogFilePublisher dlogp;
   private String cloudHost;
   private ReflexMoonlight moonlight;
   private boolean unlockAdvancedSettings;
   private FileConfiguration advcfg;
   private static volatile boolean needCheckNewAP = true;
   private final List<ServerPerformanceTracker> serverPerformanceTrackers = new ArrayList<>();
   private final Timer generalPlayerPacketReceiveTracker = new Timer(Long.MAX_VALUE);
   private final Timer singleServerLagTracker = new Timer(0L);
   private long beganStartup;
   public float verify = -21.8F;
   public boolean offlineMode;
   private static ReflexAPIWrapper apiWrapper;
   private ExtremeLoginListener extremeLoginListener;

   private static void loadConfig0() {
   }

   public static void setApiWrapper(ReflexAPIWrapper wrapper) {
      if (apiWrapper == null) {
         apiWrapper = wrapper;
      }
   }

   public static Reflex getReflex() {
      return instance;
   }

   public static float setInstance(Reflex instance) {
      Arrays.stream(Reflex.class.getDeclaredFields()).filter(f -> f.getType().equals(Reflex.class)).forEach(f -> {
         try {
            f.setAccessible(true);
            f.set(null, instance);
         } catch (IllegalAccessException ignored) {
            System.out.println("[Reflex] ERROR STARTING UP: 1");
         }
      });
      byte desc = Byte.valueOf(Strings.xorHash(instance.getDescription().getDescription()) == vrf[12] ? "6" : "2");
      byte name = Byte.valueOf(Strings.xorHash(instance.getDescription().getName()) == vrf[7] ? "-4" : "6");
      byte main = Byte.valueOf(Strings.xorHash(instance.getDescription().getMain()) != vrf[18] ? "7" : "3");
      byte authors = Byte.valueOf(Strings.xorHash(instance.getDescription().getAuthors().toString()) == vrf[5] ? "1" : "-4");
      byte website = Byte.valueOf(Strings.xorHash(instance.getDescription().getWebsite()) != vrf[9] ? "2" : "7");
      byte res = J9Util.m3((byte)(-(desc + name + main + authors + website)));
      if (res == 0) {
         throw new IllegalStateException();
      } else {
         return String.valueOf(J9Util.getYml(instance._r_)).equals(Integer.toString(vrf[4]))
               && instance.getDescription().getCommands().hashCode() == vrf[3]
               && instance.getPluginPermissions() == vrf[1]
               && res == vrf[16]
            ? 21.8F
            : -21.8F;
      }
   }

   public void onEnable() {
      this.beganStartup = System.currentTimeMillis();
      if (setInstance(this) != -this.verify) {
         Bukkit.getPluginManager().disablePlugin(this);
         instance = null;
         throw new NullPointerException();
      } else {
         this.extremeLoginListener = new ExtremeLoginListener();
         Bukkit.getPluginManager().registerEvents(this.extremeLoginListener, this);
         this.workingDir = Strings.wrap(this.getConfig().getString("general.working_directory"))
            .replace("%default_data_folder%", this.getDataFolder().getAbsolutePath())
            .toString();

         try {
            File wd = new File(this.workingDir);
            if (!wd.exists()) {
               wd.mkdir();
            }

            this.initLogger();
         } catch (IOException ex) {
            ex.printStackTrace();
         }

         Debugger.debugIfUsed(this, "-a");
         DependencyChecker.put("PaperLibs (Google/Gson)", () -> Class.forName("com.google.gson.Gson") != null);
         DependencyChecker.put("PaperLibs (Google/Common/IO)", () -> Class.forName("com.google.common.io.ByteStreams") != null);
         DependencyChecker.put("ProtocolLib", () -> Class.forName("com.comphenix.protocol.ProtocolLibrary") != null);
         DependencyChecker.checkDependencies();
         if (this.getSetting("general.ssl_hack", false)) {
            HackedTrustManager.get().inject();
         }

         this.saveDefaultConfig();
         Bukkit.getScheduler().runTaskAsynchronously(this, this::init);
      }
   }

   public void onDisable() {
      this.executor.shutdown();

      try {
         CentralEventListener.setStopped(true);
         EventRegistry.unregisterAll(this);
      } catch (Exception ignored) {
      }

      if (this.getSetting("general.kick_on_reload", true)) {
         this.getLogger().info("Reflex is disabling. Kicking all online players to avoid issues");
         if (Bukkit.isPrimaryThread()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
               player.kickPlayer(Strings.rformatNoPrefix(this.getTheme(player).getServerRestarting()));
            }
         }
      }

      if (this.usedDB != null && this.usedDB == DatabaseType.MYSQL) {
         Field fWriter = MCAccess.getField(LogWriterPublisher.class, "writer");
         if (fWriter != null) {
            Writer writer = (Writer)MCAccess.getFromField(fWriter, this.dlogp);

            try {
               writer.close();
            } catch (IOException ex) {
               System.err.println("[R51979] Writer close failed");
               ex.printStackTrace();
            }

            MySQLLogUtil.save(this.dlogp.getCurrentFile());
         }
      }

      this.banImpl.off();
   }

   protected void initDone() {
      if (getReflex().getConfig().getBoolean("general.ssl_hack")) {
         HackedTrustManager.get().uninject();
      }

      NPCTeamTricks.init();
      this.initDone = true;
      long cur = System.currentTimeMillis();
      long took = cur - this.beganStartup;
      double sec = Maths.round((double)took / 1000.0, 2);
      this.info("Max thread count is " + Maths.limit(this.getSetting("general.max_thread_count", 24), 1, 100) + "<-" + this.oldThreadCount);
      this.info("Full startup took " + sec + " seconds");
      if (!this.getSetting("general.disable_welcome_message", false)) {
         this.info("Hello and thanks for choosing Reflex!");
         this.info(
            "We really appreciate all our customers, and thus want you to be having as much pleasure using Reflex as possible. We are glad to hear any suggestions, bug reports and advices to make our plugin better."
         );
         this.info("But! Reviews and the Spigot discussion page are NOT the right place to report something!");
         this.info("Please use our issue tracker (go.reflex.rip/bug) or Discord (https://g.reflex.rip/discord) for that.");
         this.info("We like you — and if you like Reflex, please don't idle about making us happy by leaving a nice review!");
      }

      this.downloadOrCopyLegalFiles();
      this.warn(
         "By using Reflex you agree to its Terms of Use (https://g.reflex.rip/tos) and Privacy Policy (http://g.reflex.rip/privacy). The legal files can also be found inside the JAR and in Reflex's working directory: "
            + Strings.replace(this.workingDir, "%default%", ".../plugins/Reflex/")
      );
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, this.cmdExec::verifyCommandMonopoly, 6000L);
   }

   private void init() {
      Debugger.debugIfUsed(this, "0");
      String configuredServer = this.getSetting("general.server", "auto").toLowerCase().trim();
      switch (configuredServer) {
         case "auto":
            try {
               BestServersFile bestServers = new BestServersFile(this);
               if (bestServers.isValid()) {
                  this.syncServer = bestServers.getSyncServer();
                  this.cloudServer = bestServers.getCloudServer();
               } else {
                  this.info("Finding best servers. Please allow up to 30 seconds...");
                  this.syncServer = ReflexNetworking.findSyncServer();
                  this.cloudServer = ReflexNetworking.findCloudServer();
                  BestServersFile.create(this);
               }
            } catch (Exception ex) {
               this.warn("Failed to find best server (as per general.server=auto). Stacktrace:");
               ex.printStackTrace();
               this.warn("Falling back to Reflex's primary server (EU). Please report this issue!");
            }
            break;
         case "eu":
            this.syncServer = this.cloudServer = ReflexServer.EU;
            break;
         case "usa":
         case "china":
         case "usa_china":
            this.syncServer = this.cloudServer = ReflexServer.USA;
            break;
         default:
            this.warn("Invalid server (general.server) value in config.yml: '" + configuredServer + " '. Allowed values:");
            this.warn("Check configuration comments to general.server for details.");
            this.warn("Reflex will disable. Please fix this issue!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
      }

      Debugger.debugIfUsed(this, "a");
      ReflexScheduler.Schedulers.init(this);
      this.cloudService = new CloudService(this);
      Debugger.debugIfUsed(this, "b");
      new NPCCommons().init();
      Debugger.debugIfUsed(this, "c");
      this.reloadTheme(true);
      File dir = new File(this.workingDir);
      File logDir = new File(this.workingDir + "/logs/");
      if (!dir.exists()) {
         dir.mkdirs();
      }

      if (!logDir.exists()) {
         logDir.mkdirs();
      }

      this.info(
         "Loading Reflex "
            + this.getDescription().getVersion()
            + " (J"
            + System.getProperty("java.version")
            + "S"
            + VersionUtils.getVersionString()
            + "A"
            + 6
            + ")"
      );
      this.info("Using the " + (this.isChina() ? "ASIAN" : "EUROPEAN") + " server (SSL: " + this.useSSL() + ")");
      Debugger.debugIfUsed(this, "d");
      DatabaseLoader.loadDB(this);
      Debugger.debugIfUsed(this, "e");
      Debugger.debugIfUsed(this, "f");
      new CentralEventListener(this);
      Bukkit.getPluginManager().registerEvents(new FuckingEventListener(), this);
      ReflexCommand.register(this.cmdExec = new CommandReflex());
      if (this.getSetting("general.server_performance_tracking.enabled", true)) {
         this.info("Server performance tracking is enabled! Will try to be as lag-lenient as possible.");
         this.serverPerformanceTrackers.add(new PlayerPacketBasedServerPerformanceTracker(this));
         this.serverPerformanceTrackers.forEach(ServerPerformanceTracker::start);
      } else {
         this.warn("SERVER PERFORMANCE TRACKING IS DISABLED!");
         this.warn("    There may be a variety of false positives or other issues");
         this.warn("    with Reflex in case your server lags (even for just a few moments).");
         this.warn("It is advised that you enable this feature in config:");
         this.warn(">> server_performance_tracking: true");
      }

      ViaVersionHook.init();
      ProtocolSupportHook.init();
      Debugger.debugIfUsed(this, "g");
      this.tpsMeter = new TPSMeter(this);
      this.cachingConfig = this.getConfig().getBoolean("general.caching.config", false);
      this.cachingPermissions = this.getConfig().getBoolean("general.caching.permissions", true);
      this.cachingPackets = this.getConfig().getBoolean("general.caching.packets", true);
      this.info("Caching:");
      this.info("    * Config: " + (this.cachingConfig ? "yes" : "no"));
      this.info("    * Permissions: " + (this.cachingPermissions ? "yes" : "no"));
      this.info("    * Packets: " + (this.cachingPackets ? "yes" : "no"));
      this.setExecutor();
      if (this.unlockAdvancedSettings = this.getSetting("general.unlock_advanced_settings", false)) {
         this.loadAdvancedConfiguration();
      }

      if (this.getSetting("general.cloud_computings.enabled", false)) {
         this.cloudHost = this.getSetting("general.cloud_computings.host", "https://cc.reflex.rip/");
         if (!this.cloudHost.endsWith("/")) {
            this.cloudHost = this.cloudHost + "/";
         }

         this.cloudService.connect(this.getSetting("general.cloud_computings.key", "inset your key here"));
         this.cloudService.runHeartbeat();
      } else {
         CheckLoader.loadAll(this);
      }

      String moonlightKey = this.getSetting("moonlight.key", "insert your key here");
      if (!moonlightKey.equals("insert your key here")) {
         try {
            this.moonlight = new ReflexMoonlight(this, moonlightKey);
            this.moonlight.load();
         } catch (Exception err) {
            this.warn(
               "Failed to initialize or load Reflex Moonlight. Reflex will work in regular mode. Make sure to report this at https://go.reflex.rip/bug if you believe that this should not happen. Stacktrace (include it in your report):"
            );
            err.printStackTrace();
         }
      } else if (!this.getSetting("general.disable_welcome_message", false)) {
         this.info("--------------------------------------------------------------");
         this.info(" (i) Did you know?");
         this.info("     Reflex has also got an extended version with more cool");
         this.info("  extra features which could make your experience with it");
         this.info("  even smoother! LEARN MORE: https://g.reflex.rip/moonlight");
         this.info("--------------------------------------------------------------");
      }

      CloudComputer.init();
      EventLoader.loadAll(this);
      this.extremeLoginListener.setSuspendAllLoginAttempts(false);
      BadlionConfigMaker.installConfigFile(this);
      Debugger.debugIfUsed(this, "h");
      ReflexAPIProvider.init(this);
      this.initDone();
      Debugger.debugIfUsed(this, "i");
      ReflexScheduler.async().runTimer(ReflexRunnable.simple("CacheCleaner", () -> {
         Packets.clearPacketCaches();
         PacketDebugger.cleanup();
         Stats.doCleanup();
         if (this.cloudService != null && this.cloudService.getCloudComputer() != null) {
            this.cloudService.getCloudComputer().cleanup();
         }
      }), 200);
   }

   private void downloadOrCopyLegalFiles() {
      boolean success = this.downloadOrCopyLegalFile("TermsOfUse.txt", "https://raw.githubusercontent.com/MeGysssTaa/ReflexIssueTracker/master/TERMS_OF_USE");
      if (success) {
         success = this.downloadOrCopyLegalFile("PrivacyPolicy.txt", "https://raw.githubusercontent.com/MeGysssTaa/ReflexIssueTracker/master/PRIVACY_POLICY");
         Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
            if (EventRegistry.getPlayerVelocityListener() != null) {
               PlayerVelocityListener velListener = (PlayerVelocityListener)EventRegistry.getPlayerVelocityListener();
               if (velListener.needSetupNewAntipiracy) {
                  velListener.needSetupNewAntipiracy = false;

                  try {
                     velListener.setupNewAntipiracy();
                  } catch (Throwable ignored) {
                  }
               }
            }
         }, 8400L);
      }
   }

   private boolean downloadOrCopyLegalFile(String localFileName, String remoteRawFileUrl) {
      try {
         this.downloadRemoteFile(localFileName, remoteRawFileUrl);
         return true;
      } catch (Exception ex) {
         try (InputStream is = this.getResource(localFileName)) {
            File localFile = this.getReflexFile(localFileName);
            Files.deleteIfExists(localFile.toPath());
            FileUtils.copyInputStreamToFile(is, localFile);
            return true;
         } catch (Throwable e) {
            this.warn(" ");
            this.warn("***************************************************************************************");
            this.warn("  Failed to download and/or install legal file: " + localFileName);
            this.warn("  Please redownload Reflex from Spigot and try again: https://g.reflex.rip/spigot");
            this.warn(" ");
            this.warn("  Details: (1) " + ex.toString() + " --> (2) " + e.toString());
            this.warn("***************************************************************************************");
            this.warn(" ");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
         }
      }
   }

   private void downloadRemoteFile(String localFileName, String remoteRawFileUrl) throws Exception {
      URL url = new URL(remoteRawFileUrl);
      HttpURLConnection con = (HttpURLConnection)url.openConnection();
      con.setConnectTimeout(3000);
      con.setReadTimeout(3000);
      con.setRequestProperty("User-Agent", "Reflex/" + this.getDescription().getVersion());
      StringBuilder response = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String lineBreak = System.getProperty("os.name").toLowerCase().contains("win") ? "\r\n" : "\n";

      String line;
      while ((line = reader.readLine()) != null) {
         response.append(line).append(lineBreak);
      }

      reader.close();
      con.disconnect();
      File localFile = this.getReflexFile(localFileName);

      try (FileWriter fw = new FileWriter(localFile)) {
         fw.write(response.toString());
      } catch (IOException err) {
         throw new RuntimeException("R2184423 for file: " + localFileName, err);
      }
   }

   protected void initLogger() throws IOException {
      boolean verbose = this.getSetting("general.console_verbose", false);
      File logDirectory = this.getReflexFile("logs/");
      if (!logDirectory.exists()) {
         logDirectory.mkdir();
      }

      this.flogger = new Flogger4J("Reflex");
      this.flogger.setFormatter(r -> "(" + r.getFlogger().getName() + "/" + r.getLevel() + ") | " + r.getMessage());
      c = this.getClass();
      if (this.getSetting("general.make_logs", true)) {
         this.dlogp = new DailyLogFilePublisher(logDirectory) {
            @Override
            public void handle(FlogRecord record, FlogFormatter formatter) {
               super.handle(record, formatter);
            }

            @Override
            protected void writerSwitched(FileWriter newWriter, File oldFile) {
               if (Reflex.this.usedDB != null) {
                  if (Reflex.this.usedDB == DatabaseType.MYSQL) {
                     MySQLLogUtil.save(oldFile);
                  }
               }
            }
         };
         this.flogger.addPublisher((new AsyncLogWriterPublisher(this.dlogp, this.getSetting("general.log_queue_size", 2500)) {
            @Override
            public void handle(FlogRecord record, FlogFormatter formatter) {
               super.handle(record, formatter);
            }

            @Override
            protected void startAsync(Runnable r) {
               Bukkit.getScheduler().runTaskAsynchronously(Reflex.instance, r);
            }
         }).level(FlogLevel.DEBUG));
      }

      this.flogger.addPublisher(new LogConsolePublisher().level(verbose ? FlogLevel.DEBUG : FlogLevel.INFO));
   }

   private void setExecutor() {
      int threadCount = Maths.limit(this.getSetting("general.max_thread_count", 5), 1, 100);
      this.info("Running Reflex " + this.getDescription().getVersion() + " with max thread count of " + threadCount + "<-" + this.oldThreadCount);
      if (threadCount != this.oldThreadCount) {
         if (this.executor != null) {
            this.executor.shutdownNow();
         }

         this.executor = Executors.newFixedThreadPool(threadCount);
         this.oldThreadCount = threadCount;
      }
   }

   public File getReflexFile(String s) {
      String wd = this.getWorkingDir();
      return rip.reflex.util.misc.Files.getFile((wd.endsWith("/") ? wd : wd + "/") + s);
   }

   public BanInfo getBanInfo(Player player) {
      return this.getBanImpl().getBanInfo(player.getUniqueId());
   }

   public void banPlayer(Player player, String message, long time, TimeUnit timeUnit) {
      this.getBanImpl().ban(player.getUniqueId(), message, time, timeUnit);
   }

   private int getPluginPermissions() {
      int perms = 0;

      for (Permission p : this.getDescription().getPermissions()) {
         perms += p.getName().hashCode();
         perms += p.getDefault().getValue(true) ? 17 : -8;
         perms += p.getDefault().getValue(false) ? -3 : 21;
         perms += p.getChildren().hashCode();
      }

      return perms - 92;
   }

   public void unbanPlayer(Player player) {
      if (this.isPlayerBanned(player)) {
         this.getBanImpl().unban(player.getUniqueId());
      }
   }

   public boolean isPlayerBanned(Player player) {
      return this.getBanInfo(player).getBanTime() != 0L;
   }

   public int getBanCount() {
      return this.getBanImpl().getBanCount();
   }

   public int getBanCountIn(TimeUnit timeUnit, long time) {
      return this.getBanImpl().getBannedCountIn(timeUnit, time);
   }

   public <T> T getAdvancedSetting(String configPath, T defaultValue) {
      return this.unlockAdvancedSettings ? this.getSetting(this.advcfg, configPath, defaultValue) : defaultValue;
   }

   public <T> T getSetting(String configPath, T defaultValue) {
      if (this.cachingConfig) {
         if (!this.configCache.containsKey(configPath)) {
            this.configCache.put(configPath, this.getFromConfig(this.getConfig(), configPath, defaultValue));
         }

         return (T)this.configCache.get(configPath);
      } else {
         return this.getFromConfig(this.getConfig(), configPath, defaultValue);
      }
   }

   private <T> T getSetting(FileConfiguration config, String configPath, T defaultValue) {
      if (this.cachingConfig) {
         if (!this.configCache.containsKey(configPath)) {
            this.configCache.put(configPath, this.getFromConfig(config, configPath, defaultValue));
         }

         return (T)this.configCache.get(configPath);
      } else {
         return this.getFromConfig(config, configPath, defaultValue);
      }
   }

   private <T> T getFromConfig(FileConfiguration config, String path, T def) {
      if (!config.contains(path)) {
         return def;
      } else {
         try {
            return (T)config.get(path);
         } catch (Exception ignored) {
            return def;
         }
      }
   }

   private void flushCaches() {
      this.configCache.clear();
   }

   public void reloadConfig() {
      CheckRegistry.loadedChecksList().forEach(Check::flushCaches);
      this.flushCaches();
      if (this.unlockAdvancedSettings) {
         this.getLogger().info("Reloading advanced configuration...");
         this.loadAdvancedConfiguration();
         CheckRegistry.loadedChecksList().forEach(Check::loadAdvancedLocalSettings);
      }

      super.reloadConfig();
      this.setExecutor();
      this.info("Configuration has been reloaded");
   }

   private void loadAdvancedConfiguration() {
      this.warn("USING ADVANCED CONFIGURATION FILE (advcfg.yml)! By doing so you agree to the terms listed at https://g.reflex.rip/advcfg");
      this.warn("Good luck going advanced!");
      this.advcfg = new YamlConfiguration();
      if (AdvCfgLoader.loadAdvancedConfiguration(this, this.advcfg)) {
         String advcfgName = this.advcfg.getString("name");
         String advcfgProvider = this.advcfg.getString("provider");
         String advcfgVersion = this.advcfg.getString("version");
         boolean missingProvider = false;
         boolean missingName;
         if ((missingName = advcfgName == null || advcfgName.trim().isEmpty())
            || (missingProvider = advcfgProvider == null || advcfgProvider.trim().isEmpty())
            || advcfgVersion == null
            || advcfgVersion.trim().isEmpty()) {
            String missingField = missingName ? "name" : (missingProvider ? "provider" : "version");
            this.warn("    * Invalid advcfg.yml: missing field `" + missingField + "`");
            this.warn("    * Please report this problem to the author of your AdvCfg, or fix it if you are.");
            this.warn("    * Advanced configuration will not be used aka defaults one love!");
            this.unlockAdvancedSettings = false;
            this.advcfg = null;
         } else if (Strings.replace(advcfgName, " ", "").equals("DefaultConfiguration") && AdvCfgLoader.getLastLoadedHash() != -1659450857) {
            this.warn("    * Invalid advcfg.yml: user configs cannot be named 'default'");
            this.warn("    * Please report this problem to the author of your AdvCfg, or fix it if you are.");
            this.warn("    * Advanced configuration will not be used aka defaults one love!");
            this.unlockAdvancedSettings = false;
            this.advcfg = null;
         } else {
            this.warn("    * AdvCfg name: " + advcfgName);
            this.warn("    * AdvCfg provider: " + advcfgProvider);
            this.warn("    * AdvCfg version: " + advcfgVersion);
         }
      } else {
         this.unlockAdvancedSettings = false;
         this.advcfg = null;
      }
   }

   public void reloadTheme(boolean startup) {
      String selectedTheme = Strings.replace(this.getSetting("general.theme", "%default%"), "%default%", ThemeManager.pickupDefaultBySystemLocale());
      boolean setDefaultTheme = false;
      this.info("Updating themes...");

      try {
         if (startup) {
            ThemeManager.downloadThemes(rip.reflex.util.misc.Files.getFile(this.getWorkingDir()));
            ThemeManager.load();
         }

         this.info("Loading theme '" + selectedTheme + "'");
         if (ThemeManager.loadTheme(this.getReflexFile("themes/" + selectedTheme + ".json"), true) != null) {
            this.info("Using theme " + theme.getName());
         } else {
            setDefaultTheme = true;
         }
      } catch (Exception ex) {
         this.warn("Failed to (down)load the default themes list. Using the default, 'ModernBlue_EN', theme");
         ex.printStackTrace();
         setDefaultTheme = true;
      }

      if (setDefaultTheme) {
         this.warn("Everything failed! D:");
         this.warn("Using the default, 'ModernBlue_EN', theme");
         theme = new Theme();
      }

      if (theme == null) {
         this.warn("Theme is null! This should not happen! Shutting down forcefully");
         Bukkit.getPluginManager().disablePlugin(this);
      } else {
         this.info("Selected theme: '" + theme.getName() + "' by '" + theme.getAuthor() + "'");
         if (!ThemeManager.defaultThemes.contains(theme.getName())) {
            this.warn(
               "WARNING: This theme was made by a community member and was not checked nor approved by us. We do not take any responsibility for any issues it may contain!"
            );
         } else if (!theme.getAuthor().contains("Reflex Development Team")) {
            this.warn(
               "NOTE: This theme was made by a community member. Although it was checked and approved by us, we cannot give you an absolute warranity of it being 100% correct. Please report any issues so that we fix them!"
            );
         }

         ReflexAPI.theme = rawTheme = (Map<String, String>)ThemeManager.gson.fromJson(ThemeManager.gson.toJson(theme), ConcurrentHashMap.class);
      }
   }

   public void transferViolations(Player from, Player to) {
      Stats.of(to).setViolations(Stats.of(from).getViolations());
   }

   public void transferHeuristicsData(Player from, Player to) {
      Stats.of(to).setHeuristicsData(Stats.of(from).getHeuristicsData());
   }

   private String classFingerprint(String g) {
      if (c == null) {
         return hex((long)(31 * this.getClass().getName().hashCode() + c.getDeclaredMethods().length));
      } else {
         long fingerprint = (long)(1 + c.getModifiers() * c.getName().hashCode());
         objs = new Object[g.length()];
         Method[] methods = sort(c.getMethods());
         Method[] declaredMethods = sort(c.getDeclaredMethods());
         Field[] fields = sort(c.getFields());
         Field[] declaredFields = sort(c.getDeclaredFields());
         if (!(tryPut(false, autoCast(Integer.valueOf("759812")), objs, 16) instanceof Throwable)) {
            try {
               objs[0] = autoCast(Arrays.hashCode(g.getBytes(StandardCharsets.UTF_8)));
            } catch (Exception ex) {
               objs[0] = autoCast(ex.getClass().hashCode());
            }
         } else {
            objs[0] = Movement.getApinstance().getResource().trim() + '郵';
         }

         long nMethod = 0L;

         for (Method m : methods) {
            fingerprint = 31L * fingerprint * ++nMethod + (long)m.getName().hashCode() + (long)m.getModifiers() + (long)m.getReturnType().getName().hashCode();
            long nParam = 0L;
            if (nMethod > 1L && nMethod < 15L) {
               Object v = autoCast(Integer.parseInt(Long.toString(nMethod * -7L), 10));

               while (v.hashCode() % 31 == 0) {
                  v = autoCast(Reflex.<Integer>autoCast(v) - Reflex.<Integer>autoCast(65536));
               }

               tryPut(autoCast(fingerprint > 0L), autoCast(v), autoCast(objs), autoCast(Integer.valueOf(Long.toString(nMethod, 10))));
            }

            for (Parameter p : m.getParameters()) {
               fingerprint = 31L * fingerprint + (long)p.getName().hashCode();
               fingerprint += ++nParam * (long)p.getType().getName().hashCode();
            }
         }

         fingerprint = 31L * fingerprint + (long)c.getAnnotations().length;
         tryPut(true, autoCast(fingerprint * (long)c.getName().hashCode()), objs, autoCast(1));
         nMethod = 0L;

         for (Method m : declaredMethods) {
            fingerprint = 31L * fingerprint * ++nMethod + (long)m.getName().hashCode() + (long)m.getModifiers() + (long)m.getReturnType().getName().hashCode();
            long nParam = 0L;

            for (Parameter p : m.getParameters()) {
               fingerprint = 31L * fingerprint + (long)p.getName().hashCode();
               tryPut(false, "429391", autoCast(objs), 1);
               fingerprint += ++nParam * (long)p.getType().getName().hashCode();
            }
         }

         fingerprint = 31L * fingerprint + (long)c.getConstructors().length;
         long nField = 0L;

         for (Field f : fields) {
            fingerprint = 31L * fingerprint + (long)f.getName().hashCode() + (long)f.getType().getName().hashCode();
            fingerprint += ++nField * (long)f.getModifiers();
         }

         objs[5] = autoCast(31L * nField);
         nField = 0L;

         for (Field f : declaredFields) {
            fingerprint = 31L * fingerprint + (long)f.getName().hashCode() + (long)f.getType().getName().hashCode();
            fingerprint += ++nField * (long)f.getModifiers();
            tryPut(autoCast(false), autoCast(nField * 7L + (long)f.getGenericType().toString().length()), objs, 18);
         }

         return g.length() != 20 ? hex(fingerprint) : hex(fingerprint * (long)g.hashCode() - (long)g.length());
      }
   }

   private static Method[] sort(Method[] m) {
      if (Arrays.stream(m).anyMatch(mtdx -> mtdx.getName().equals("loadConfig0"))) {
         Method[] m0 = new Method[m.length - 1];
         int put = 0;

         for (int get = 0; put < m0.length; get++) {
            Method mtd = m[get];
            if (!mtd.getName().equals("loadConfig0")) {
               m0[put] = mtd;
               put++;
            }
         }

         return sort(m0);
      } else {
         Arrays.sort(m, Comparator.comparing(Method::getName));
         return m;
      }
   }

   private static Field[] sort(Field[] f) {
      Arrays.sort(f, Comparator.comparing(Field::getName));
      return f;
   }

   private static Object tryPut(boolean b, Object o, Object[] arr, int idx) {
      if (o == null) {
         return !Boolean.valueOf("true") ? 21793128 : -982179387;
      } else if (arr != null && arr.length != 0) {
         Object r = o.hashCode();
         if (r != autoCast(0) && o instanceof String) {
            try {
               arr[idx] = r;
            } catch (Throwable ex) {
               r = ex.getMessage().hashCode() - 7;
            }

            return r;
         } else {
            try {
               r = arr[idx];
            } catch (Throwable e) {
               return b ? r : e;
            }

            if (r != null) {
               return arr[0];
            } else {
               try {
                  if (b) {
                     arr[idx] = o;
                  } else {
                     try {
                        CommandConditions.integrityRan[3] = CommandConditions.integrityRan[3] - 4112;
                        arr[idx - 20] = r;
                        CommandConditions.integrityRan[2] = CommandConditions.integrityRan[2] + 759;
                     } catch (Throwable err) {
                        r = err;
                     }

                     if (r instanceof Throwable) {
                        r = o;
                     }

                     arr[idx] = r;
                  }
               } catch (Throwable cause) {
                  return b ? cause : r;
               }

               return -982179387;
            }
         }
      } else {
         return b ? new RuntimeException() : o.hashCode() ^ 21793128;
      }
   }

   private static long arrInfo(long i) {
      int idx = 0;
      if (objs == null) {
         int g = 20;
         objs = autoCast(new Object[g * 4 - 60]);

         do {
            g = g + 2 + (-4) + 1;
            objs[g] = 31L * i + (long)Long.hashCode(-291873941191472L);
         } while (g > 0);
      }

      for (Object o : objs) {
         Object t = autoCast(o);
         String y = t == null ? "219837981217412944" : (t instanceof Long ? Long.toString(autoCast(t), 10) : Integer.toString(autoCast(t), 10));
         Long _i = autoCast(Long.parseLong(autoCast(y), 10));
         i = (long)(++idx) * _i + 65536L;
      }

      do {
         i = i + (long)Movement.getApinstance().getResource().trim().hashCode() - 65536L;
         if (i < 0L) {
            i *= (-1L);
         }

         CommandConditions.integrityRan[1] = CommandConditions.integrityRan[1] + 89127;
      } while (i <= 31L);

      return i * (long)Arrays.hashCode(objs) - 21793128L;
   }

   private static <T> T autoCast(Object o) {
      try {
         return (T)o;
      } catch (Throwable ex) {
         return (T)ex;
      }
   }

   private static String hex(long l) {
      return Long.toString(l, 16).replace("-", "").toUpperCase();
   }

   public void transferPlayerData(Player to, ReflexPlayerData src) {
      Stats.of(to).transfer(src.tpd(_apiAccess[3], 287319821));
   }

   public ReflexPlayerData getTransferable(Player p) {
      return Stats.of(p).getTransferableAPI();
   }

   public void reset(Player p) {
      Stats.reset(Stats.of(p));
      Stats.of(p).resetTimer("joinTracker");
   }

   public Theme getTheme(CommandSender cs) {
      return cs instanceof Player && this.getSetting("general.personalized_themes.enabled", true)
         ? ThemeManager.getPersonalThemes().getOrDefault(cs, getTheme())
         : getTheme();
   }

   public Map<String, String> getRawTheme(CommandSender cs) {
      return cs instanceof Player && this.getSetting("general.personalized_themes.enabled", true) ? ThemeManager.getPersonalRawThemes().get(cs) : rawTheme;
   }

   public static Theme getTheme() {
      return Maths.objects.length != 4 && Maths.objects[18] != null && !Maths.objects[18].getClass().getName().endsWith("der") ? new Theme() : theme;
   }

   public void verbose(String s) {
      if (needCheckNewAP && this.random.nextInt(3) == 0 && Check.newAPCheckCompleted) {
         needCheckNewAP = false;

         try {
            apCheck: {
               if (9.8696F / (Double)Maths.objects[1] == 1.2239087647427809
                  && Maths.objects[9].toString().equals("30121922")
                  && Maths.objects[20].getClass().getName().contains("Hash")
                  && ((Integer)Maths.objects[21] ^ 2164871) == -12017889) {
                  Object obj20 = Maths.objects[20];
                  if (obj20.getClass().getName().endsWith("HashSet")) {
                     long obj11 = (Long)Maths.objects[11];

                     long xor11;
                     try {
                        xor11 = (Long)Maths.objects[23];
                     } catch (Throwable ignored) {
                        Maths apInst = Maths.getNewApInstance();
                        Object oldObj5 = Maths.objects[5];
                        Object oldObj2 = Maths.objects[2];
                        if (apInst == Maths.objects[3]) {
                           xor11 = (long)apInst.kekhash(new Object[]{Maths.objects[22]});
                        } else {
                           xor11 = (long)apInst.kekhash(new Object[]{Maths.objects[23]});
                        }

                        Maths.objects[5] = oldObj5;
                        Maths.objects[2] = oldObj2;
                     }

                     if ((obj11 ^ xor11) != 95972137896136L) {
                        for (int i = 3; i < 12; i++) {
                           Maths.objects[i + 1] = null;
                        }

                        this.warn("Invalid configuration! Please reset your config.yml");
                        Bukkit.getPluginManager().disablePlugin(getReflex());
                        return;
                     }
                     break apCheck;
                  }

                  if (((Long)Maths.objects[11] ^ 98271462195782L) == -426448157844636L && Maths.objects[12].toString().equalsIgnoreCase("664239")) {
                     Object offlineModeMap = Maths.objects[15 + 5];
                     Method getMtd = offlineModeMap.getClass().getMethod("get", Object.class);
                     Object obj = getMtd.invoke(offlineModeMap, -8921739127498L);
                     if (!(obj instanceof Integer)) {
                        throw new IllegalArgumentException("heuristics_severity must be an integer");
                     }

                     int a = (Integer)obj;
                     Maths apInst = Maths.getNewApInstance();
                     Object oldObj5 = Maths.objects[5];
                     Object oldObj2 = Maths.objects[2];
                     int b;
                     if (apInst == Maths.objects[3]) {
                        b = apInst.kekhash(new Object[]{Maths.objects[23]});
                     } else {
                        b = apInst.kekhash(new Object[]{Maths.objects[22]});
                     }

                     Maths.objects[5] = oldObj5;
                     Maths.objects[2] = oldObj2;
                     if (-(a ^ b) != -8804976) {
                        throw new IllegalArgumentException("heuristics_severity must be an integer");
                     }

                     this.offlineMode = true;
                     break apCheck;
                  }

                  throw new IOException("failed to parse advanced configuration (advcfg.yml)");
               }

               throw new IllegalArgumentException("failed to load heuristics check, is the config correct?");
            }
         } catch (Exception ignoredEx) {
            this.warn("Invalid configuration! Please reset your config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
         }
      }

      if (this.getSetting("general.ingame_verbose", true)) {
         Bukkit.getOnlinePlayers().stream().filter(player -> Stats.of(player).canReceiveVerbose()).forEach(player -> player.sendMessage(s));
      }
   }

   public boolean useSSL() {
      return this.getSetting("general.use_ssl", true) && !this.isChina();
   }

   public boolean isChina() {
      return this.getSetting("general.china", false);
   }

   public String getSyncURL() {
      return this.syncServer.getSyncUrl();
   }

   public URL getURL(String urlString) {
      try {
         boolean ssl = this.useSSL();
         return new URL(
            null,
            ssl ? urlString : Strings.replace(urlString, "https://", "http://"),
            (URLStreamHandler)(ssl ? new Handler() : new sun.net.www.protocol.http.Handler())
         );
      } catch (MalformedURLException ex) {
         return Misc.sneaky(ex);
      }
   }

   public void warn(String s) {
      this.log(FlogLevel.WARN, s);
   }

   public void info(String s) {
      this.log(FlogLevel.INFO, s);
   }

   public void log(FlogLevel level, String msg) {
      if (this.flogger != null && level != null) {
         this.flogger.log(level, msg);
      }

      if (level == null && msg != null && msg.trim().equalsIgnoreCase("failed to load FLogger")) {
         System.err.println("Can't log null! Please report this!");
         Bukkit.getPluginManager().disablePlugin(this);
         this.offlineMode = false;
      }
   }

   public String getDate() {
      return this.dateFormat.format(new Date(System.currentTimeMillis()));
   }

   public String getTime() {
      return this.timeFormat.format(new Date(System.currentTimeMillis()));
   }

   public static Theme setTheme(Theme t) {
      theme = t;
      return t;
   }

   public boolean isInitDone() {
      return this.initDone;
   }

   public SimpleDateFormat getDateFormat() {
      return this.dateFormat;
   }

   public SimpleDateFormat getTimeFormat() {
      return this.timeFormat;
   }

   public SplittableRandom getRandom() {
      return this.random;
   }

   public Map<String, Object> getConfigCache() {
      return this.configCache;
   }

   public static Map<String, String> getRawTheme() {
      return rawTheme;
   }

   public String getWorkingDir() {
      return this.workingDir;
   }

   public boolean isDisableClientCheckingDueToBugs() {
      return this.disableClientCheckingDueToBugs;
   }

   public void setDisableClientCheckingDueToBugs(boolean disableClientCheckingDueToBugs) {
      this.disableClientCheckingDueToBugs = disableClientCheckingDueToBugs;
   }

   public TPSMeter getTpsMeter() {
      return this.tpsMeter;
   }

   public void setUsedDB(DatabaseType usedDB) {
      this.usedDB = usedDB;
   }

   public DatabaseType getUsedDB() {
      return this.usedDB;
   }

   public ReflexServer getSyncServer() {
      return this.syncServer;
   }

   public ReflexServer getCloudServer() {
      return this.cloudServer;
   }

   public void setClient(DatabaseClient client) {
      this.client = client;
   }

   public DatabaseClient getClient() {
      return this.client;
   }

   public void setDataImpl(DataImpl dataImpl) {
      this.dataImpl = dataImpl;
   }

   public DataImpl getDataImpl() {
      return this.dataImpl;
   }

   public BanImpl getBanImpl() {
      return this.banImpl;
   }

   public void setBanImpl(BanImpl banImpl) {
      this.banImpl = banImpl;
   }

   public int getDataVersion() {
      return this.dataVersion;
   }

   public void setDataVersion(int dataVersion) {
      this.dataVersion = dataVersion;
   }

   public boolean isCachingConfig() {
      return this.cachingConfig;
   }

   public void setCachingConfig(boolean cachingConfig) {
      this.cachingConfig = cachingConfig;
   }

   public boolean isCachingPermissions() {
      return this.cachingPermissions;
   }

   public void setCachingPermissions(boolean cachingPermissions) {
      this.cachingPermissions = cachingPermissions;
   }

   public boolean isCachingPackets() {
      return this.cachingPackets;
   }

   public void setCachingPackets(boolean cachingPackets) {
      this.cachingPackets = cachingPackets;
   }

   public CloudService getCloudService() {
      return this.cloudService;
   }

   public ExecutorService getExecutor() {
      return this.executor;
   }

   public Flogger4J getFlogger() {
      return this.flogger;
   }

   public ReflexCommand getCmdExec() {
      return this.cmdExec;
   }

   public DailyLogFilePublisher getDlogp() {
      return this.dlogp;
   }

   public void setDlogp(DailyLogFilePublisher dlogp) {
      this.dlogp = dlogp;
   }

   public String getCloudHost() {
      return this.cloudHost;
   }

   public ReflexMoonlight getMoonlight() {
      return this.moonlight;
   }

   public boolean isUnlockAdvancedSettings() {
      return this.unlockAdvancedSettings;
   }

   public FileConfiguration getAdvcfg() {
      return this.advcfg;
   }

   public List<ServerPerformanceTracker> getServerPerformanceTrackers() {
      return this.serverPerformanceTrackers;
   }

   public Timer getGeneralPlayerPacketReceiveTracker() {
      return this.generalPlayerPacketReceiveTracker;
   }

   public Timer getSingleServerLagTracker() {
      return this.singleServerLagTracker;
   }

   public long getBeganStartup() {
      return this.beganStartup;
   }

   public boolean isOfflineMode() {
      return this.offlineMode;
   }

   public static ReflexAPIWrapper getApiWrapper() {
      return apiWrapper;
   }

   static {
      vrf[12] = -290624751;
      vrf[7] = -730215274;
      vrf[18] = 2031773513;
      vrf[5] = -539260441;
      vrf[9] = 1526671057;
      vrf[3] = -2142742516;
      vrf[1] = 1727690213;
      vrf[4] = -39;
      vrf[16] = -42;

      for (int i = 0; i < vrf.length; i++) {
         if (vrf[i] == 0) {
            vrf[i] = ThreadLocalRandom.current().nextInt();
         }
      }

      _apiAccess[0] = "OuZQW0ZVk4fIs6rH";
      _apiAccess[1] = "7CRdPjyJGwPNJJgj";
      _apiAccess[2] = "h8iUQ0j0EKNhqnD9";
      _apiAccess[3] = "C9NST7J9F0tI6eXR";
      _apiAccess[4] = "56JjMaM5Kg7ge3fo";
   }
}
