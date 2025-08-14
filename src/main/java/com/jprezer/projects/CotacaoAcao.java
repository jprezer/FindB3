package com.jprezer.projects;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class CotacaoAcao {

      public static void main(String[] args) {
            try {
                  BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                  System.out.print("Digite o código da ação (ex: BBAS3): ");
                  String ticker = br.readLine().toUpperCase();

                  // Coloque seu token aqui
                  String token = "r4nczfsvgs8wpywBqaBtHM";
                  String urlString = "https://brapi.dev/api/quote/" + ticker;

                  URL url = new URL(urlString);
                  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                  conn.setRequestMethod("GET");
                  conn.setRequestProperty("Authorization", "Bearer " + token);

                  BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                  StringBuilder content = new StringBuilder();
                  String line;
                  while ((line = in.readLine()) != null) content.append(line);
                  in.close();
                  conn.disconnect();

                  JSONObject json = new JSONObject(content.toString());
                  JSONArray results = json.getJSONArray("results");
                  if (results.length() == 0) {
                        System.out.println("Ticker não encontrado.");
                        return;
                  }

                  JSONObject acao = results.getJSONObject(0);
                  double preco = acao.getDouble("regularMarketPrice"); // preço atual
                  System.out.println("Preço atual de " + ticker + ": R$ " + preco);

            } catch (Exception e) {
                  System.out.println("Erro ao buscar cotação: " + e.getMessage());
            }
      }
}

