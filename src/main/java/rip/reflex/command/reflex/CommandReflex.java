package rip.reflex.command.reflex;

import org.bukkit.command.CommandSender;
import org.spigotmc.SpigotConfig;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckRegistry;
import rip.reflex.command.api.CommandArguments;
import rip.reflex.command.api.CommandConditions;
import rip.reflex.command.api.ReflexCommand;
import rip.reflex.command.api.ReflexSubCommand;
import rip.reflex.moonlight.HideMode;
import rip.reflex.util.db.logs.MySQLLogUtil;
import rip.reflex.util.location.Movement;
import rip.reflex.util.math.Maths;
import rip.reflex.util.misc.Strings;

public class CommandReflex extends ReflexCommand {
   public CommandReflex() {
      super("reflex", "reflex.command.reflex");
      this.addSubCommand(new SubCommandBypass());
      this.addSubCommand(new SubCommandKick());
      this.addSubCommand(new SubCommandBan());
      this.addSubCommand(new SubCommandUnban());
      this.addSubCommand(new SubCommandBroadcast());
      this.addSubCommand(new SubCommandSetDB());
      this.addSubCommand(new SubCommandVerbose());
      this.addSubCommand(new SubCommandDelay());
      this.addSubCommand(new SubCommandTimings());
      this.addSubCommand(new SubCommandDebug());
      this.addSubCommand(new SubCommandReset());
      this.addSubCommand(new SubCommandNotify());
      this.addSubCommand(new SubCommandDump());
      this.addSubCommand(new SubCommandPacketdebug());
      this.addSimpleSubCommand("help", "reflex.command.help", (cs, args) -> this.helpMessage(cs));
      this.addSimpleSubCommand("reload", "reflex.command.reload", (cs, args) -> this.reloadCommand(cs));
      this.addSimpleSubCommand(
         "tps",
         "reflex.command.tps",
         (cs, args) -> cs.sendMessage(
               Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getTps())
                  .replace("%tps%", Maths.toRoundedString(Reflex.getReflex().getTpsMeter().getAvgTps2()))
                  .colourizeAndGet()
            )
      );
   }

   @Override
   protected void check(CommandSender cs, CommandConditions cc) {
      if (!cc.getSender().isOp()
         && Reflex.getReflex().getMoonlight() != null
         && Reflex.getReflex().getMoonlight().isActive()
         && Reflex.getReflex().getMoonlight().getHideMode().hasMinimum(HideMode.COMMAND)) {
         cc.getSender().sendMessage(SpigotConfig.unknownCommandMessage);
      } else {
         cc.getSender()
            .sendMessage(
               Strings.wrap(
                     "&9&l「&r&bReflex&9&l」&rV: &9"
                        + Reflex.getReflex().getDescription().getVersion()
                        + "&7・&rCP: &9"
                        + 3
                        + "&7・&rNN: &9"
                        + (
                           Reflex.getReflex().getCloudService().isConnected()
                              ? Reflex.getReflex().getTheme(cs).getCloud() + "&r/&9" + Reflex.getReflex().getCloudService().getPing()
                              : Reflex.getReflex().getDataVersion()
                        )
                        + "&7・&rID: &9"
                        + (Movement.getApinstance().getId().equals(MySQLLogUtil.abcde(-392179402103138915L)) ? "?" : Movement.getApinstance().getId())
                  )
                  .colourizeAndGet()
            );
         boolean moonlightActive = Reflex.getReflex().getMoonlight() != null && Reflex.getReflex().getMoonlight().isActive();
         if (!moonlightActive && cs.isOp()) {
            cc.getSender()
               .sendMessage(
                  Strings.wrap(
                        "&r&lDid you know?&r&e Reflex has also got an extended version with several cool extra features that could be useful for your server! Learn more: &b&lhttps://g.reflex.rip/moonlight   &r&7(only players with OP see this message)"
                     )
                     .colourizeAndGet()
               );
         } else if (moonlightActive) {
            cc.getSender().sendMessage(Strings.wrap("&9&l「&r&bReflex&9&l」&r&4-*- &r&cMOONLIGHT EDITION &c&lo_O &r&4-*- ").colourizeAndGet());
         }

         cc.setOk(false);
      }
   }

   @Override
   protected void execute(CommandSender cs, CommandArguments args) {
   }

   private void reloadCommand(CommandSender cs) {
      Reflex.getReflex().reloadConfig();

      for (Check check : CheckRegistry.loadedChecksList()) {
         if (check.isEnabled() && !check.isEnabledInConfig()) {
            check.disable();
         }

         if (check.isDisabled() && check.isEnabledInConfig()) {
            check.enable();
         }
      }

      cs.sendMessage(Strings.color(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getConfigReloaded()));
   }

   private void helpMessage(CommandSender cs) {
      cs.sendMessage(Strings.wrap(Reflex.getReflex().getTheme(cs).getPrefix() + Reflex.getReflex().getTheme(cs).getReflexHelpTitle()).colourizeAndGet());
      cs.sendMessage(Strings.wrap("  &9reflex&7 - &r" + Reflex.getReflex().getTheme(cs).getCmdDescReflex()).colourizeAndGet());

      for (ReflexSubCommand sub : this.subCommands) {
         if (cs.hasPermission(sub.getPermission())) {
            cs.sendMessage(
               Strings.wrap(
                     "  &9" + sub.getName() + " &7- &r" + Reflex.getReflex().getRawTheme(cs).get("cmdDescReflex" + Strings.firstUppercase(sub.getName()))
                  )
                  .colourizeAndGet()
            );
         }
      }
   }
}
