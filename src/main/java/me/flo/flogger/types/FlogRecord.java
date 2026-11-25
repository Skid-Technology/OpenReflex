package me.flo.flogger.types;

import java.beans.ConstructorProperties;
import me.flo.flogger.Flogger4J;

public class FlogRecord {
   private final Flogger4J flogger;
   private FlogLevel level;
   private String message;
   private final long timestamp;

   @ConstructorProperties({"flogger", "level", "message", "timestamp"})
   public FlogRecord(Flogger4J flogger, FlogLevel level, String message, long timestamp) {
      this.flogger = flogger;
      this.level = level;
      this.message = message;
      this.timestamp = timestamp;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof FlogRecord)) {
         return false;
      } else {
         FlogRecord other = (FlogRecord)o;
         if (!other.canEqual(this)) {
            return false;
         } else {
            Object this$flogger = this.getFlogger();
            Object other$flogger = other.getFlogger();
            if (this$flogger == null ? other$flogger == null : this$flogger.equals(other$flogger)) {
               Object this$level = this.getLevel();
               Object other$level = other.getLevel();
               if (this$level == null ? other$level == null : this$level.equals(other$level)) {
                  Object this$message = this.getMessage();
                  Object other$message = other.getMessage();
                  return (this$message == null ? other$message == null : this$message.equals(other$message))
                     ? this.getTimestamp() == other.getTimestamp()
                     : false;
               } else {
                  return false;
               }
            } else {
               return false;
            }
         }
      }
   }

   protected boolean canEqual(Object other) {
      return other instanceof FlogRecord;
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $flogger = this.getFlogger();
      result = result * 59 + ($flogger == null ? 43 : $flogger.hashCode());
      Object $level = this.getLevel();
      result = result * 59 + ($level == null ? 43 : $level.hashCode());
      Object $message = this.getMessage();
      result = result * 59 + ($message == null ? 43 : $message.hashCode());
      long $timestamp = this.getTimestamp();
      return result * 59 + (int)($timestamp >>> 32 ^ $timestamp);
   }

   public Flogger4J getFlogger() {
      return this.flogger;
   }

   public void setLevel(FlogLevel level) {
      this.level = level;
   }

   public FlogLevel getLevel() {
      return this.level;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getMessage() {
      return this.message;
   }

   public long getTimestamp() {
      return this.timestamp;
   }
}
