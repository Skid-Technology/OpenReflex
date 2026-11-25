package rip.reflex.cloud;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import me.darksidecode.easysockets.client.BasicSocketClient;
import me.darksidecode.easysockets.client.config.SCConfigBuilder;
import me.darksidecode.easysockets.client.config.SocketClientConfig;
import me.darksidecode.easysockets.event.client.ClientTimeoutEvent;
import org.bukkit.Bukkit;
import rip.reflex.Reflex;
import rip.reflex.check.base.Check;
import rip.reflex.check.base.CheckLoader;
import rip.reflex.cloud.listener.AuthResponseListener;
import rip.reflex.cloud.listener.ComputeResponseListener;
import rip.reflex.cloud.listener.PingListener;
import rip.reflex.cloud.protocol.client.AuthRequestPacket;
import rip.reflex.util.location.Movement;
import rip.reflex.util.machinelearning.Classifier;
import rip.reflex.util.thread.ReflexRunnable;
import rip.reflex.util.thread.ReflexScheduler;
import rip.reflex.util.time.Timer;

public class CloudService {
   public static final int PROTOCOL_VERSION = 3;
   private static final String TRUST_STORE_FILE_NAME = "reflex-ts.jks";
   private static final String TRUST_STORE_PASSWORD = "Nfn4er";
   private static final int HEARTBEAT_PERIOD = 6000;
   private static final int HEARTBEAT_TIMEOUT = 60000;
   private static final long MIN_RECONNECT_DELAY = 30000L;
   private final Reflex reflex;
   private String lastKey;
   private CloudComputer cloudComputer;
   private BasicSocketClient client;
   private int ping;
   private boolean stopped;
   private Timer stopTracker = new Timer(0L);
   private Timer pingTracker = new Timer(0L);
   private int reconnections;
   private boolean heartbeatRan;
   private boolean reconnecting;
   private final Object connectionLock = new Object();
   private final List<Check> cloudSupportedChecks = new ArrayList<>();
   private final Timer connectTracker = new Timer(0L);

   public CloudService(Reflex reflex) {
      this.reflex = reflex;
   }

   public boolean isConnected() {
      return this.cloudComputer != null && !this.stopped;
   }

   public void reconnect() {
      if (!this.stopped) {
         int maxReconnections = this.reflex.getSetting("general.cloud_computings.max_reconnections", 5);
         if (maxReconnections > 0) {
            synchronized (this.connectionLock) {
               if (++this.reconnections > maxReconnections) {
                  this.reflex.info("Failed to reconnect after " + maxReconnections + " attempts. Giving up.");
                  this.stop();
                  return;
               }

               this.reflex.info("Performing reconnection attempt " + this.reconnections + "/" + maxReconnections + "...");
               if (this.client != null) {
                  this.client.shutdown();
                  this.client = null;
               }

               this.cloudComputer = null;
               this.connect(this.lastKey);
            }
         } else {
            this.reflex.info("general.cloud_computings.max_reconnections is set to " + maxReconnections + ". No reconnection attempts will be performed");
            this.stop();
         }
      }
   }

   public void runHeartbeat() {
      if (!this.heartbeatRan) {
         this.heartbeatRan = true;
         this.pingTracker.reset();
         ReflexScheduler.async().runTimer(new ReflexRunnable("PS-32391945") {
            @Override
            public void execute() throws Exception {
               if (!CloudService.this.stopped && !CloudService.this.reconnecting && CloudService.this.pingTracker.hasPassed(60000L)) {
                  CloudService.this.connectionLost("heartbeat");
               }
            }
         }, 6000);
      }
   }

   private void connectionLost(String reason) {
      this.reflex.warn("Lost connection with Cloud! (" + reason + ")");
      this.cloudComputer = null;
      this.reconnecting = true;

      try {
         this.reconnect();
      } catch (Throwable ex) {
         System.err.println("ERROR DURING RECONNECTION");
         ex.printStackTrace();
      }

      this.reconnecting = false;
   }

