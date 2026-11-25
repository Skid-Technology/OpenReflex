package me.darksidecode.kantanj.ipapi;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import me.darksidecode.kantanj.formatting.CommonJson;
import me.darksidecode.kantanj.networking.GetHttpRequest;
import me.darksidecode.kantanj.networking.Networking;
import me.darksidecode.kantanj.system.Threads;
import me.darksidecode.kantanj.time.ExpiringList;
import me.darksidecode.kantanj.types.Check;
import org.apache.http.entity.ContentType;

public class IPAPI {
   public static final String BASE_URL = "http://ip-api.com";
   private static final int MAX_REQUESTS_PER_MIN = 145;
   private static final long MIN_BLOCK_MILLIS = 1000L;
   private static boolean enableCaching = true;
   private static boolean blockOnLimitExcess = true;
   private static long threadBlockMillis = TimeUnit.SECONDS.toMillis(30L);
   private static final ExpiringList requestCounter = new ExpiringList(1L, TimeUnit.MINUTES);
   private static final Cache<Integer, IPAddress> cache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS).build();
   private static final Object cacheLock = new Object();
   private static final Object requestLock = new Object();

   public static void setThreadBlockTime(long blockTime, TimeUnit timeUnit) {
      if (blockTime < 1L) {
         throw new IllegalArgumentException("blockTime cannot be negative or zero");
      } else {
         long millis = Check.notNull(timeUnit, "timeUnit cannot be null").toMillis(blockTime);
         if (millis < 1000L) {
            throw new IllegalArgumentException("cannot block for less than 1 second");
         } else {
            threadBlockMillis = millis;
         }
      }
   }

   public static IPAddress info(InetAddress address) {
      return info(address.getHostAddress());
   }

   public static IPAddress info(InetAddress address, IPAPILanguage lang) {
      return info(address.getHostAddress(), lang);
   }

   public static IPAddress info(String ip) {
      return info(ip, IPAPILanguage.ENGLISH);
   }

   public static IPAddress info(String ip, IPAPILanguage lang) {
      if (enableCaching) {
         IPAPI.IPAPIRequest ipapiRequest = new IPAPI.IPAPIRequest(ip, lang);
         int hash = ipapiRequest.hashCode();
         synchronized (cacheLock) {
            IPAddress cached = (IPAddress)cache.getIfPresent(hash);
            if (cached != null) {
               return cached;
            } else {
               IPAddress ipAddr = info0(ip, lang);
               cache.put(hash, ipAddr);
               return ipAddr;
            }
         }
      } else {
         return info0(ip, lang);
      }
   }

   private static IPAddress info0(String ip, IPAPILanguage lang) {
      synchronized (requestLock) {
         int requestsLastMin = requestCounter.updateAndCount();
         if (requestsLastMin > 145 && blockOnLimitExcess) {
            Threads.sleepQuietly(threadBlockMillis);
         }

         GetHttpRequest request = (GetHttpRequest)new GetHttpRequest()
            .baseUrl("http://ip-api.com")
            .path("json/" + ip)
            .queryParam("lang", lang.getIso639Name())
            .requestProperty("Content-Type", ContentType.APPLICATION_JSON.toString())
            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101");
         String response = Networking.Http.get(request);
         return CommonJson.fromJson(response, IPAddress.class);
      }
   }

   public static boolean isEnableCaching() {
      return enableCaching;
   }

   public static void setEnableCaching(boolean enableCaching) {
      IPAPI.enableCaching = enableCaching;
   }

   public static boolean isBlockOnLimitExcess() {
      return blockOnLimitExcess;
   }

   public static void setBlockOnLimitExcess(boolean blockOnLimitExcess) {
      IPAPI.blockOnLimitExcess = blockOnLimitExcess;
   }

   private static class IPAPIRequest {
      private final String ip;
      private final IPAPILanguage lang;

      @Override
      public int hashCode() {
         return this.ip.hashCode() + this.lang.name().hashCode();
      }

      public String getIp() {
         return this.ip;
      }

      public IPAPILanguage getLang() {
         return this.lang;
      }

      public IPAPIRequest(String ip, IPAPILanguage lang) {
         this.ip = ip;
         this.lang = lang;
      }
   }
}
