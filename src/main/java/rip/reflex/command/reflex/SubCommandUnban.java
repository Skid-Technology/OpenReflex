package rip.reflex.command.reflex;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.misc.Strings;

public class SubCommandUnban extends ReflexSubCommand {
   public SubCommandUnban() {
      super("unban", "reflex.command.unban");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getUnbanUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidPlayer()), 0, ArgumentType.PLAYER_OFFLINE);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      OfflinePlayer op = args.getOfflinePlayer(0);
      if (Reflex.getReflex().getBanImpl().unban(args.getOfflinePlayer(0).getUniqueId())) {
         cs.sendMessage(
            Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getUnbanned())
               .replace("%player%", op.getName())
               .colourizeAndGet()
         );
      } else {
         cs.sendMessage(
            Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getNotBanned())
               .replace("%player%", op.getName())
               .colourizeAndGet()
         );
      }
   }
}
