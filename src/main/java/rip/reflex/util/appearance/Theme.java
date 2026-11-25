package rip.reflex.util.appearance;

import com.google.gson.annotations.Expose;

public class Theme {
   @Expose
   private String name = "ModernBlue_EN";
   @Expose
   private String author = "Reflex Development Team (DarksideCode)";
   @Expose
   private String openingBracket = "&9&l「";
   @Expose
   private String closingBracket = "&9&l」&r";
   @Expose
   private String title = "&r&bReflex";
   @Expose
   private String noPermissions = "You're too weak! More power is needed";
   @Expose
   private String cmdInvalidPlayer = "The specified player is that far away that I can't find them! ";
   @Expose
   private String cmdInvalidMessage = "The entered message is either invalid or not actually entered";
   @Expose
   private String cmdInvalidDuration = "Invalid duration. Format: '-1' for permanent; '1[s|m|h|d|w|M|Y]' for 1 second|minute|hour|day|week|month|year";
   @Expose
   private String cmdInvalidCheck = "The entered check name is either invalid or not actually entered";
   @Expose
   private String cmdInvalidBoolean = "Invalid boolean parameter. Must be either &9true&r or &9false&r";
   @Expose
   private String cmdInvalidInt = "The entered integer is either invalid or not actually entered";
   @Expose
   private String cmdNegativeInt = "The number must be greater than zero";
   @Expose
   private String cmdInvalidCommand = "The entered command is either invalid or not actually entered";
   @Expose
   private String cmdInvalidOperation = "Although I'm kinda professional, I can't perform that operation... Maybe because it's missing...";
   @Expose
   private String cmdInvalidDbType = "Invalid database type (sadly, only MySQL is currently supported)";
   @Expose
   private String cmdInvalidDbHost = "Invalid database host";
   @Expose
   private String cmdInvalidDbPort = "Invalid database port";
   @Expose
   private String cmdInvalidDbName = "Invalid database name";
   @Expose
   private String cmdInvalidDbUser = "Invalid database username";
   @Expose
   private String cmdInvalidDbPass = "Invalid database password";
   @Expose
   private String cmdPlayersOnly = "Too much power! Try to do that gentler or as a player. &9&lTIP&r&9:&r for console verbose, turn the &9general.console_verbose&r option in the config on";
   @Expose
   private String cmdUnknownSubcmd = "Are you trying to break everything again?! Please use &9/reflex help&r for the list of all commands Reflex actually has!";
   @Expose
   private String debugBusy = "Hold on! Debug collection is already in progress...";
   @Expose
   private String debugSpam = "A debug paste has already been created recently: &9%link%";
   @Expose
   private String creatingDebug = "Hold on! Creating debug paste...";
   @Expose
   private String debugSuccess = "Successfully created a debug paste: &9%link%";
   @Expose
   private String debugFailure = "Failed to create a debug paste. See console for details";
   @Expose
   private String cloud = "CLOUD";
   @Expose
   private String console = "CONSOLE";
   @Expose
   private String tps = "According to Reflex, the server ticks around &9%tps%&r times in a second";
   @Expose
   private String configReloaded = "Configuration has been reloaded successfully";
   @Expose
   private String allowedBypass = "&7%player%&r can now bypass &9%check%&r. I'm not responsible if they destroy the server now!";
   @Expose
   private String forbadeBypass = "&7%player%&r can no longer bypass &9%check%&r. Safety is back!";
   @Expose
   private String dumpingBegan = "Hold on! Dumping...";
   @Expose
   private String dumpingDone = "Successfully created a dump: &9%file%";
   @Expose
   private String enabledPacketDebug = "Enabled packet debug for &9%player%";
   @Expose
   private String disabledPacketDebug = "Disabled packet debug for &9%player%";
   @Expose
   private String packetDebugSpam = "Hold on! You are doing saves too quickly!";
   @Expose
   private String packetDebugInactive = "Packet debugging for &9%player%&r is disabled. Enable it with &9/reflex packetdebug toggle %player%&r first!";
   @Expose
   private String packetDebugInProgress = "Hold on! Saving is already in progress...";
   @Expose
   private String packetDebugSaved = "Packet debug for player &9%player%&r was saved successfully: &9%link%";
   @Expose
   private String reflexReset = "Let the &9%player%&r's sins go";
   @Expose
   private String setdbSuccess = "The database configuration has been updated successfully. If you wish to clone it to other servers, just copy the &9dbconf&r file which can be found in the Reflex working directory";
   @Expose
   private String timingsAlreadyActive = "Timings are already active!";
   @Expose
   private String timingsEnabled = "Enabled timings. Watching the performance now!";
   @Expose
   private String timingsInactive = "Timings turned off. Enable them with &9/reflex timings on";
   @Expose
   private String timingsSaving = "Hold on! The timings are being saved...";
   @Expose
   private String timingsSaved = "Done! The file with timings can be found in &9%dir%";
   @Expose
   private String unbanned = "Unbanned player &9%player%";
   @Expose
   private String notBanned = "The player &9%player%&r is not banned.. yet..";
   @Expose
   private String enabledVerbose = "Enabled verbose. Prepare for 20 GB chat messages/ms!";
   @Expose
   private String disabledVerbose = "Disabled verbose. Let the tranquility reign!";
   @Expose
   private String usage = "Usage: &9%usage%";
   @Expose
   private String banUsage = "ban <player> <duration> <message>";
   @Expose
   private String broadcastUsage = "broadcast <message>";
   @Expose
   private String bypassUsage = "bypass <player> <check> [true|false]";
   @Expose
   private String delayUsage = "delay <seconds> <command>";
   @Expose
   private String kickUsage = "kick <player> <message>";
   @Expose
   private String notifyUsage = "notify <message>";
   @Expose
   private String packetdebugUsage = "packetdebug <toggle|save> <player>";
   @Expose
   private String resetUsage = "reset <player>";
   @Expose
   private String setdbUsage = "setdb <type> <host> <port> <database name> <username> <password>";
   @Expose
   private String timingsUsage = "timings <on|save>";
   @Expose
   private String unbanUsage = "unban <player>";
   @Expose
   private String banBroadcast = "&7%player% &fwas banned for cheating";
   @Expose
   private String kickBroadcast = "&7%player% &fwas kicked for cheating";
   @Expose
   private String reflexHelpTitle = "Available commands:";
   @Expose
   private String cmdDescReflex = "Display info about the installed Reflex version";
   @Expose
   private String cmdDescReflexHelp = "Display the list of available commands";
   @Expose
   private String cmdDescReflexBroadcast = "Broadcast a message to all online players";
   @Expose
   private String cmdDescReflexNotify = "Broadcast a message to the staff members";
   @Expose
   private String cmdDescReflexVerbose = "Toggle in-game logs (verbose)";
   @Expose
   private String cmdDescReflexKick = "Kick a player with configured visuals";
   @Expose
   private String cmdDescReflexBan = "Ban a player with configured visuals";
   @Expose
   private String cmdDescReflexUnban = "Unban a player banned using Reflex ban";
   @Expose
   private String cmdDescReflexDelay = "Run a command with a delay";
   @Expose
   private String cmdDescReflexReload = "Reload Reflex configuration";
   @Expose
   private String cmdDescReflexSetdb = "Setup a database";
   @Expose
   private String cmdDescReflexBypass = "Toggle the bypass of a check for a player";
   @Expose
   private String cmdDescReflexTimings = "Detailed performance and timing control";
   @Expose
   private String cmdDescReflexTps = "Display the server TPS calculated by Reflex";
   @Expose
   private String cmdDescReflexDebug = "Create a server info paste for bug reports";
   @Expose
   private String cmdDescReflexReset = "Reset all the Reflex data for a player";
   @Expose
   private String cmdDescReflexDump = "Dump detailed Reflex memory/CPU usage";
   @Expose
   private String cmdDescReflexPacketdebug = "Log server packets sent to a player";
   @Expose
   private String broadcastFormat = "&7%sender%&r %message%";
   @Expose
   private String notifyFormat = "&9&l（&r&bStaff&9&l） &7%sender%&r %message%";
   @Expose
   private String verboseFormat = "&7%player% &f%check% &9%vl% (+%mod%)%tags%";
   @Expose
   private String verboseTagsCol = "&7";
   @Expose
   private String verboseTagsSeparator = "&8・";
   @Expose
   private String notStartedUp = "&cThe server is still starting up.%break%&rPlease try again in a few minutes!";
   @Expose
   private String serverRestarting = "&cThe server is reloading or restarting.%break%&rCome back in a few minutes!";
   private String prefix;

