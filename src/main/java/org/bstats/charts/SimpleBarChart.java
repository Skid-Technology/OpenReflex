package org.bstats.charts;

import java.util.HashMap;
import java.util.Map.Entry;
import org.bstats.Metrics;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class SimpleBarChart extends Metrics.CustomChart {
   public SimpleBarChart(String chartId) {
      super(chartId);
   }

   public abstract HashMap<String, Integer> getValues(HashMap<String, Integer> valueMap);

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      JSONObject values = new JSONObject();
      HashMap<String, Integer> map = this.getValues(new HashMap<>());
      if (map != null && !map.isEmpty()) {
         for (Entry<String, Integer> entry : map.entrySet()) {
            JSONArray categoryValues = new JSONArray();
            categoryValues.add(entry.getValue());
            values.put(entry.getKey(), categoryValues);
         }

         data.put("values", values);
         return data;
      } else {
         return null;
      }
   }
}
