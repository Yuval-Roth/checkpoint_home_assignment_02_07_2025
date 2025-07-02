package com.yuval;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuval.records.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WeatherService {

    private String apiKey;
    private String filePath;

    public WeatherService(String filePath) {
        this.apiKey = getApiKey();
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new RuntimeException("API key is not set or empty");
        }
        this.filePath = filePath;
    }

    public void run(){
        var cities = getCities();
        Map<City, Double> dataMap = new HashMap<>();
        for(City city : cities) {
            Double temperature = getTemperature(city.name());
            dataMap.put(city, temperature);

            // rate limit
            Random rand = new Random();
            long sleepTime = rand.nextLong(50,200);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ignored) {}
        }
        WeatherData data = new WeatherData(dataMap);
        System.out.println(data);
         writeToFile(data); // not done yet
    }


    private List<City> getCities() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://weather-automation-checkpoint-task.westeurope.cloudapp.azure.com:3000/cities"))
                .build();

        String response;
        try {
            response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch cities", e);
        }

        // deserialize
        Gson gson = new Gson();
        try{
            Cities cities = gson.fromJson(response, Cities.class);
            if(cities == null || cities.cities() == null || cities.cities().isEmpty()) {
                throw new RuntimeException("No cities found in the response");
            }
            return cities.cities();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cities", e);
        }
    }

    private Double getTemperature (String cityName){
        // https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}
        Geocode geocode = geocodeFromCityName(cityName);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openweathermap.org/data/2.5/weather?lat=" + geocode.lat() + "&lon=" + geocode.lon() + "&appid=" + apiKey))
                .build();
        String response;
        try {
            response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch weather data for city: " + cityName, e);
        }

        // deserialize
        Gson gson = new Gson();
        try {
            TemperatureResponse tempResp = gson.fromJson(response, TemperatureResponse.class);
            return tempResp.main().temp();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse temperature for city: " + cityName, e);
        }
    }

    private Geocode geocodeFromCityName(String cityName) {
        //http://api.openweathermap.org/geo/1.0/direct?q={city name},{state code},{country code}&limit={limit}&appid={API key}

        String encodedCityName = URLEncoder.encode(cityName, java.nio.charset.StandardCharsets.UTF_8);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://api.openweathermap.org/geo/1.0/direct?q=" + encodedCityName + "&limit=1&appid="+ apiKey))
                .build();
        String response;
        try {
            response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch geocode for city: " + cityName, e);
        }
        // deserialize
        Gson gson = new Gson();
        Type type = new TypeToken<List<Geocode>>(){}.getType();
        try{
            List<Geocode> geocodes = gson.fromJson(response, type);
            if(geocodes == null || geocodes.isEmpty()) {
                throw new RuntimeException("No geocode found for city: " + cityName);
            }
            Geocode geocode = geocodes.get(0);
            return geocode;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse geocode for city: " + cityName, e);
        }
    }

    private String getApiKey(){
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://weather-automation-checkpoint-task.westeurope.cloudapp.azure.com:3000//privateKey_shh"))
                .build();

        String response;
        try {
            response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch API key", e);
        }

        // deserialize
        Gson gson = new Gson();
        try{
            ApiKey key = gson.fromJson(response, ApiKey.class);
            return key.key();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch API key", e);
        }
    }

    private void writeToFile(WeatherData weatherData) {
        String content = weatherData.toCsv();
        Utils.appendToFile(filePath, content);
    }
}
