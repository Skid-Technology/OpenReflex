package org.bstats;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import rip.reflex.Reflex;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;

public class Metrics {
   public static final int B_STATS_VERSION = 1;
   private static final String URL = "https://bStats.org/submitData/bukkit";
   private static boolean logFailedRequests;
   private static String serverUUID;
   private final JavaPlugin plugin;
   private final List<Metrics.CustomChart> charts = new ArrayList<>();
   private boolean firstSubmit = true;

   public Metrics(JavaPlugin p) {
      if (p == null) {
         throw new IllegalArgumentException("Plugin cannot be null!");
      } else {
         this.plugin = p;
         File bStatsFolder = new File(p.getDataFolder().getParentFile(), "bStats");
         File configFile = new File(bStatsFolder, "config.yml");
         YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
         if (!config.isSet("serverUuid")) {
            config.addDefault("enabled", true);
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            config.addDefault("logFailedRequests", false);
            config.options()
               .header(
                  "bStats collects some data for plugin authors like how many servers are using their plugins.\nTo honor their work, you should not disable it.\nThis has nearly no effect on the server performance!\nCheck out https://bStats.org/ to learn more :)"
               )
               .copyDefaults(true);

            try {
               config.save(configFile);
            } catch (IOException ignored) {
            }
         }

         serverUUID = config.getString("serverUuid");
         logFailedRequests = config.getBoolean("logFailedRequests", false);
         if (config.getBoolean("enabled", true)) {
            boolean found = false;

            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
               try {
                  service.getField("B_STATS_VERSION");
                  found = true;
                  break;
               } catch (NoSuchFieldException ignoredEx) {
               }
            }

            ReflexScheduler.sync()
               .run(ReflexRunnable.simple("metric-load", () -> Bukkit.getServicesManager().register(Metrics.class, this, p, ServicePriority.Normal)));
            if (!found) {
               this.startSubmitting();
            }
         }
      }
   }

   private static void sendData(JSONObject data) throws Exception {
      if (data == null) {
         throw new IllegalArgumentException("Data cannot be null!");
      } else if (Bukkit.isPrimaryThread()) {
         throw new IllegalAccessException("This method must not be called from the f thread!");
      } else {
         HttpsURLConnection connection = (HttpsURLConnection)new URL("https://bStats.org/submitData/bukkit").openConnection();
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         byte[] compressedData = compress(data.toString());
         connection.setRequestMethod("POST");
         connection.addRequestProperty("Accept", "application/json");
         connection.addRequestProperty("Connection", "close");
         connection.addRequestProperty("Content-Encoding", "gzip");
         connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
         connection.setRequestProperty("Content-Type", "application/json");
         connection.setRequestProperty("User-Agent", "MC-Server/1");
         connection.setDoOutput(true);
         DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
         outputStream.write(compressedData);
         outputStream.flush();
         outputStream.close();
         connection.getInputStream().close();
      }
   }

   private static byte[] compress(String str) throws IOException {
      if (str == null) {
         return null;
      } else {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
         gzip.write(str.getBytes("UTF-8"));
         gzip.close();
         return outputStream.toByteArray();
      }
   }

   public void addCustomChart(Metrics.CustomChart chart) {
      if (chart == null) {
         throw new IllegalArgumentException("Chart cannot be null!");
      } else {
         this.charts.add(chart);
      }
   }

   private void startSubmitting() {
      if (this.firstSubmit) {
         Bukkit.getScheduler().runTaskLaterAsynchronously(Reflex.getReflex(), () -> {
            this.firstSubmit = false;
            this.startSubmitting();
         }, 6000L);
      } else {
         ReflexScheduler.sync().runTimer(new ReflexRunnable("M_987192") {
            @Override
            public void execute() throws Exception {
               if (Metrics.this.plugin.isEnabled()) {
                  Metrics.this.submitData();
               }
            }
         }, 36000);
      }
   }

   public JSONObject getPluginData() {
      JSONObject data = new JSONObject();
      String pluginName = this.plugin.getDescription().getName();
      String pluginVersion = this.plugin.getDescription().getVersion();
      data.put("pluginName", pluginName);
      data.put("pluginVersion", pluginVersion);
      JSONArray customCharts = this.charts
         .stream()
         .map(Metrics.CustomChart::getRequestJsonObject)
         .filter(Objects::nonNull)
         .collect(Collectors.toCollection(JSONArray::new));
      data.put("customCharts", customCharts);
      return data;
   }

   private JSONObject getServerData() {
      int playerAmount = Bukkit.getOnlinePlayers().size();
      int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
      String bukkitVersion = Bukkit.getVersion();
      bukkitVersion = bukkitVersion.substring(bukkitVersion.indexOf("MC: ") + 4, bukkitVersion.length() - 1);
      String javaVersion = System.getProperty("java.version");
      String osName = System.getProperty("os.name");
      String osArch = System.getProperty("os.arch");
      String osVersion = System.getProperty("os.version");
      int coreCount = Runtime.getRuntime().availableProcessors();
      JSONObject data = new JSONObject();
      data.put("serverUUID", serverUUID);
      data.put("playerAmount", playerAmount);
      data.put("onlineMode", onlineMode);
      data.put("bukkitVersion", bukkitVersion);
      data.put("javaVersion", javaVersion);
      data.put("osName", osName);
      data.put("osArch", osArch);
      data.put("osVersion", osVersion);
      data.put("coreCount", coreCount);
      return data;
   }

   private void submitData() {
      JSONObject data = this.getServerData();
      JSONArray pluginData = new JSONArray();

      for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
         try {
            service.getField("B_STATS_VERSION");
         } catch (NoSuchFieldException ignored) {
            continue;
         }

         try {
            pluginData.add(service.getMethod("getPluginData").invoke(Bukkit.getServicesManager().load(service)));
         } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignoredEx) {
         }
      }

      data.put("plugins", pluginData);
      Bukkit.getScheduler().runTaskAsynchronously(Reflex.getReflex(), () -> {
         try {
            sendData(data);
         } catch (Exception ex) {
            if (logFailedRequests) {
               this.plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + this.plugin.getName(), (Throwable)ex);
            }
         }
      });
   }

   public abstract static class CustomChart {
      protected final String chartId;

      public CustomChart(String chartId) {
         if (chartId != null && !chartId.isEmpty()) {
            this.chartId = chartId;
         } else {
            throw new IllegalArgumentException("ChartId cannot be null or empty!");
         }
      }

      protected JSONObject getRequestJsonObject() {
         JSONObject chart = new JSONObject();
         chart.put("chartId", this.chartId);

         try {
            JSONObject data = this.getChartData();
            if (data == null) {
               return null;
            } else {
               chart.put("data", data);
               return chart;
            }
         } catch (Throwable e) {
            if (Metrics.logFailedRequests) {
               Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + this.chartId, e);
            }

            return null;
         }
      }

      protected abstract JSONObject getChartData();
   }
}
