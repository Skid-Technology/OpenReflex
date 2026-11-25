package me.darksidecode.easysockets.server.config;

public class SocketServerConfig {
   private int timeout;
   private int maxPacketSize;
   private int sendRateLimit;
   private int receiveRateLimit;
   private boolean useSSL;
   private String pathToKeyStore;
   private String keyStorePassword;

   public SocketServerConfig(
      int timeout, int maxPacketSize, int sendRateLimit, int receiveRateLimit, boolean useSSL, String pathToKeyStore, String keyStorePassword
   ) {
      this.timeout = timeout;
      this.maxPacketSize = maxPacketSize;
      this.sendRateLimit = sendRateLimit;
      this.receiveRateLimit = receiveRateLimit;
      this.useSSL = useSSL;
      this.pathToKeyStore = pathToKeyStore;
      this.keyStorePassword = keyStorePassword;
   }

   public int getTimeout() {
      return this.timeout;
   }

   public int getMaxPacketSize() {
      return this.maxPacketSize;
   }

   public int getSendRateLimit() {
      return this.sendRateLimit;
   }

   public int getReceiveRateLimit() {
      return this.receiveRateLimit;
   }

   public boolean isUseSSL() {
      return this.useSSL;
   }

   public String getPathToKeyStore() {
      return this.pathToKeyStore;
   }

   public String getKeyStorePassword() {
      return this.keyStorePassword;
   }

   public void setTimeout(int timeout) {
      this.timeout = timeout;
   }

   public void setMaxPacketSize(int maxPacketSize) {
      this.maxPacketSize = maxPacketSize;
   }

   public void setSendRateLimit(int sendRateLimit) {
      this.sendRateLimit = sendRateLimit;
   }

   public void setReceiveRateLimit(int receiveRateLimit) {
      this.receiveRateLimit = receiveRateLimit;
   }

   public void setUseSSL(boolean useSSL) {
      this.useSSL = useSSL;
   }

   public void setPathToKeyStore(String pathToKeyStore) {
      this.pathToKeyStore = pathToKeyStore;
   }

   public void setKeyStorePassword(String keyStorePassword) {
      this.keyStorePassword = keyStorePassword;
   }
}
