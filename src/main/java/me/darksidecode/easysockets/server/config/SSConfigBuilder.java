package me.darksidecode.easysockets.server.config;

public class SSConfigBuilder {
   private final SocketServerConfig config = new SocketServerConfig(30000, 65536, 50, 0, false, null, null);

   public SSConfigBuilder timeout(int timeout) {
      this.config.setTimeout(timeout);
      return this;
   }

   public SSConfigBuilder maxPacketSize(int maxPacketSize) {
      this.config.setMaxPacketSize(maxPacketSize);
      return this;
   }

   public SSConfigBuilder sendRateLimit(int sendRateLimit) {
      this.config.setSendRateLimit(sendRateLimit);
      return this;
   }

   public SSConfigBuilder receiveRateLimit(int receive) {
      this.config.setReceiveRateLimit(receive);
      return this;
   }

   public SSConfigBuilder useSSL(boolean use) {
      this.config.setUseSSL(use);
      return this;
   }

   public SSConfigBuilder pathToKeyStore(String pathToKeyStore) {
      this.config.setPathToKeyStore(pathToKeyStore);
      return this;
   }

   public SSConfigBuilder keyStorePassword(String keyStorePassword) {
      this.config.setKeyStorePassword(keyStorePassword);
      return this;
   }

   public SocketServerConfig build() {
      return this.config;
   }

   public static SocketServerConfig def() {
      return new SSConfigBuilder().build();
   }
}
