package me.darksidecode.easysockets.server;

import java.io.Serializable;

public class ServerStats implements Serializable {
   private static final long serialVersionUID = 2770526316580474952L;
   public final long startTime;
   public volatile long bytesSent;
   public volatile long bytesReceived;
   public volatile long connectionsRegistered;

   public ServerStats(long startTime) {
      this.startTime = startTime;
   }
}
