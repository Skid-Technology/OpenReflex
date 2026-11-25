package rip.reflex.command.reflex;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.command.api.ArgumentType;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class SubCommandDelay extends ReflexSubCommand {
   public SubCommandDelay() {
      super("delay", "reflex.command.delay");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      cc.usage(Strings.wrap(Reflex.getReflex().getTheme(cs).getUsage()).replace("%usage%", Reflex.getReflex().getTheme(cs).getDelayUsage()).colourizeAndGet());
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidInt()), 0, ArgumentType.INT);
      cc.check(Strings.color(Reflex.getReflex().getTheme(cs).getCmdNegativeInt()), args -> args.getInt(0) > 0);
      cc.checkArgument(Strings.color(Reflex.getReflex().getTheme(cs).getCmdInvalidCommand()), 1, ArgumentType.STRING);
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      ReflexScheduler.sync()
         .runLater(ReflexRunnable.simple("cmd-delay", () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), args.joinFrom(1))), args.getInt(0) * 20);
   }
}
