package rip.reflex.command.reflex;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.spigotmc.SpigotConfig;
import rip.reflex.Reflex;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.data.Stats;
import rip.reflex.util.misc.AdvCfgLoader;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.misc.Strings;

public class SubCommandDebug extends ReflexSubCommand {
   private boolean inProcess = false;
   private long lastTime = -1L;
   private String link;

   public SubCommandDebug() {
      super("debug", "reflex.command.debug");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.checkBool(this.inProcess, Strings.color(Reflex.getReflex().getTheme(cs).getDebugBusy()));
      cc.checkBool(
         this.lastTime + 30000L > System.currentTimeMillis(),
         Strings.wrap(Reflex.getReflex().getTheme(cs).getDebugSpam()).replace("%link%", this.link).colourizeAndGet()
      );

      try {
         Reflex.class.getDeclaredMethod("loadConfig0");
      } catch (Exception ignored) {
         Stats.trigger = null;
      }
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      if (!Reflex.getReflex().offlineMode) {
         this.inProcess = true;
         StringBuilder b = new StringBuilder();
         PluginManager pm = Bukkit.getPluginManager();
         Reflex reflex = Reflex.getReflex();
         cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getCreatingDebug()));
         b.append(" *** Collected server info *** \n\n");
         this.append(b, "Date: ", reflex.getDateFormat().format(Long.valueOf(System.currentTimeMillis())));
         this.append(b, "Time: ", reflex.getTimeFormat().format(Long.valueOf(System.currentTimeMillis())));
         this.append(b, "\n", " * Version info");
         this.append(b, "Java version: ", this.tryGet(() -> System.getProperty("java.version")));
         this.append(b, "Server version: ", Bukkit.getName(), "/", Bukkit.getVersion());
         this.append(b, "Reflex version: ", Reflex.getReflex().getDescription().getVersion());
         this.append(b, "Protocollib Version: ", pm.getPlugin("ProtocolLib").getDescription().getVersion());
         this.append(b, "\n", " * Global protocol enhancement plugins");
         this.append(b, "ViaVersion: ", this.tryGet(() -> pm.getPlugin("ViaVersion").getDescription().getVersion()));
         this.append(b, "ProtocolSupport: ", this.tryGet(() -> pm.getPlugin("ProtocolSupport").getDescription().getVersion()));
         this.append(b, "\n", " * Other");
         this.append(b, "SecurityManager: ", System.getSecurityManager() == null ? "Cannot be resolved" : System.getSecurityManager().getClass().getName());
         this.append(b, "BungeeCord: ", String.valueOf(SpigotConfig.bungee));
         this.append(b, "Unlock advcfg.yml: " + reflex.isUnlockAdvancedSettings());
         if (reflex.isUnlockAdvancedSettings()) {
            this.append(b, "    - Modified (non-default): " + (AdvCfgLoader.getLastLoadedHash() != -1659450857));
            this.append(b, "    - Name: " + reflex.getAdvcfg().getString("name"));
            this.append(b, "    - Provider: " + reflex.getAdvcfg().getString("provider"));
            this.append(b, "    - Version: " + reflex.getAdvcfg().getString("version"));
         }

         this.append(b, "\n", " * Plugins");
         Arrays.stream(pm.getPlugins()).forEach(p -> this.append(b, "- ", p.getName(), ": ", p.getDescription().getVersion()));
         this.append(b, "\n", " * Config\n");
         this.append(
            b,
            reflex.getConfig()
               .saveToString()
               .replace(Reflex.getReflex().getSetting("general.cloud_computings.key", "AAAAAAAAAA-BBBBBBBBBB-CCCCCCCCCC"), "**CENSORED**")
         );
         if (reflex.isUnlockAdvancedSettings()) {
            this.append(b, "\n", " * Advanced configuration (advcfg.yml)\n");
            this.append(b, reflex.getAdvcfg().saveToString());
         }

         try {
            this.link = Misc.postPaste(b.toString(), "https://p.reflex.rip", "Reflex-Debug/" + reflex.getDescription().getVersion());
            cs.sendMessage(
               Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getDebugSuccess())
                  .replace("%link%", this.link)
                  .colourizeAndGet()
            );
         } catch (IOException ex) {
            cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getDebugFailure()));
            ex.printStackTrace();
         }

         this.inProcess = false;
         this.lastTime = System.currentTimeMillis();
      }
   }

   private void append(StringBuilder b, String... s) {
      for (String s1 : s) {
         b.append(s1);
      }

      b.append('\n');
   }

   private String tryGet(Supplier<String> supp) {
      try {
         return supp.get();
      } catch (Exception ignored) {
         return "Cannot be resolved";
      }
   }
}
