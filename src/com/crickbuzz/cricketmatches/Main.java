package com.crickbuzz.cricketmatches;
public class Main {
    public static void main(String[] args) {
        CricketAnalysis cricket_Analysis = new CricketAnalysis();
        try {
            String json_Res = cricket_Analysis.fetchMatchData();
            cricket_Analysis.processMatchData(json_Res);
        } catch (Exception e) {
            System.out.println("❌ Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
