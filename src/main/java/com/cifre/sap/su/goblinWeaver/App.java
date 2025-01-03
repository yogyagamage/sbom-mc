package com.cifre.sap.su.goblinWeaver;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class App
{
    private static final String API_URL = "http://localhost:8181";
    private static final int nodesMaxId = 15118235;
    private static final int batchSize = 50000;

    public static void main( String[] args )
    {
        int startId = 0;
        for (int currentStart = startId; currentStart <= nodesMaxId; currentStart += batchSize) {
            int currentEnd = currentStart + batchSize - 1;
            String cypherQuery = String.format(
                    "MATCH (n:Release) " +
                            "WHERE id(n) >= %d AND id(n) <= %d " +
                            "RETURN n;",
                    currentStart, currentEnd
            );
            System.out.println(currentEnd + "/" + nodesMaxId);
            cypherQuery(cypherQuery, List.of("SBOM"));
        }
    }

    public static void cypherQuery(String query, List<String> addedValues){
        String apiRoute = "/cypher";
        JSONObject bodyJsonObject = new JSONObject();
        bodyJsonObject.put("query", query);
        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(addedValues);
        bodyJsonObject.put("addedValues", jsonArray);
        executeQuery(bodyJsonObject, apiRoute);
    }

    private static void executeQuery(JSONObject bodyJsonObject, String apiRoute){
        try {
            URL url = new URL(API_URL+apiRoute);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json; utf-8");
            http.setRequestProperty("Accept", "application/json");
            http.setDoOutput(true);

            byte[] out = bodyJsonObject.toString().getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            if(http.getResponseCode() != 200){
                System.out.println("Error with query: \n "+bodyJsonObject+"  "+url);
            }
            http.disconnect();
        } catch (IOException e) {
            System.out.println("Unable to connect to API:\n" + e);
        }
    }
}