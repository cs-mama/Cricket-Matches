package com.crickbuzz.cricketmatches;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class CricketAnalysis {

    private static final String API_URL = "https://api.cuvora.com/car/partner/cricket-data";
    private static final String API_KEY = "test-creds@2320";
    private static final String API_KEY_HEADER = "apiKey";

    public String fetchMatchData() throws Exception {
        try {
            URI uri = new URI(API_URL);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty(API_KEY_HEADER, API_KEY);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed to fetch data: HTTP error code : " + responseCode);
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
            }

            connection.disconnect();
            return content.toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URI syntax: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching match data: " + e.getMessage());
        }
    }

    public void processMatchData(String json_Res) {
        JSONObject json_Obj = new JSONObject(json_Res);
        JSONArray match_Array = json_Obj.optJSONArray("data");
        if (match_Array == null) {
            System.out.println("❌ Key 'data' not found or not an array in the JSON response.");
            return;
        }

        Map<String, Integer> high_Score_Map = new HashMap<>();
        int match_Cnt_300P = 0;

        for (int i = 0; i < match_Array.length(); i++) {
            JSONObject match_ob = match_Array.optJSONObject(i);
            if (match_ob == null) {
                System.out.println("❌ Match data at index " + i + " is null or not a valid JSONObject.");
                continue;
            }

            String t_1 = match_ob.optString("t1", "Unknown Team 1");
            String t_2 = match_ob.optString("t2", "Unknown Team 2");
            int score_T1 = getScore(match_ob.optString("t1s", "0/0"));
            int score_T2 = getScore(match_ob.optString("t2s", "0/0"));

            updateHighestScore(high_Score_Map, t_1, score_T1);
            updateHighestScore(high_Score_Map, t_2, score_T2);

            if (score_T1 + score_T2 > 300) {
                match_Cnt_300P++;
            }
        }

        Map.Entry<String, Integer> highest_Score_Entry = high_Score_Map.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        String highest_Score = highest_Score_Entry != null ? String.valueOf(highest_Score_Entry.getValue()) : "0";
        String team_Name = highest_Score_Entry != null ? highest_Score_Entry.getKey() : "No Team";
        String number_Of_Matches = String.valueOf(match_Cnt_300P);

        String highest_Score_Print = "Highest Score: \"" + highest_Score + "\" and Team Name is: \"" + team_Name + "\"";
        String number_Of_Matches_Print = "Number Of Matches with total 300 Plus Score: \"" + number_Of_Matches + "\"";

        System.out.println(highest_Score_Print);
        System.out.println(number_Of_Matches_Print);
    }

    private void updateHighestScore(Map<String, Integer> high_Score_Map, String team, int score) {
        high_Score_Map.put(team, Math.max(high_Score_Map.getOrDefault(team, 0), score));
    }

    private int getScore(String score) {
        try {
            if (score.contains("/")) {
                return Integer.parseInt(score.split("/")[0]);
            }
            return Integer.parseInt(score);
        } catch (NumberFormatException e) {
            System.out.println("❌ Error parsing score: " + score);
            return 0;
        }
    }
}
