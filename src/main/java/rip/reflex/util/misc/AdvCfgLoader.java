package rip.reflex.util.misc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import rip.reflex.Reflex;

public class AdvCfgLoader {
   public static final String DEFAULT_NAME = "DefaultConfiguration";
   public static final String DEFAULT_PROVIDER = "Reflex Development Team";
   public static final int DEFAULT_CONFIG_HASH = -1659450857;
   private static int lastLoadedHash = 0;

   public static boolean loadAdvancedConfiguration(Reflex reflex, FileConfiguration advcfg) {
      File cfgFile = checkInstallation(reflex);
      if (cfgFile == null) {
         reflex.warn("Failed to load advanced configuration file [0]. Defaults one love! (please report this)");
         return false;
      } else {
         try {
            advcfg.load(cfgFile);
            lastLoadedHash = advcfg.saveToString().hashCode();
            return true;
         } catch (InvalidConfigurationException | IOException ex) {
            reflex.warn("Failed to load advanced configuration file [1]. Defaults one love! (please report this)");
            reflex.warn("Cause:");
            ex.printStackTrace();
            return false;
         }
      }
   }

   private static File checkInstallation(Reflex reflex) {
      File cfgFile = reflex.getReflexFile("advcfg.yml");
      if (!cfgFile.exists()) {
         try (InputStream is = reflex.getResource("advcfg.yml")) {
            FileUtils.copyInputStreamToFile(is, cfgFile);
         } catch (IOException e) {
            reflex.warn("Failed to install advcfg.yml");
            e.printStackTrace();
            return null;
         }
      }

      return cfgFile;
   }

   public static int getLastLoadedHash() {
      return lastLoadedHash;
   }
}
