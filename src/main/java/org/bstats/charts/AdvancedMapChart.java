package org.bstats.charts;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bstats.Country;
import org.bstats.Metrics;
import org.json.simple.JSONObject;

public abstract class AdvancedMapChart extends Metrics.CustomChart {
   public AdvancedMapChart(String chartId) {
      super(chartId);
   }

   public abstract HashMap<Country, Integer> getValues(HashMap<Country, Integer> valueMap);

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      JSONObject values = new JSONObject();
      HashMap<Country, Integer> map = this.getValues(new HashMap<>());
      if (map != null && !map.isEmpty()) {
         boolean allSkipped = true;

         for (Entry<Country, Integer> entry : map.entrySet()) {
            if (entry.getValue() != 0) {
               allSkipped = false;
               values.put(entry.getKey().getCountryIsoTag(), entry.getValue());
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
