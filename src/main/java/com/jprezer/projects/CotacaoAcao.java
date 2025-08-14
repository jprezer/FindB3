package com.jprezer.projects;

import org.knowm.xchart.*;
import org.knowm.xchart.style.markers.None;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CotacaoAcao {

      public static void main(String[] args) throws Exception {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Digite o código da ação (ex: BBAS3): ");
            String ticker = br.readLine().toUpperCase();

            String token = "r4nczfsvgs8wpywBqaBtHM";
            String urlStr = "https://brapi.dev/api/quote/" + ticker + "?range=1mo&interval=1d";

            // Conexão HTTP
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestMethod("GET");

            // Lê resposta
            StringBuilder jsonText = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                  String line;
                  while ((line = reader.readLine()) != null) jsonText.append(line);
            }

            JSONObject json = new JSONObject(jsonText.toString());
            JSONArray historical = json.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONArray("historicalDataPrice");

            List<Double> precos = new ArrayList<>();
            List<LocalDate> datas = new ArrayList<>();

            for (int i = 0; i < historical.length(); i++) {
                  JSONObject dia = historical.getJSONObject(i);
                  double close = dia.optDouble("close", 0);
                  long timestamp = dia.getLong("date");
                  LocalDate data = Instant.ofEpochSecond(timestamp)
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();

                  precos.add(close);
                  datas.add(data);
            }

            // Cria o gráfico
            XYChart chart = new XYChartBuilder()
                    .width(800)
                    .height(600)
                    .title("Histórico de " + ticker)
                    .xAxisTitle("Data")
                    .yAxisTitle("Preço (R$)")
                    .build();

            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setXAxisLabelRotation(45);
            chart.getStyler().setMarkerSize(6);

            // Converte datas para String para o XChart
            List<Date> datasDate = new ArrayList<>();
            for (LocalDate d : datas) {
                  datasDate.add(java.util.Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }

            chart.addSeries("Preço", datasDate, precos).setMarker(null);


            // Mostra em uma janela
            SwingWrapper<XYChart> sw = new SwingWrapper<>(chart);
            sw.displayChart();
      }
}
