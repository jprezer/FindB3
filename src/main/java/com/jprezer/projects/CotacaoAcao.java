package com.jprezer.projects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class PrecoAcao {
      String data;
      double preco;

      public PrecoAcao(String data, double preco) {
            this.data = data;
            this.preco = preco;
      }
}

public class CotacaoAcao {

      public static void main(String[] args) throws Exception {
            Scanner sc = new Scanner(System.in);
            System.out.print("Digite o código da ação (ex: BBAS3): ");
            String ticker = sc.nextLine().toUpperCase();

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

            List<PrecoAcao> lista = new ArrayList<>();

            for (int i = 0; i < historical.length(); i++) {
                  JSONObject dia = historical.getJSONObject(i);
                  double close = dia.optDouble("close", 0);
                  long timestamp = dia.getLong("date");
                  LocalDate data = Instant.ofEpochSecond(timestamp)
                          .atZone(ZoneId.systemDefault())
                          .toLocalDate();

                  lista.add(new PrecoAcao(data.toString(), close));
            }

            // Salva dados em JSON
            try (FileWriter writer = new FileWriter("dados.json")) {
                  writer.write(new JSONArray(lista).toString(4));
            }

            // Gera HTML para Plotly automaticamente
            String html = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
        </head>
        <body>
            <div id="grafico" style="width:100%%;height:500px;"></div>
            <script>
                fetch('dados.json')
                    .then(response => response.json())
                    .then(data => {
                        const trace = {
                            x: data.map(d => d.data),
                            y: data.map(d => d.preco),
                            mode: 'lines+markers',
                            type: 'scatter',
                            marker: { size: 8 }
                        };
                        const layout = {
                            title: 'Histórico de %s',
                            xaxis: { title: 'Data' },
                            yaxis: { title: 'Preço (R$)' }
                        };
                        Plotly.newPlot('grafico', [trace], layout);
                    });
            </script>
        </body>
        </html>
        """, ticker);


            String htmlFileName = "grafico.html";
            try (FileWriter writer = new FileWriter(htmlFileName)) {
                  writer.write(html);
            }

            System.out.println("Arquivos gerados com sucesso!");
            System.out.println("Abrindo o gráfico interativo no navegador...");

            // Abre o HTML automaticamente no navegador padrão
            if (Desktop.isDesktopSupported()) {
                  Desktop.getDesktop().browse(new File(htmlFileName).toURI());
            } else {
                  System.out.println("Não foi possível abrir o navegador automaticamente. Abra 'grafico.html' manualmente.");
            }
      }
}