   public void connect(String token) {
      if (!this.stopped) {
         new Thread(() -> {
            synchronized (this.connectionLock) {
               try {
                  if (!this.connectTracker.hasPassed(30000L)) {
                     throw new IllegalStateException("Reconnect limit of 1 time per 30 seconds exceeded");
                  }

                  this.connectTracker.reset();
                  this.cloudComputer = null;
                  this.lastKey = token;
                  if (this.client != null) {
                     this.reflex.warn("There is already an active Cloud connection. Closing it");
                     this.client.shutdown();
                  }

                  if (token == null || token.trim().length() < 2) {
                     this.reflex.warn("Invalid Cloud token. No connection attempt will be performed");
                     CheckLoader.loadAll(this.reflex);
                     return;
                  }

                  this.reflex.info("Connecting to Cloud...");
                  int port = this.reflex.getSetting("general.cloud_computings.port", 8109);
                  if (port != 8109) {
                     this.reflex.warn("Connecting to Cloud through NON-DEFAULT PORT!");
                  }

                  SocketClientConfig config = new SCConfigBuilder().sendRateLimit(100).receiveRateLimit(100).maxPacketSize(33554432).timeout(60000).build();
                  boolean cloudSSL = this.reflex.getSetting("general.cloud_computings.ssl", false);
                  if (cloudSSL) {
                     config.setUseSSL(true);
                     config.setTrustStoreStream(this.getTrustStore());
                     config.setTrustStorePassword("Nfn4er");
                  }

                  String host = this.reflex.getSyncServer().getCloudHost();
                  this.client = new BasicSocketClient(3, host, port, null, config);
                  this.client.getEventManager().registerListener(new PingListener());
                  this.client.getEventManager().registerListener(new AuthResponseListener());
                  this.client.getEventManager().registerListener(new ComputeResponseListener());
                  this.client.getEventManager().registerListener(e -> {
                     if (!this.stopped && e instanceof ClientTimeoutEvent) {
                        this.reflex.warn("Cloud may has stopped responding (scheduled restart?)");
                        this.cloudComputer = null;
                     }
                  });

                  int id;
                  try {
                     id = Integer.parseInt(Movement.getApinstance().getId());
                  } catch (Exception ignored) {
                     Bukkit.getPluginManager().disablePlugin(this.reflex);
                     return;
                  }

                  this.client.getProtocolManager().sendPacket(new AuthRequestPacket(id, token));
               } catch (Exception cause) {
                  if (this.client != null) {
                     this.client.shutdown();
                     this.client = null;
                  }

                  this.reflex.warn("Failed to connect to Cloud: " + cause.toString());
                  this.cloudComputer = null;
               }
            }
         }, "Reflex Cloud Connection Thread").start();
      }
   }

   private InputStream getTrustStore() {
      return this.reflex.getResource("reflex-ts.jks");
   }

   private void stop() {
      synchronized (this.connectionLock) {
         this.reflex.info("Reflex Cloud Service shutting down");
         this.stopped = true;
         this.cloudComputer = null;
         this.stopTracker.reset();
         if (this.reflex.getSetting("general.cloud_computings.auto_switch", true)) {
            if (Classifier.getNetlist().isEmpty()) {
               ReflexScheduler.async().run(ReflexRunnable.simple("CY-4921934", Classifier::init));
            }
         } else {
            this.cloudSupportedChecks.forEach(Check::disableComponent);
         }
      }
   }

   public CloudComputer getCloudComputer() {
      return this.cloudComputer;
   }

   public void setCloudComputer(CloudComputer cloudComputer) {
      this.cloudComputer = cloudComputer;
   }

   public BasicSocketClient getClient() {
      return this.client;
   }

   public int getPing() {
      return this.ping;
   }

   public void setPing(int ping) {
      this.ping = ping;
   }

   public Timer getPingTracker() {
      return this.pingTracker;
   }

   public void setReconnections(int reconnections) {
      this.reconnections = reconnections;
   }

   public List<Check> getCloudSupportedChecks() {
      return this.cloudSupportedChecks;
   }
}
