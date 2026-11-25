package org.bstats.charts;

import org.bstats.Metrics;
import org.json.simple.JSONObject;

public abstract class SingleLineChart extends Metrics.CustomChart {
   public SingleLineChart(String chartId) {
      super(chartId);
   }

   public abstract int getValue();

   @Override
   protected JSONObject getChartData() {
      JSONObject data = new JSONObject();
      int value = this.getValue();
      if (value == 0) {
         return null;
      } else {
         data.put("value", value);
         return data;
      }
   }
}
