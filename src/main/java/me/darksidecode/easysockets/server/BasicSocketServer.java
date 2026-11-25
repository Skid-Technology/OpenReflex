package me.darksidecode.easysockets.server;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import me.darksidecode.easysockets.event.EventCancelledException;
import me.darksidecode.easysockets.event.EventManager;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.event.server.ServerStartEvent;
import me.darksidecode.easysockets.event.server.ServerStopEvent;
import me.darksidecode.easysockets.server.config.SocketServerConfig;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BasicSocketServer {
   private static final long KICK_FREQ = TimeUnit.MINUTES.toMillis(1L);
   private final Object kickQueueLock = new Object();
   private final int protocolVersion;
   private final int port;
   private final ServerSocket serverSocket;
   private final ProtocolManager protocolManager;
   private final EventManager eventManager;
   private final SocketServerConfig config;
   private final Map<Socket, SocketListener> socketListeners = new ConcurrentHashMap<>();
   private final Set<Socket> kickQueue = new HashSet<>();
   private final int maxConnections;
   private final ServerStats stats;

   @Deprecated
   public BasicSocketServer(int protocolVersion, int port, int backlog, int maxConnections, Object __, SocketServerConfig config) {
      this(protocolVersion, port, backlog, maxConnections, config);
   }

   public BasicSocketServer(int protocolVersion, int port, int backlog, int maxConnections, SocketServerConfig config) {
      if ((this.eventManager = new EventManager()).callEvent(new ServerStartEvent(EventState.PRE, System.currentTimeMillis()))) {
         throw new EventCancelledException("Server startup aborted");
      } else {
         if (config.isUseSSL()) {
            File keyStoreFile = new File(config.getPathToKeyStore());
            if (!keyStoreFile.exists()) {
               throw new RuntimeException("KeyStore file does not exist: " + keyStoreFile.getAbsolutePath());
            }

            Security.addProvider(new BouncyCastleProvider());
            System.setProperty("javax.net.ssl.keyStore", keyStoreFile.getAbsolutePath());
            System.setProperty("javax.net.ssl.keyStorePassword", config.getKeyStorePassword());
         }

         this.protocolVersion = protocolVersion;
         this.port = port;
         this.maxConnections = maxConnections;
         this.config = config;

         try {
            this.serverSocket = config.isUseSSL()
               ? SSLServerSocketFactory.getDefault().createServerSocket()
               : ServerSocketFactory.getDefault().createServerSocket();
            this.stats = new ServerStats(System.currentTimeMillis());
            this.protocolManager = new ProtocolManager(this, protocolVersion, this.serverSocket, this.stats, this.eventManager, config);
            if (config.isUseSSL()) {
               List<String> ciphers = new ArrayList<>(
                  Arrays.asList(
                     "TLS_RSA_WITH_AES_128_CBC_SHA256",
                     "TLS_RSA_WITH_AES_256_CBC_SHA256",
                     "TLS_DH_RSA_WITH_AES_128_CBC_SHA256",
                     "TLS_DH_RSA_WITH_AES_256_CBC_SHA256",
                     "TLS_RSA_WITH_AES_128_GCM_SHA256",
                     "TLS_RSA_WITH_AES_256_GCM_SHA384",
                     "TLS_DH_RSA_WITH_AES_128_GCM_SHA256",
                     "TLS_DH_RSA_WITH_AES_256_GCM_SHA384",
                     "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                     "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
                     "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256",
                     "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384",
                     "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                     "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
                     "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256",
                     "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384",
                     "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                     "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                     "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256",
                     "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384",
                     "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                     "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
                     "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256",
                     "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384",
                     "TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                     "TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256",
                     "TLS_AES_128_CCM_SHA256",
                     "TLS_AES_128_GCM_SHA256",
                     "TLS_AES_256_GCM_SHA384",
                     "TLS_DHE_RSA_WITH_CHACHA20_POLY1305_SHA256",
                     "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
                     "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
                     "TLS_CHACHA20_POLY1305_SHA256"
                  )
               );
               ciphers.retainAll(Arrays.asList(((SSLServerSocketFactory)SSLServerSocketFactory.getDefault()).getSupportedCipherSuites()));
               ((SSLServerSocket)this.serverSocket).setEnabledCipherSuites(ciphers.toArray(new String[0]));
               ((SSLServerSocket)this.serverSocket).setEnabledProtocols(new String[]{"TLSv1.2"});
            }

            this.serverSocket.bind(new InetSocketAddress("0.0.0.0", this.port), backlog);
            new Thread(() -> {
               new Timer().schedule(new TimerTask() {
                  @Override
                  public void run() {
                     synchronized (BasicSocketServer.this.kickQueueLock) {
                        BasicSocketServer.this.kickQueue.forEach(x$0 -> BasicSocketServer.this.kick0(x$0));
                        BasicSocketServer.this.kickQueue.clear();
                     }
                  }
               }, KICK_FREQ, KICK_FREQ);
               this.eventManager.callEvent(new ServerStartEvent(EventState.POST, System.currentTimeMillis()));

               try {
                  while (!this.serverSocket.isClosed()) {
                     try {
                        if (this.socketListeners.keySet().size() < maxConnections) {
                           Socket client = this.serverSocket.accept();
                           SocketListener listener = new SocketListener(this, client, this.protocolManager, this.stats, this.eventManager, config);
                           this.socketListeners.put(client, listener);
                           listener.start();
                        }
                     } catch (Exception ex) {
                        if (!(ex instanceof EventCancelledException)) {
                           System.err.println("[EasySockets] Error accepting new socket connection");
                           ex.printStackTrace();
                        }
                     }
                  }
               } catch (Exception e) {
                  throw new RuntimeException("Exception starting packet processing thread", e);
               }

               this.shutdown();
            }, "Packet Processing Thread").start();
         } catch (Exception cause) {
            throw new RuntimeException("Failed to start a BasicSocketServer", cause);
         }
      }
   }

   private void kick0(Socket s) {
      SocketListener listener = this.socketListeners.get(s);
      if (listener != null) {
         this.socketListeners.remove(s);
      }
   }

   void addToKickQueue(Socket s) {
      synchronized (this.kickQueue) {
         this.kickQueue.add(s);
      }
   }

   public void kick(Socket s) {
      SocketListener listener = this.socketListeners.get(s);
      if (listener != null) {
         this.socketListeners.remove(s);
         listener.endQuietly();
      }
   }

   public void shutdown() {
      try {
         if (!this.serverSocket.isClosed()) {
            this.serverSocket.close();
         }
      } catch (Exception ignored) {
      }

      this.eventManager.callEvent(new ServerStopEvent(EventState.POST, System.currentTimeMillis()));
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public int getPort() {
      return this.port;
   }

   public ProtocolManager getProtocolManager() {
      return this.protocolManager;
   }

   public EventManager getEventManager() {
      return this.eventManager;
   }

   public SocketServerConfig getConfig() {
      return this.config;
   }

   Map<Socket, SocketListener> getSocketListeners() {
      return this.socketListeners;
   }

   public Set<Socket> getKickQueue() {
      return this.kickQueue;
   }

   public int getMaxConnections() {
      return this.maxConnections;
   }

   public ServerStats getStats() {
      return this.stats;
   }
}
