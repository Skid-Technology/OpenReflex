package rip.reflex.util.mod.badlionclient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import rip.reflex.Reflex;
import rip.reflex.util.misc.Files;

public class BadlionConfigMaker {
   private static final String CONFIG_FILE_NAME = "blc_config.json";
   private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().disableHtmlEscaping().create();
   private static byte[] cache = null;

   public static byte[] make(Reflex reflex) {
      return cache == null ? (cache = gson.toJson(make0(reflex).getModsDisallowed()).getBytes()) : cache;
   }

   public static void installConfigFile(Reflex r) {
      File file = r.getReflexFile("blc_config.json");
      if (!file.exists()) {
         Files.writeFromStream(r.getResource("blc_config.json"), file);
      }
   }

   private static BadlionConfig make0(Reflex r) {
      File file = r.getReflexFile("blc_config.json");
      installConfigFile(r);

      try (FileReader reader = new FileReader(file)) {
         BadlionConfig config = (BadlionConfig)gson.fromJson(reader, BadlionConfig.class);
         reader.close();
         return config;
      } catch (Exception ex) {
         throw new RuntimeException(
            "[Reflex/ClientControl] Failed to load BLC config. Make sure your JSON is valid. You can check it using https://jsonformatter.curiousconcept.com/"
               + file.getAbsolutePath(),
            ex
         );
      }
   }
}
