package org.bstats.charts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bstats.Metrics;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class AdvancedBarChart extends Metrics.CustomChart {
   public AdvancedBarChart(String chartId) {
      super(chartId);
   }

   public abstract HashMap<String, int[]> getValues(HashMap<String, int[]> valueMap);

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      JSONObject values = new JSONObject();
      HashMap<String, int[]> map = this.getValues(new HashMap<>());
      if (map != null && !map.isEmpty()) {
         boolean allSkipped = true;

         for (Entry<String, int[]> entry : map.entrySet()) {
            if (entry.getValue().length != 0) {
               allSkipped = false;
               JSONArray categoryValues = new JSONArray();
               categoryValues.addAll(Arrays.asList(entry.getValue()));
               values.put(entry.getKey(), categoryValues);
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
