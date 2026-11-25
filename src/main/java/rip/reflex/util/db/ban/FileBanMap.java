package rip.reflex.util.db.ban;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileBanMap {
   private Map<UUID, FileBanMap.InternalBanInfo> map;

   public Map<UUID, FileBanMap.InternalBanInfo> setMap() {
      return this.map = new HashMap<UUID, FileBanMap.InternalBanInfo>() {
         public FileBanMap.InternalBanInfo put(UUID key, FileBanMap.InternalBanInfo value) {
            return super.put(key, value);
         }
      };
   }

   public Map<UUID, FileBanMap.InternalBanInfo> getMap() {
      if (this.map == null) {
         this.setMap();
      }

      return this.map;
   }

   public FileBanMap(Map<UUID, FileBanMap.InternalBanInfo> map) {
      this.map = map;
   }

   public FileBanMap() {
   }

   static class InternalBanInfo implements Serializable {
      private final long endOfBan;
      private final long banStart;
      private final String message;

      public boolean shallBeUnbanned() {
         return this.endOfBan < System.currentTimeMillis() && this.endOfBan != -1L;
      }

      public InternalBanInfo(long endOfBan, long banStart, String message) {
         this.endOfBan = endOfBan;
         this.banStart = banStart;
         this.message = message;
      }

      public long getEndOfBan() {
         return this.endOfBan;
      }

      public long getBanStart() {
         return this.banStart;
      }

      public String getMessage() {
         return this.message;
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof FileBanMap.InternalBanInfo)) {
            return false;
         } else {
            FileBanMap.InternalBanInfo other = (FileBanMap.InternalBanInfo)o;
            if (!other.canEqual(this)) {
               return false;
            } else if (this.getEndOfBan() != other.getEndOfBan()) {
               return false;
            } else if (this.getBanStart() != other.getBanStart()) {
               return false;
            } else {
               Object this$message = this.getMessage();
               Object other$message = other.getMessage();
               return this$message == null ? other$message == null : this$message.equals(other$message);
            }
         }
      }

      protected boolean canEqual(Object other) {
         return other instanceof FileBanMap.InternalBanInfo;
      }

      @Override
      public int hashCode() {
         int PRIME = 59;
         int result = 1;
         long $endOfBan = this.getEndOfBan();
         result = result * 59 + (int)($endOfBan >>> 32 ^ $endOfBan);
         long $banStart = this.getBanStart();
         result = result * 59 + (int)($banStart >>> 32 ^ $banStart);
         Object $message = this.getMessage();
         return result * 59 + ($message == null ? 43 : $message.hashCode());
      }

      @Override
      public String toString() {
         return "FileBanMap.InternalBanInfo(endOfBan=" + this.getEndOfBan() + ", banStart=" + this.getBanStart() + ", message=" + this.getMessage() + ")";
      }
   }
}
