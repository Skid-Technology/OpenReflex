package rip.reflex.command.api;

import org.bukkit.command.CommandSender;

public abstract class ReflexSubCommand {
   private final String name;
   private final String permission;
   private String noperm;

   protected abstract void check(CommandSender sender, CommandConditions conditions);

   protected abstract void execute(CommandSender sender, CommandArguments args);

   public ReflexSubCommand(String name, String permission) {
      this.name = name;
      this.permission = permission;
   }

   public ReflexSubCommand(String name, String permission, String noperm) {
      this.name = name;
      this.permission = permission;
      this.noperm = noperm;
   }

   public String getName() {
      return this.name;
   }

   public String getPermission() {
      return this.permission;
   }

   public void setNoperm(String noperm) {
      this.noperm = noperm;
   }

   public String getNoperm() {
      return this.noperm;
   }
}
