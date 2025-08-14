package com.jprezer.projects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;
import java.util.Scanner;

public class CotacaoAcao {

      public static void main(String[] args) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Digite o código da ação (ex: BBAS3): ");
            String ticker = br.readLine().toUpperCase();

            String token = "r4nczfsvgs8wpywBqaBtHM";

            // URL da Brapi
            String urlStr = "https://brapi.dev/api/quote/" + ticker + "?range=1mo&interval=7d";

            // Conexão HTTP
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestMethod("GET");

            // Lê resposta
            StringBuilder jsonText = new StringBuilder();
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                  while (scanner.hasNextLine()) {
                        jsonText.append(scanner.nextLine());
                  }
            }

            JSONObject json = new JSONObject(jsonText.toString());
            JSONArray historical = json.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONArray("historicalDataPrice");

            // Encontrar maior valor para escalar gráfico
            double maxClose = 0;
            for (int i = 0; i < historical.length(); i++) {
                  double close = historical.getJSONObject(i).optDouble("close", 0);
                  if (close > maxClose) maxClose = close;
            }

            // Exibir valores e gráfico
            System.out.println("Histórico de " + ticker + ":");
            for (int i = 0; i < historical.length(); i++) {
                  JSONObject dia = historical.getJSONObject(i);

                  long timestamp = dia.getLong("date");
                  double close = dia.optDouble("close", 0);

                  // Converte timestamp para data legível
                  LocalDate data = Instant.ofEpochSecond(timestamp)
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();

                  // Escala gráfica simples
                  int barras = (int) ((close / maxClose) * 50); // 50 colunas
                  String grafico = "█".repeat(barras);

                  System.out.printf("%s | R$ %.2f %s%n",
                          data.toString(),
                          close,
                          grafico);
            }
      }
}
