package org.bstats.pie;

import org.bstats.Metrics;
import org.json.simple.JSONObject;

public abstract class SimplePie extends Metrics.CustomChart {
   public SimplePie(String chartId) {
      super(chartId);
   }

   public abstract String getValue();

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      String value = this.getValue();
      if (value != null && !value.isEmpty()) {
         data.put("value", value);
         return data;
      } else {
         return null;
      }
   }
}
