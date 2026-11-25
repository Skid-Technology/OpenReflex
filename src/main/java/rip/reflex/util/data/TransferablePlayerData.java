package rip.reflex.util.data;

import java.io.Serializable;

class TransferablePlayerData implements Serializable {
   private static final long serialVersionUID = -7187571944591175938L;
   private final TransferablePlayerData.InnerObj violations;
   private final TransferablePlayerData.InnerObj heuristicsData;

   TransferablePlayerData(TransferablePlayerData.InnerObj violations, TransferablePlayerData.InnerObj heuristicsData) {
      this.violations = violations;
      this.heuristicsData = heuristicsData;
   }

   TransferablePlayerData.InnerObj getViolations() {
      return this.violations;
   }

   TransferablePlayerData.InnerObj getHeuristicsData() {
      return this.heuristicsData;
   }

   static class InnerObj implements Serializable {
      private static final long serialVersionUID = 2597219357509132296L;
      private final Object inner;

      InnerObj(Object inner) {
         this.inner = inner;
      }

      Object getInner() {
         return this.inner;
      }
   }
}
