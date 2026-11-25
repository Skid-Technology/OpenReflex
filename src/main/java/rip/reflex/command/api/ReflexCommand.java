package rip.reflex.command.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import rip.reflex.Reflex;
import rip.reflex.util.data.Stats;
import rip.reflex.util.location.Movement;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public abstract class ReflexCommand implements CommandExecutor {
   private final String name;
   private final String permission;
   protected final List<ReflexSubCommand> subCommands = new ArrayList<>();
   private final List<String> permList = new ArrayList<>();

   public ReflexCommand(String name, String permission) {
      this.name = name;
      this.permission = permission;
   }

   public static void register(ReflexCommand cmd) {
      cmd.register();
   }

   protected abstract void check(CommandSender sender, CommandConditions conditions);

   protected abstract void execute(CommandSender sender, CommandArguments args);

   public boolean onCommand(CommandSender cs, Command command, String s, String[] args) {
      this.onCommand(cs, command, s, args, false);
      return true;
   }

   private void onCommand(final CommandSender cs, final Command cmd, final String label, final String[] args, boolean fromPacket) {
      if (!(cs instanceof Player) || fromPacket) {
         ReflexScheduler.async().run(new ReflexRunnable("command-" + this.getName()) {
            @Override
            public void execute() throws Exception {
               try {
                  ReflexCommand.this.onCommandAsync(cs, cmd, label, args);
               } catch (Exception ex) {
                  cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + "Error executing command. See console for details"));
                  System.out.println("Error executing command '" + Strings.join(0, args, " ") + "'");
                  ex.printStackTrace();
               }
            }
         });
      }
   }

   public void onCommandAsync(CommandSender cs, Command cmd, String label, String[] sargs) {
      CommandArguments args = new CommandArguments(sargs);
      if (!this.subCommands.isEmpty() && sargs.length != 0) {
         ReflexSubCommand subCommand = args.has(0)
            ? this.subCommands.stream().filter(rsc -> args.get(0).equalsIgnoreCase(rsc.getName())).findFirst().orElse(null)
            : null;
         if (subCommand == null) {
            cs.sendMessage(Strings.rformat(cs, Reflex.getReflex().getTheme(cs).getCmdUnknownSubcmd()));
         } else {
            boolean permitted = cs instanceof Player
               ? Stats.of((Player)cs).hasPermission(subCommand.getPermission())
               : cs.hasPermission(subCommand.getPermission());
            if (!permitted) {
               cs.sendMessage(Strings.rformat(cs, Reflex.getReflex().getTheme(cs).getNoPermissions()));
            } else {
               CommandConditions cc = new CommandConditions(args.cut(), cs);
               subCommand.check(cs, cc);
               if (cc.isOk()) {
                  subCommand.execute(cs, args);
               }
            }
         }
      } else {
         CommandConditions cc = new CommandConditions(args, cs);
         this.check(cs, cc);
         if (cc.isOk()) {
            this.execute(cs, args);
         }
      }
   }

   public void addSubCommand(ReflexSubCommand rsc) {
      this.subCommands.add(rsc);
      this.permList.add(rsc.getPermission());
   }

   public void addSimpleSubCommand(String name, String permission, final BiConsumer<CommandSender, CommandArguments> consumer) {
      this.addSubCommand(new ReflexSubCommand(name, permission) {
         @Override
         protected void check(CommandSender cs, CommandConditions cc) {
         }

         @Override
         protected void execute(CommandSender cs, CommandArguments args) {
            consumer.accept(cs, args);
         }
      });
   }

   private void register() {
      Reflex.getReflex().getCommand(this.name).setExecutor(this);
   }

   public void verifyCommandMonopoly() {
      for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
         PluginDescriptionFile desc = p.getDescription();
         if (Strings.xorHash(desc.getName()) == Reflex.vrf[7]) {
            if (Strings.xorHash(desc.getMain()) != Reflex.vrf[18]) {
               Reflex.getReflex().warn("Corrupted Reflex copy found. Disabling to avoid unwanted magic");
               Bukkit.getPluginManager().disablePlugin(Reflex.getReflex());
               throw new IllegalStateException("Corrupted Reflex copy found. Disabling to avoid unwanted magic");
            }
         } else if (desc.getCommands() != null) {
            for (String cmd : desc.getCommands().keySet()) {
               if (cmd.contains(":")) {
                  cmd = cmd.split(Pattern.quote(":"))[1];
               }

               if (cmd.equalsIgnoreCase("reflex")) {
                  Reflex.getReflex().warn("There seems to be a plugin that attempts to override the /reflex command. Disabling because that's sad :(");
                  Bukkit.getPluginManager().disablePlugin(Reflex.getReflex());
                  throw new IllegalStateException("There seems to be a plugin that attempts to override the /reflex command. Disabling because that's sad :(");
               }
            }
         }
      }
   }

   public void execute(Player p, String s) {
      String[] args = s.toLowerCase().equals("/reflex") ? new String[0] : Strings.join(1, s.split(" "), " ").split(" ");
      this.onCommand(p, null, "reflex", args, true);
   }

   private String[] check(String g) {
      try {
         Class reflex = Class.forName("rip.reflex.Reflex");

         try {
            Method classFingerprint = reflex.getDeclaredMethod("classFingerprint", String.class);
            classFingerprint.setAccessible(true);
            String fingerprint = (String)classFingerprint.invoke(
               Reflex.getReflex(), "喫園夢雪道前膳書語移置零鳴" + hex((long)Movement.getApinstance().getResource().hashCode())
            );
            classFingerprint.setAccessible(false);

            try {
               CommandConditions.integrityRan[4] = CommandConditions.integrityRan[4] - (-972);
               Method arrInfo = reflex.getDeclaredMethod("arrInfo", long.class);
               arrInfo.setAccessible(true);
               long info = (Long)arrInfo.invoke(Reflex.getReflex(), System.nanoTime());
               arrInfo.setAccessible(false);
               String[] s = new String[Integer.parseInt(g, 10)];

               try {
                  s[1] = fingerprint;
                  s[0] = hex(info);
                  return s;
               } catch (Throwable ex) {
                  throw new RuntimeException("4", ex);
               }
            } catch (Throwable e) {
               throw new RuntimeException("3", e);
            }
         } catch (Throwable err) {
            throw new RuntimeException("2", err);
         }
      } catch (Throwable cause) {
         throw new RuntimeException("1", cause);
      }
   }

   private static String hex(long l) {
      return Long.toString(l, 16).replace("-", "").toUpperCase();
   }

   public String getName() {
      return this.name;
   }

   public String getPermission() {
      return this.permission;
   }
}
