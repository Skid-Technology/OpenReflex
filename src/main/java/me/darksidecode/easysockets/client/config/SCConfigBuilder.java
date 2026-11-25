package me.darksidecode.easysockets.client.config;

import java.io.InputStream;

public class SCConfigBuilder {
   private final SocketClientConfig config = new SocketClientConfig(30000, 65536, 50, 0, false, null, null);

   public SCConfigBuilder timeout(int timeout) {
      this.config.setTimeout(timeout);
      return this;
   }

   public SCConfigBuilder maxPacketSize(int maxPacketSize) {
      this.config.setMaxPacketSize(maxPacketSize);
      return this;
   }

   public SCConfigBuilder sendRateLimit(int sendRateLimit) {
      this.config.setSendRateLimit(sendRateLimit);
      return this;
   }

   public SCConfigBuilder receiveRateLimit(int receive) {
      this.config.setReceiveRateLimit(receive);
      return this;
   }

   public SCConfigBuilder useSSL(boolean use) {
      this.config.setUseSSL(use);
      return this;
   }

   public SCConfigBuilder trustStoreStream(InputStream trustStoreStream) {
      this.config.setTrustStoreStream(trustStoreStream);
      return this;
   }

   public SCConfigBuilder trustStorePassword(String trustStorePassword) {
      this.config.setTrustStorePassword(trustStorePassword);
      return this;
   }

   public SocketClientConfig build() {
      return this.config;
   }

   public static SocketClientConfig def() {
      return new SCConfigBuilder().build();
   }
}
