package rip.reflex.check.base;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.flo.flogger.types.FlogLevel;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import rip.reflex.Reflex;
import rip.reflex.api.Cheat;
import rip.reflex.api.CheckResult;
import rip.reflex.api.event.ReflexCheckEvent;
import rip.reflex.api.event.ReflexCommandEvent;
import rip.reflex.util.appearance.Theme;
import rip.reflex.util.data.Stats;
import rip.reflex.util.loop.LoopHelper;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.performance.LagStatus;
import rip.reflex.util.performance.MethodProfiler;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public abstract class Check implements Listener {
   public static volatile boolean newAPCheckCompleted = Boolean.parseBoolean("FALSE".toLowerCase());
   private static final String ID_CHARSET = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
   private static final Set<String> disabledComps = new HashSet<>();
   protected final Reflex reflex;
   protected final CheckType type;
   private static final Set<String> invalidWorlds = new HashSet<>();
   protected final Cheat cheat;
   private final Map<String, Object> configCache = new ConcurrentHashMap<>();
   private final String name;
   private final String comp;
   private boolean enabled;
   private boolean sync = false;
   private boolean lagSensetive = true;
   private final Set<World> disabledWorlds = new HashSet<>();

   public Check(String name, String comp, CheckType type, Cheat cheat) {
      this.name = name;
      this.comp = comp;
      this.type = type;
      this.cheat = cheat;
      this.reflex = Reflex.getReflex();
      this.enabled = this.isEnabledInConfig();
      if (this.isEnabledInConfig()) {
         List<String> disabledComps = this.reflex.getConfig().getStringList(name.toLowerCase() + ".disabled_checks");
         if (disabledComps != null) {
            disabledComps.remove("example entry");
            Check.disabledComps.addAll(disabledComps);
         }

         List<String> disabledWorlds = this.reflex.getConfig().getStringList(name.toLowerCase() + ".disabled_worlds");
         if (disabledWorlds != null) {
            disabledWorlds.forEach(world -> {
               World w = Bukkit.getWorld(world);
               if (w == null) {
                  reportInvalidWorld(world);
               } else {
                  this.disabledWorlds.add(w);
               }
            });
         }

         this.loadAdvancedLocalSettings();
         if (disabledComps != null && disabledComps.contains(comp)) {
            reportDisabledComponent(name, comp);
         } else {
            Bukkit.getPluginManager().registerEvents(this, this.reflex);
         }
      }
   }

   public boolean isServerLagging() {
      return this.isServerLagging(3000L);
   }

   public boolean isServerLagging(long maxMsSinceLastLag) {
      return !this.reflex.getSingleServerLagTracker().hasPassed(maxMsSinceLastLag);
   }

   protected <T> T getAdvancedLocalSetting(String name, T defaultValue) {
      return this.reflex.getAdvancedSetting(this.comp + "." + name, defaultValue);
   }

   protected void supportCloudComputings() {
      this.reflex.getCloudService().getCloudSupportedChecks().add(this);
   }

   public void loadAdvancedLocalSettings() {
   }

   public boolean isDisabledInWorld(World w) {
      return this.disabledWorlds.contains(w);
   }

   private static void reportInvalidWorld(String world) {
      if (!invalidWorlds.contains(world)) {
         Reflex.getReflex().warn("World '" + world + "' does not exist or is unloaded, and hence cannot be added to disabled worlds");
         invalidWorlds.add(world);
      }
   }

   private static void reportDisabledComponent(String check, String comp) {
      Reflex.getReflex()
         .warn(
            "A "
               + (check == null ? " " : check)
               + " component "
               + comp
               + " is disabled. It is highly recommended that you report bugs at go.reflex.rip/bug rather than turn the checks off! Support regarding bypasses will not be providen in case there are disabled components."
         );
   }

   public static String correctName(String s) {
      for (Check check : CheckRegistry.loadedChecksList()) {
         if (check.getName().equalsIgnoreCase(s)) {
            return check.getName();
         }
      }

      return null;
   }

   public static void addVl(Stats s, Cheat c, CheckResult r) {
      if (r.getViolationsMod() != 0) {
         int v = s.getLagStatus().correctVl(r.getViolationsMod());
         s.addViolations(c, v);
         r.setMod(v);
      }
   }

   public static boolean isComponentEnabled(String name) {
      return !isComponentDisabled(name);
   }

   public static boolean isComponentDisabled(String name) {
      return disabledComps.contains(name);
   }

   public static void enableComponent(String name) {
      Reflex.getReflex().info("Enabled component " + name);
      disabledComps.remove(name);
   }

   public static void disableComponent(String name) {
      if (!disabledComps.contains(name)) {
         reportDisabledComponent(null, name);
         disabledComps.add(name);
      }
   }

   public static void check(Player p, Stats stats, Cancellable event, Check check, CheckResult result) {
      if (!result.isZero() && !check.isCompDisabled()) {
         try {
            Reflex reflex = Reflex.getReflex();
            List<String> disabledChecks = reflex.getConfig().getStringList(check.getName().toLowerCase() + ".disabled_checks");
            if (disabledChecks != null && !disabledChecks.isEmpty()) {
               for (String tag : result.getTags()) {
                  for (String disabled : disabledChecks) {
                     if (tag.toLowerCase().contains(disabled.toLowerCase())) {
                        stats.addViolations(check.getCheat(), -result.getViolationsMod());
                        return;
                     }
                  }
               }
            }

            MethodProfiler mp = new MethodProfiler("FI-78461793");
            String id = "#" + Strings.randomComp(8, "QWERTYUIOPASDFGHJKLZXCVBNM1234567890");
            ReflexCheckEvent checkEvent = new ReflexCheckEvent(p, check.getCheat(), result, id);
            Bukkit.getPluginManager().callEvent(checkEvent);
            if (result.isCheckPassed()) {
               return;
            }

            if (!p.isOnline()) {
               Stats.reset(stats);
               return;
            }

            if (stats.getStat("canBypass" + check.getName(), boolean.class) || checkEvent.isCancelled()) {
               stats.addViolations(check.getCheat(), -result.getViolationsMod());
               return;
            }

            if (result.shouldCancel() && event != null) {
               event.setCancelled(true);
            }

            check0(reflex, p, stats, check, result, id);
            mp.checkExecutionTime();
            mp.addToTimings();
         } catch (NullPointerException ignored) {
         }
      }
   }

   private static void check0(Reflex reflex, Player p, Stats stats, Check check, CheckResult result, String id) {
      MethodProfiler mp = new MethodProfiler("NH-39501295");
      if (reflex.getSetting("general.verbose", true)) {
         MethodProfiler mpTags = new MethodProfiler("UG-57619233");
         Theme t = Reflex.getTheme();
         boolean includeTags = t.getVerboseFormat().contains("%tags%");
         StringBuilder message = new StringBuilder(
            t.getPrefix()
               + Strings.wrap(t.getVerboseFormat())
                  .replace("%player%", p::getName)
                  .replace("%check%", check::getName)
                  .replace("%vl%", () -> stats.getViolations(check.getCheat()))
                  .replace("%mod%", result::getViolationsMod)
                  .replace("%tags%", "")
                  .placeholderApi(p)
                  .get()
         );
         if (includeTags) {
            for (String tag : result.getTags()) {
               message.append(result.getTags().indexOf(tag) == 0 ? " " : t.getVerboseTagsSeparator()).append(t.getVerboseTagsCol()).append(tag);
            }

            if (result.shouldCancel()) {
               message.append(t.getVerboseTagsSeparator()).append(t.getVerboseTagsCol()).append("cancel");
            }

            message.append(t.getVerboseTagsSeparator()).append(t.getVerboseTagsCol()).append("ping: ").append(stats.getPing());
            message.append(t.getVerboseTagsSeparator())
               .append(t.getVerboseTagsCol())
               .append("lp: ")
               .append(stats.getLagPoints())
               .append(" (")
               .append(stats.getLagStatus().fancyName())
               .append(")");
            message.append(t.getVerboseTagsSeparator())
               .append(t.getVerboseTagsCol())
               .append("tps: ")
               .append(Maths.toRoundedString(reflex.getTpsMeter().getTps()));
            message.append(t.getVerboseTagsSeparator()).append(t.getVerboseTagsCol()).append("comp: ").append(check.comp);
         }

         mpTags.addToTimings();
         MethodProfiler mpLog = new MethodProfiler("DI-10237519");
         reflex.log(FlogLevel.DEBUG, Strings.wrap(message).noColors().replace(t.getPrefix(), "").get());
         reflex.verbose(Strings.wrap(message.toString()).colourize().toString());
         mpLog.addToTimings();
      }

      MethodProfiler mpPrepare = new MethodProfiler("CX-2193901");
      List<String> actions = new ArrayList<>(check.getActions().getKeys(false));
      Collections.reverse(actions);
      int curVl = stats.getViolations(check.getCheat());
      int preVl = stats.getLastVl(check.getCheat());
      if (curVl != 0) {
         List<String> dispatchList = new ArrayList<>();
         actions.stream()
            .filter(vl -> {
               int vli = Integer.parseInt(vl);
               return preVl < vli && curVl >= vli;
            })
            .forEach(
               s -> {
                  MethodProfiler mpCmd = new MethodProfiler("KY-598712691");

                  for (String command : reflex.getConfig().getStringList(check.getName().toLowerCase() + ".actions." + s)) {
                     ReflexCommandEvent cmdEvent = new ReflexCommandEvent(p, check.getCheat(), command, id);
                     Bukkit.getPluginManager().callEvent(cmdEvent);
                     if (!cmdEvent.isCancelled()) {
                        int d = cmdEvent.getCommand().contains("%bannedplayersinlast")
                           ? Integer.valueOf(command.split("bannedplayersinlast")[1].split("days%")[0])
                           : 0;
                        command = Strings.wrap(cmdEvent.getCommand())
                           .replace("%player%", p::getName)
                           .replace("%ping%", stats::getPing)
                           .replace("%violations%", () -> stats.getViolations(check.getCheat()))
                           .replace("%check%", check::getName)
                           .replace("%break%", "\n")
                           .replace("%bannedplayers%", reflex::getBanCount)
                           .replace("%bannedplayersinlast" + d + "days%", () -> reflex.getBanCountIn(TimeUnit.DAYS, (long)d))
                           .replace("%execid%", id)
                           .replace("%id%", id)
                           .replace("%date%", reflex::getDate)
                           .replace("%tps%", () -> "" + reflex.getTpsMeter().getTps())
                           .replace("%lp%", () -> stats.getInt("lagPoints"))
                           .replace("%lagstatus%", () -> stats.getStat("lagStatus", LagStatus.class))
                           .replace("%time%", reflex::getTime)
                           .placeholderApi(p)
                           .colourizeAndGet();
                        dispatchList.add(command);
                     }
                  }

                  mpCmd.addToTimings();
               }
            );
         mpPrepare.addToTimings();
         MethodProfiler mpDispatch = new MethodProfiler("OW-4978217489");
         if (Bukkit.isPrimaryThread()) {
            dispatchCommands(reflex, id, dispatchList);
         } else {
            ReflexScheduler.sync().run(ReflexRunnable.simple("dispatch-cmds-thread", () -> dispatchCommands(reflex, id, dispatchList)));
         }

         mpDispatch.addToTimings();
         mp.addToTimings();
      }
   }

   private static void dispatchCommands(Reflex reflex, String id, List<String> dispatchList) {
      MethodProfiler mp = new MethodProfiler("OT-74021485");
      dispatchList.forEach(c -> {
         reflex.log(FlogLevel.DEBUG, "Dispatching command " + Strings.wrap(c).replace("\n", "%break%").replace(" ", "%space%") + " (execution id: " + id + ")");
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c);
      });
      mp.checkExecutionTime();
      mp.addToTimings();
   }

   public boolean isNotSilent() {
      return !this.isSilent();
   }

   public String getName() {
      return this.name;
   }

   public String comp() {
      return Strings.encode(this.comp);
   }

   public CheckType getType() {
      return this.type;
   }

   public <T> T getLocalSetting(String configPath, T defaultValue) {
      return this.getGeneralSetting(this.name.toLowerCase() + "." + configPath, defaultValue);
   }

   public <T> T getGeneralSetting(String configPath, T defaultValue) {
      if (this.reflex.isCachingConfig()) {
         if (!this.configCache.containsKey(configPath)) {
            this.configCache.put(configPath, this.getFromConfig(configPath, defaultValue));
         }

         return (T)this.configCache.get(configPath);
      } else {
         return this.getFromConfig(configPath, defaultValue);
      }
   }

   public void flushCaches() {
      this.configCache.clear();
   }

   private <T> T getFromConfig(String path, T def) {
      if (!this.reflex.getConfig().contains(path)) {
         return def;
      } else {
         try {
            return (T)this.reflex.getConfig().get(path);
         } catch (Exception ignored) {
            return def;
         }
      }
   }

   public ConfigurationSection getActions() {
      return this.reflex.getConfig().getConfigurationSection(this.name.toLowerCase() + ".actions");
   }

   public boolean isCompEnabled() {
      return !this.isCompDisabled();
   }

   public boolean isCompDisabled() {
      return disabledComps.contains(this.comp);
   }

   public void enableComponent() {
      this.reflex.info("Enabled component " + this.comp);
      disabledComps.remove(this.comp);
   }

   public void disableComponent() {
      if (!disabledComps.contains(this.comp)) {
         reportDisabledComponent(null, this.name);
         disabledComps.add(this.comp);
      }
   }

   public boolean isEnabled() {
      return CheckRegistry.getStatus(this);
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      CheckRegistry.updateStatus(this);
   }

   public boolean isDisabled() {
      return !this.isEnabled();
   }

   public void toggle() {
      this.setEnabled(!this.isEnabled());
   }

   public void enable() {
      if (!this.isEnabled()) {
         this.reflex.log(FlogLevel.DEBUG, "Enabled check " + this.name);
         this.setEnabled(true);
      }
   }

   public void disable() {
      if (!this.isDisabled()) {
         this.reflex.log(FlogLevel.DEBUG, "Disabled check " + this.name);
         this.setEnabled(false);
      }
   }

   public boolean isEnabledInConfig() {
      return this.getLocalSetting("enabled", false);
   }

   protected boolean shouldCancel(Stats stats, int threshold) {
      return this.isNotSilent()
         && stats.getViolations(this.cheat)
            > (this.getLocalSetting("dynamic_cancel_thresholds", true) ? threshold : this.getLocalSetting("cancel_threshold", threshold));
   }

   protected boolean shouldCancel(Stats stats) {
      return this.isNotSilent() && stats.getViolations(this.cheat) > this.getLocalSetting("cancel_threshold", 0);
   }

   protected boolean isSilent() {
      return this.getLocalSetting("silent", false);
   }

   protected void setupVlResetThread(int resetDelaySeconds) {
      if (this.isEnabledInConfig()) {
         LoopHelper.loopStatsEvery(stats -> stats.resetViolations(this.cheat), resetDelaySeconds * 20);
      }
   }

   protected void setupVlReductionThread(int reductionDelaySeconds, float reductionFac) {
      if (this.isEnabledInConfig()) {
         LoopHelper.loopStatsEvery(
            stats -> stats.setViolations(this.cheat, Maths.floor((float)stats.getViolations(this.cheat) * reductionFac)), reductionDelaySeconds * 20
         );
      }
   }

   protected void setupVlSubtractionThread(int subtractionDelaySeconds, int subtractionFac) {
      if (this.isEnabledInConfig()) {
         LoopHelper.loopStatsEvery(stats -> stats.addViolations(this.cheat, -subtractionFac), subtractionDelaySeconds * 20);
      }
   }

   protected void runTask(ReflexRunnable task, boolean async) {
      if (this.isEnabledInConfig()) {
         if (async) {
            ReflexScheduler.async().run(task);
         } else {
            ReflexScheduler.sync().run(task);
         }
      }
   }

   protected void delayTask(ReflexRunnable task, int startDelayTicks, boolean async) {
      if (this.isEnabledInConfig()) {
         if (async) {
            ReflexScheduler.async().runLater(task, startDelayTicks);
         } else {
            ReflexScheduler.sync().runLater(task, startDelayTicks);
         }
      }
   }

   protected void runTimer(ReflexRunnable task, int repeatDelayTicks, boolean async) {
      if (this.isEnabledInConfig()) {
         if (async) {
            ReflexScheduler.async().runTimer(task, repeatDelayTicks);
         } else {
            ReflexScheduler.sync().runTimer(task, repeatDelayTicks);
         }
      }
   }

   protected void registerPacketHandler(PacketAdapter adapter) {
      if (this.isEnabledInConfig()) {
         ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
      }
   }

   boolean isEnabled0() {
      return this.enabled;
   }

   public Cheat getCheat() {
      return this.cheat;
   }

   public Map<String, Object> getConfigCache() {
      return this.configCache;
   }

   public boolean isSync() {
      return this.sync;
   }

   public boolean isLagSensetive() {
      return this.lagSensetive;
   }

   protected void setSync(boolean sync) {
      this.sync = sync;
   }

   protected void setLagSensetive(boolean lagSensetive) {
      this.lagSensetive = lagSensetive;
   }

   public Set<World> getDisabledWorlds() {
      return this.disabledWorlds;
   }
}
