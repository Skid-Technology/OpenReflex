package org.bstats.pie;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bstats.Metrics;
import org.json.simple.JSONObject;

public abstract class AdvancedPie extends Metrics.CustomChart {
   public AdvancedPie(String chartId) {
      super(chartId);
   }

   public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      JSONObject values = new JSONObject();
      HashMap<String, Integer> map = this.getValues(new HashMap<>());
      if (map != null && !map.isEmpty()) {
         boolean allSkipped = true;

         for (Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() != 0) {
               allSkipped = false;
               values.put(entry.getKey(), entry.getValue());
            }
         }

         if (allSkipped) {
            return null;
         } else {
            data.put("values", values);
            return data;
         }
      } else {
         return null;
      }
   }
}
