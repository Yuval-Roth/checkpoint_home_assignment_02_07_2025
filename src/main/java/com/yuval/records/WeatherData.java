package com.yuval.records;

import java.util.Map;

public record WeatherData(Map<City, Double> weatherData) {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Sort descending by temperature
        var sortedData = weatherData.entrySet().stream()
            .sorted(Map.Entry.comparingByValue((o1, o2) -> Double.compare(o2, o1)))
            .toList();

        // build string
        for (var entry : sortedData) {
            String shortNum = String.format("%.2f", entry.getValue());
            sb.append(entry.getKey().name()).append(": ").append(shortNum).append("°C\n");
        }
        return sb.toString();
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("City,Temperature\n");

        // Sort descending by temperature
        var sortedData = weatherData.entrySet().stream()
            .sorted(Map.Entry.comparingByValue((o1, o2) -> Double.compare(o2, o1)))
            .toList();

        // build CSV string
        for (var entry : sortedData) {
            String shortNum = String.format("%.2f", entry.getValue());
            sb.append(entry.getKey().name()).append(",").append(shortNum).append("\n");
        }
        return sb.toString();
    }
}
