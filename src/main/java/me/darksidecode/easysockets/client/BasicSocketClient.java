package me.darksidecode.easysockets.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import me.darksidecode.easysockets.client.config.SocketClientConfig;
import me.darksidecode.easysockets.event.EventCancelledException;
import me.darksidecode.easysockets.event.EventManager;
import me.darksidecode.easysockets.event.EventState;
import me.darksidecode.easysockets.event.client.ClientStartEvent;
import me.darksidecode.easysockets.event.client.ClientStopEvent;
import me.darksidecode.easysockets.event.client.ClientTimeoutEvent;
import me.darksidecode.easysockets.protocol.packet.Packet;
import me.darksidecode.easysockets.util.io.Compressor;

public class BasicSocketClient {
   private final int protocolVersion;
   private final String host;
   private final int port;
   private final Socket socket;
   private final DataInputStream input;
   private final DataOutputStream output;
   private final ProtocolManager protocolManager;
   private final EventManager eventManager;
   private final SocketClientConfig config;
   private SSLContext sslContext;
   private boolean stopped;

   @Deprecated
   public BasicSocketClient(int protocolVersion, String host, int port, Object __, SocketClientConfig config) {
      this(protocolVersion, host, port, config);
   }

   public BasicSocketClient(int protocolVersion, String host, int port, SocketClientConfig config) {
      if (config.isUseSSL()) {
         if (config.getTrustStoreStream() == null) {
            throw new RuntimeException("TrustStore stream is null");
         }

         try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore trustStore = KeyStore.getInstance("jks");
            trustStore.load(config.getTrustStoreStream(), config.getTrustStorePassword().toCharArray());
            factory.init(trustStore);
            this.sslContext = SSLContext.getInstance("TLSv1.2");
            this.sslContext.init(null, factory.getTrustManagers(), SecureRandom.getInstanceStrong());
         } catch (CertificateException | KeyStoreException | KeyManagementException | IOException | NoSuchAlgorithmException ex) {
            this.shutdown();
            throw new RuntimeException("failed to start a BasicSocketClient: SSL creation error", ex);
         }
      }

      try {
         if ((this.eventManager = new EventManager()).callEvent(new ClientStartEvent(EventState.PRE, System.currentTimeMillis()))) {
            throw new EventCancelledException("Client startup aborted");
         } else {
            this.protocolVersion = protocolVersion;
            this.host = host;
            this.port = port;
            this.config = config;
            this.socket = config.isUseSSL()
               ? this.sslContext.getSocketFactory().createSocket(host, port)
               : SocketFactory.getDefault().createSocket(host, port);
            this.socket.setSoTimeout(config.getTimeout());
            this.socket.setTcpNoDelay(true);
            this.socket.setKeepAlive(true);
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
               ciphers.retainAll(Arrays.asList(this.sslContext.getServerSocketFactory().getSupportedCipherSuites()));
               ((SSLSocket)this.socket).setEnabledCipherSuites(ciphers.toArray(new String[0]));
               ((SSLSocket)this.socket).setEnabledProtocols(new String[]{"TLSv1.2"});
            }

            this.input = new DataInputStream(Compressor.newInflateInput(this.socket.getInputStream()));
            this.output = new DataOutputStream(Compressor.newDeflateOutput(this.socket.getOutputStream()));
            this.protocolManager = new ProtocolManager(protocolVersion, this.socket, this.input, this.output, this.eventManager, config);
            new Thread(() -> {
               this.eventManager.callEvent(new ClientStartEvent(EventState.POST, System.currentTimeMillis()));

               try {
                  while (this.socket.isConnected() && !this.socket.isClosed() && !this.stopped) {
                     try {
                        int len = this.input.readInt();
                        if (len > 0 && len <= config.getMaxPacketSize()) {
                           byte[] bytes = new byte[len];
                           this.input.readFully(bytes, 0, len);
                           Packet packet = Packet.receivePacket(bytes);
                           this.protocolManager.receivePacket(packet);
                           this.protocolManager.getReceiveTracker().reset();
                        }
                     } catch (Exception ignored) {
                        this.eventManager.callEvent(new ClientTimeoutEvent(EventState.POST, System.currentTimeMillis()));
                        this.shutdown();
                     }
                  }
               } catch (Exception e) {
                  throw new RuntimeException("Exception in packet processing thread", e);
               } finally {
                  this.shutdown();
               }
            }, "Packet Processing Thread").start();
         }
      } catch (Exception err) {
         throw new RuntimeException("Failed to start a BasicSocketClient", err);
      }
   }

   public void shutdown() {
      this.stopped = true;

      try {
         if (this.socket.isConnected() && !this.socket.isClosed()) {
            this.socket.close();
         }

         this.input.close();
         this.output.close();
      } catch (Exception ignored) {
      }

      this.eventManager.callEvent(new ClientStopEvent(EventState.POST, System.currentTimeMillis()));
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public String getHost() {
      return this.host;
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

   public boolean isStopped() {
      return this.stopped;
   }
}