   public String getPrefix() {
      return this.prefix == null ? (this.prefix = this.openingBracket + this.title + this.closingBracket) : this.prefix;
   }

   public String getName() {
      return this.name;
   }

   public String getAuthor() {
      return this.author;
   }

   public String getOpeningBracket() {
      return this.openingBracket;
   }

   public String getClosingBracket() {
      return this.closingBracket;
   }

   public String getTitle() {
      return this.title;
   }

   public String getNoPermissions() {
      return this.noPermissions;
   }

   public String getCmdInvalidPlayer() {
      return this.cmdInvalidPlayer;
   }

   public String getCmdInvalidMessage() {
      return this.cmdInvalidMessage;
   }

   public String getCmdInvalidDuration() {
      return this.cmdInvalidDuration;
   }

   public String getCmdInvalidCheck() {
      return this.cmdInvalidCheck;
   }

   public String getCmdInvalidBoolean() {
      return this.cmdInvalidBoolean;
   }

   public String getCmdInvalidInt() {
      return this.cmdInvalidInt;
   }

   public String getCmdNegativeInt() {
      return this.cmdNegativeInt;
   }

   public String getCmdInvalidCommand() {
      return this.cmdInvalidCommand;
   }

   public String getCmdInvalidOperation() {
      return this.cmdInvalidOperation;
   }

