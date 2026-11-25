package me.darksidecode.easysockets.client.config;

import java.io.InputStream;

public class SocketClientConfig {
   private int timeout;
   private int maxPacketSize;
   private int sendRateLimit;
   private int receiveRateLimit;
   private boolean useSSL;
   private InputStream trustStoreStream;
   private String trustStorePassword;

   public SocketClientConfig(
      int timeout, int maxPacketSize, int sendRateLimit, int receiveRateLimit, boolean useSSL, InputStream trustStoreStream, String trustStorePassword
   ) {
      this.timeout = timeout;
      this.maxPacketSize = maxPacketSize;
      this.sendRateLimit = sendRateLimit;
      this.receiveRateLimit = receiveRateLimit;
      this.useSSL = useSSL;
      this.trustStoreStream = trustStoreStream;
      this.trustStorePassword = trustStorePassword;
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

   public InputStream getTrustStoreStream() {
      return this.trustStoreStream;
   }

   public String getTrustStorePassword() {
      return this.trustStorePassword;
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

   public void setTrustStoreStream(InputStream trustStoreStream) {
      this.trustStoreStream = trustStoreStream;
   }

   public void setTrustStorePassword(String trustStorePassword) {
      this.trustStorePassword = trustStorePassword;
   }
}
