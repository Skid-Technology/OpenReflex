package rip.reflex.api;

public class BanInfo {
   public static final BanInfo NOT_BANNED = new BanInfo(null, 0L, -1L);
   private final String message;
   private final long banTime;
   private final long banStart;

   public BanInfo(String message, long banTime, long banStart) {
      this.message = message;
      this.banTime = banTime;
      this.banStart = banStart;
   }

   public String getMessage() {
      return this.message;
   }

   public long getBanTime() {
      return this.banTime;
   }
}