   public String getCmdInvalidDbType() {
      return this.cmdInvalidDbType;
   }

   public String getCmdInvalidDbHost() {
      return this.cmdInvalidDbHost;
   }

   public String getCmdInvalidDbPort() {
      return this.cmdInvalidDbPort;
   }

   public String getCmdInvalidDbName() {
      return this.cmdInvalidDbName;
   }

   public String getCmdInvalidDbUser() {
      return this.cmdInvalidDbUser;
   }

   public String getCmdInvalidDbPass() {
      return this.cmdInvalidDbPass;
   }

   public String getCmdPlayersOnly() {
      return this.cmdPlayersOnly;
   }

   public String getCmdUnknownSubcmd() {
      return this.cmdUnknownSubcmd;
   }

   public String getDebugBusy() {
      return this.debugBusy;
   }

   public String getDebugSpam() {
      return this.debugSpam;
   }

   public String getCreatingDebug() {
      return this.creatingDebug;
   }

   public String getDebugSuccess() {
      return this.debugSuccess;
   }

   public String getDebugFailure() {
      return this.debugFailure;
   }

   public String getCloud() {
      return this.cloud;
   }

   public String getConsole() {
      return this.console;
   }

   public String getTps() {
      return this.tps;
   }

   public String getConfigReloaded() {
      return this.configReloaded;
   }

   public String getAllowedBypass() {
      return this.allowedBypass;
   }

   public String getForbadeBypass() {
      return this.forbadeBypass;
   }

   public String getDumpingBegan() {
      return this.dumpingBegan;
   }

   public String getDumpingDone() {
      return this.dumpingDone;
   }

   public String getEnabledPacketDebug() {
      return this.enabledPacketDebug;
   }

   public String getDisabledPacketDebug() {
      return this.disabledPacketDebug;
   }

   public String getPacketDebugSpam() {
      return this.packetDebugSpam;
   }

   public String getPacketDebugInactive() {
      return this.packetDebugInactive;
   }

   public String getPacketDebugInProgress() {
      return this.packetDebugInProgress;
   }

   public String getPacketDebugSaved() {
      return this.packetDebugSaved;
   }

   public String getReflexReset() {
      return this.reflexReset;
   }

