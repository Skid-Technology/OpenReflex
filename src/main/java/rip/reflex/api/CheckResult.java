package rip.reflex.api;

import java.util.ArrayList;
import java.util.List;

public class CheckResult {
   private final List<String> tags;
   private int violationsMod;
   private boolean cancel;

   public CheckResult() {
      this.tags = new ArrayList<>();
   }

   public CheckResult(int violationsMod, boolean cancel, List<String> tags) {
      this.violationsMod = violationsMod;
      this.cancel = cancel;
      this.tags = tags;
   }

   public int getViolationsMod() {
      return this.violationsMod;
   }

   public List<String> getTags() {
      return this.tags;
   }

   public CheckResult setMod(int violationsMod) {
      this.violationsMod = violationsMod;
      return this;
   }

   public CheckResult addTag(String tag) {
      if (this.tags.contains(tag)) {
         return this;
      } else {
         this.tags.add(tag);
         return this;
      }
   }

   public CheckResult addTagIf(String tag, boolean cond) {
      return cond ? this.addTag(tag) : this;
   }

   public boolean isZero() {
      return this.violationsMod == 0;
   }

   public boolean hasTag(String tag) {
      return this.getTags().contains(tag);
   }

   public boolean isCheckFailed() {
      return this.violationsMod > 0;
   }

   public boolean isCheckPassed() {
      return this.violationsMod <= 0;
   }

   public boolean shouldCancel() {
      return this.cancel;
   }

   public CheckResult cancel() {
      return this.cancel(true);
   }

   public CheckResult dontCancel() {
      return this.cancel(false);
   }

   public CheckResult cancel(boolean cancel) {
      this.cancel = cancel;
      return this;
   }
}
