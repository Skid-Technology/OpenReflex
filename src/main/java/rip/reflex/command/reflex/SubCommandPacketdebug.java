package rip.reflex.command.reflex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.debugging.PacketDebugger;
import rip.reflex.util.misc.Misc;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.time.Timer;

public class SubCommandPacketdebug extends ReflexSubCommand {
   private static final Map<CommandSender, List<Player>> debugMap = new ConcurrentHashMap<>();
   private final Timer postTracker = new Timer().subtractSeconds(3L);
   private boolean posting;

   public SubCommandPacketdebug() {
      super("packetdebug", "reflex.command.packetdebug");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(
         Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getPacketdebugUsage()).colourizeAndGet()
      );
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidOperation()), 0, ArgumentType.STRING);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidPlayer()), 1, ArgumentType.PLAYER);
   }

   public static void dumpTo(CommandSender cs, Player p) {
      if (cs != null) {
         if (cs == p && !p.isOnline()) {
            cs = Bukkit.getConsoleSender();
         }

         String s = PacketDebugger.printAndReset(p);
         if (s != null) {
            try {
               cs.sendMessage(
                  Strings.wrap(Reflex.getReflex().getTheme(p).getPrefix() + Reflex.getReflex().getTheme(p).getPacketDebugSaved())
                     .replace("%player%", p.getName())
                     .replace("%link%", Misc.postPaste(s, "https://p.reflex.rip", "Reflex-Debug/" + Reflex.getReflex().getDescription().getVersion()))
                     .placeholderApi(p)
                     .colourizeAndGet()
               );
            } catch (Exception ex) {
               throw new RuntimeException("Failed to post packet debug for " + p.getName(), ex);
            }

            getDebuggingList(cs).remove(p);
         }
      }
   }

   public static CommandSender getDebugger(Player p) {
      return debugMap.keySet().stream().filter(cs -> debugMap.get(cs).contains(p)).findFirst().orElse(null);
   }

   private static boolean register(CommandSender cs, Player p) {
      if (PacketDebugger.toogleDebug(p)) {
         getDebuggingList(cs).add(p);
         return true;
      } else {
         getDebuggingList(cs).remove(p);
         return false;
      }
   }

   private static List<Player> getDebuggingList(CommandSender cs) {
      if (debugMap.containsKey(cs)) {
         return debugMap.get(cs);
      } else {
         List<Player> l = new ArrayList<>();
         debugMap.put(cs, l);
         return l;
      }
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      String action = args.get(0).toLowerCase();
      Player p = args.getPlayer(1);
      if (!Reflex.getReflex().offlineMode) {
         switch (action) {
            case "toggle":
               cs.sendMessage(
                  Strings.wrap(
                        Reflex.getReflex().getTheme(p).getPrefix()
                           + (
                              register(cs, p)
                                 ? Reflex.getReflex().getTheme(p).getEnabledPacketDebug()
                                 : Reflex.getReflex().getTheme(p).getDisabledPacketDebug()
                           )
                     )
                     .replace("%player%", p.getName())
                     .placeholderApi(p)
                     .colourizeAndGet()
               );
               break;
            case "save":
               if (!this.postTracker.hasPassed(5000L)) {
                  cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(p).getPrefix() + Reflex.getReflex().getTheme(p).getPacketDebugSpam()));
               } else if (!PacketDebugger.isDebugging(p)) {
                  cs.sendMessage(
                     Strings.wrap(Reflex.getReflex().getTheme(p).getPrefix() + Reflex.getReflex().getTheme(p).getPacketDebugInactive())
                        .replace("%player%", p.getName())
                        .placeholderApi(p)
                        .colourizeAndGet()
                  );
               } else if (this.posting) {
                  cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(p).getPrefix() + Reflex.getReflex().getTheme(p).getPacketDebugInProgress()));
               } else {
                  this.posting = true;
                  dumpTo(cs, p);
                  this.posting = false;
               }
               break;
            default:
               cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(p).getPrefix() + Reflex.getReflex().getTheme(p).getCmdInvalidOperation()));
         }
      }
   }
}