   public String getSetdbSuccess() {
      return this.setdbSuccess;
   }

   public String getTimingsAlreadyActive() {
      return this.timingsAlreadyActive;
   }

   public String getTimingsEnabled() {
      return this.timingsEnabled;
   }

   public String getTimingsInactive() {
      return this.timingsInactive;
   }

   public String getTimingsSaving() {
      return this.timingsSaving;
   }

   public String getTimingsSaved() {
      return this.timingsSaved;
   }

   public String getUnbanned() {
      return this.unbanned;
   }

   public String getNotBanned() {
      return this.notBanned;
   }

   public String getEnabledVerbose() {
      return this.enabledVerbose;
   }

   public String getDisabledVerbose() {
      return this.disabledVerbose;
   }

   public String getUsage() {
      return this.usage;
   }

   public String getBanUsage() {
      return this.banUsage;
   }

   public String getBroadcastUsage() {
      return this.broadcastUsage;
   }

   public String getBypassUsage() {
      return this.bypassUsage;
   }

   public String getDelayUsage() {
      return this.delayUsage;
   }

   public String getKickUsage() {
      return this.kickUsage;
   }

   public String getNotifyUsage() {
      return this.notifyUsage;
   }

   public String getPacketdebugUsage() {
      return this.packetdebugUsage;
   }

   public String getResetUsage() {
      return this.resetUsage;
   }

   public String getSetdbUsage() {
      return this.setdbUsage;
   }

   public String getTimingsUsage() {
      return this.timingsUsage;
   }

   public String getUnbanUsage() {
      return this.unbanUsage;
   }

   public String getBanBroadcast() {
      return this.banBroadcast;
   }

   public String getKickBroadcast() {
      return this.kickBroadcast;
   }

   public String getReflexHelpTitle() {
      return this.reflexHelpTitle;
   }

   public String getCmdDescReflex() {
      return this.cmdDescReflex;
   }

   public String getCmdDescReflexHelp() {
      return this.cmdDescReflexHelp;
   }

   public String getCmdDescReflexBroadcast() {
      return this.cmdDescReflexBroadcast;
   }

   public String getCmdDescReflexNotify() {
      return this.cmdDescReflexNotify;
   }

   public String getCmdDescReflexVerbose() {
      return this.cmdDescReflexVerbose;
   }

   public String getCmdDescReflexKick() {
      return this.cmdDescReflexKick;
   }

   public String getCmdDescReflexBan() {
      return this.cmdDescReflexBan;
   }

   public String getCmdDescReflexUnban() {
      return this.cmdDescReflexUnban;
   }

   public String getCmdDescReflexDelay() {
      return this.cmdDescReflexDelay;
   }

   public String getCmdDescReflexReload() {
      return this.cmdDescReflexReload;
   }

   public String getCmdDescReflexSetdb() {
      return this.cmdDescReflexSetdb;
   }

   public String getCmdDescReflexBypass() {
      return this.cmdDescReflexBypass;
   }

   public String getCmdDescReflexTimings() {
      return this.cmdDescReflexTimings;
   }

   public String getCmdDescReflexTps() {
      return this.cmdDescReflexTps;
   }

   public String getCmdDescReflexDebug() {
      return this.cmdDescReflexDebug;
   }

   public String getCmdDescReflexReset() {
      return this.cmdDescReflexReset;
   }

   public String getCmdDescReflexDump() {
      return this.cmdDescReflexDump;
   }

   public String getCmdDescReflexPacketdebug() {
      return this.cmdDescReflexPacketdebug;
   }

   public String getBroadcastFormat() {
      return this.broadcastFormat;
   }

   public String getNotifyFormat() {
      return this.notifyFormat;
   }

   public String getVerboseFormat() {
      return this.verboseFormat;
   }

   public String getVerboseTagsCol() {
      return this.verboseTagsCol;
   }

   public String getVerboseTagsSeparator() {
      return this.verboseTagsSeparator;
   }

   public String getNotStartedUp() {
      return this.notStartedUp;
   }

   public String getServerRestarting() {
      return this.serverRestarting;
   }
}
