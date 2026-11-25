package rip.reflex.command.reflex;

import org.bukkit.command.CommandSender;
import rip.reflex.Reflex;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.util.misc.Strings;
import rip.reflex.util.performance.Memory;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class SubCommandDump extends ReflexSubCommand {
   public SubCommandDump() {
      super("dump", "reflex.command.dump");
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
      if (!Reflex.getReflex().offlineMode) {
         cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getDumpingBegan()));
         ReflexScheduler.async()
            .run(
               ReflexRunnable.simple(
                  "NG-2137281",
                  () -> cs.sendMessage(
                        Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getDumpingDone())
                           .replace("%file%", Memory.dump())
                           .colourizeAndGet()
                     )
               )
            );
      }
   }
}
