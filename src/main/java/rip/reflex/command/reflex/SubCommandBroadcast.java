package rip.reflex.command.reflex;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.misc.Strings;

public class SubCommandBroadcast extends ReflexSubCommand {
   private static char[] intshit = new char[16];

   public SubCommandBroadcast() {
      super("broadcast", "reflex.command.broadcast");
      intshit = (char[])"喫園夢雪道前膳書語移置零鳴".toCharArray().clone();
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(
         Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getBroadcastUsage()).colourizeAndGet()
      );
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidMessage()), 0, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      String sender = cs instanceof Player ? cs.getName() : Reflex.getReflex().getTheme(cs).getConsole();
      String message = Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getBroadcastFormat())
         .replace("%sender%", sender)
         .replace("%message%", args.joinFrom(0))
         .replace("%break%", "\n")
         .replace("\\n", "\n")
         .colourizeAndGet();
      Bukkit.broadcastMessage(message);
   }

   public static char[] integrityShit() {
      return intshit;
   }
}
