package com.it353.WeatherGenerator.Controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpSession;

import org.apache.hc.core5.http.ParseException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

@Controller
public class controller {


    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/generate")
    public String generate() {
        return "generate";
    }

    @GetMapping("/customize")
    public String customize() {
        return "customize";
    }

    // save preferences to session data
    @PostMapping("/preferences")
    public String postPreferences(@RequestParam(name = "genres", required = false) String[] genres,
            @RequestParam(name = "length", required = false) String length,
            HttpSession session) {
        session.setAttribute("prefs", new Preferences(genres, length));
        for (int i = 0; i < genres.length; i++) {
            System.out.println(genres[i]);
        }
        //System.out.println(length);
        return "/index";
    }

    // all logic with APIs goes here
    @PostMapping("/generate")
    public String postWeather(@RequestParam("locationData") String location, HttpSession session, Model model)
            throws ParseException, SpotifyWebApiException, IOException, URISyntaxException, InterruptedException,
            ExecutionException {
        


        // save preferences to session data
        Preferences prefs = null;
        try {
            prefs = (Preferences) session.getAttribute("prefs");
        } catch (Exception e) {
            System.out.println("Error retrieving preferences from session");
        }
        if (prefs == null) {
            // Set default values
            prefs = new Preferences(new String[] {"pop", "indie-pop"}, "10");
        }

        System.out.println("testing"+location);
        // turn location data into coordinates array
        String[] coordinates = new String[2];
        if(location.length() > 1){
            System.out.println("Hello I'm setting the location");
            session.setAttribute("location-data", location);
        }else{
            System.out.println("Hello I'm getting the location");
            location = (String)session.getAttribute("location-data");
        }
        coordinates = location.split(", ");

        // convert ZIP code to coordinates
        if (coordinates.length < 2) {
            try {
                System.out.println("INSIDE LOOP " + coordinates + "  " + (String)session.getAttribute("location-data"));
                String url = "http://api.openweathermap.org/geo/1.0/zip?zip=" + coordinates[0]
                        + "&appid=1289d74b4b8369f314b56c857a2a6dde";
                String json = Unirest.get(url).asString().getBody();
                System.out.println(json);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);
                double lat = root.get("lat").asDouble();
                double lon = root.get("lon").asDouble();

                String tmp[] = { String.valueOf(lat), String.valueOf(lon) };
                coordinates = tmp;
                //System.out.println(coordinates);
            } catch (UnirestException e) {
                System.out.println("Oopsie");
            }
        }

        // to get weather data and location data with newly formatted coordinates
        JSONObject weatherJson;
        JSONObject locationJson;
        String name = "";
        String weatherDesc = "";
        String weatherMain = "";
        String tempString = "";
        double tempF = 0;

        try {
            HttpResponse<String> weatherData = Unirest
                    .get("https://api.openweathermap.org/data/2.5/weather?lat=" + coordinates[0] + "&lon="
                            + coordinates[1] + "&appid=1289d74b4b8369f314b56c857a2a6dde")
                    .header("accept", "application/json")
                    .asString();
            // System.out.println(weatherData.getBody());

            HttpResponse<String> locationData = Unirest
                    .get("http://api.openweathermap.org/geo/1.0/reverse?lat=" + coordinates[0] + "&lon="
                            + coordinates[1] + "&appid=1289d74b4b8369f314b56c857a2a6dde")
                    .header("accept", "application/json")
                    .asString();

            weatherJson = new JSONObject(weatherData.getBody());
            locationJson = new JSONObject(locationData.getBody().substring(1, locationData.getBody().length() - 1));
            //System.out.println(weatherJson);

            // getting variables from JSON formatted responses
            JSONObject weatherObj = weatherJson.getJSONArray("weather").getJSONObject(0);

            name = locationJson.getString("name");
            weatherDesc = weatherObj.getString("description");
            weatherMain = weatherObj.getString("main");
            double tempCelc = weatherJson.getJSONObject("main").getDouble("temp") - 272.15;
            tempF = (tempCelc * 9 / 5) + 32;
            // the formatt isnt working for some reason
            tempString = String.format("%.2f", tempF);

        } catch (UnirestException e) {
            System.out.println("Oopsie");
        }

        model.addAttribute("name", name);
        model.addAttribute("weatherMain", weatherMain);
        model.addAttribute("tempString", tempString);
        model.addAttribute("weatherDesc", weatherDesc);

        System.out.println(name + " " + weatherDesc + " " + weatherMain + " " + tempString);

        // validation with spotify
        final String clientId = "00f68e6073d64352bcd382ba7f910365";
        final String clientSecret = "512032a99c004f33978efd87a23a1e36";
        String accessToken = "";

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();

        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        Future<se.michaelthelin.spotify.model_objects.credentials.ClientCredentials> future = clientCredentialsRequest
                .executeAsync();
        se.michaelthelin.spotify.model_objects.credentials.ClientCredentials clientCredentials = future.get();

        accessToken = clientCredentials.getAccessToken();
        spotifyApi.setAccessToken(accessToken);

        // set platlist discription name
        int tempRound = (int) Math.round(tempF);
        String plName = tempRound + "\u00B0 and " + weatherDesc;
        model.addAttribute("plName", plName);

        // get recommendations
        Recommendations recs;
        String currentWeatherMain = weatherMain;
        double currentTemp = tempF;
        double[] params = new double[8];
        params[5] = 65;

        // add genres custumization from prefs session data
        if (prefs.getLength() == null) {
            params[7] = 10;
        } else {
            int plLength = Integer.parseInt(prefs.getLength());
            params[7] = plLength;
        }

        // getting the genre seeds
        String[] genreSeed = new String[5];
        String genreSeedString = "";
        int genrelimit = prefs.getGenres().length > 2 ? 2 : prefs.getGenres().length;
            // add customized genres to the array
            for (int i = 0; i < genrelimit; i++) {
                genreSeed[i] = prefs.getGenres()[i];
                System.out.println(genreSeed[i]);
            }
            // add these to the string
            for (int i = 0; i < genrelimit; i++) {
                if (i == genrelimit-1) {
                    genreSeedString += genreSeed[i];
                } else {
                    genreSeedString += genreSeed[i] + ",";
                }
            }
            System.out.println(genreSeedString);


        // 0)energy,
        // 1)acousticness,
        // 2)instrumentalness(no vocal=1 instrumental=0.5),
        // 3)tempo BPM
        // 4)valence(postitiveness),
        // 5)popularity
        // 6)dancibility
        // 7)limit

        switch (currentWeatherMain) {

            case "Thunderstorm":
                params[0] = 0.75;
                params[1] = 0.35;
                params[2] = 0.2;
                params[3] = 100;
                params[4] = 0.4;

                break;

            case "Drizzle":
                params[0] = 0.25;
                params[1] = 0.8;
                params[2] = 0.35;
                params[3] = 60;
                params[4] = 0.3;
                break;

            case "Rain":
                params[0] = 0.15;
                params[1] = 0.9;
                params[2] = 0.4;
                params[3] = 90;
                params[4] = 0.1;
                break;

            case "Snow":
                params[0] = 0.5;
                params[1] = 0.5;
                params[2] = 0.2;
                params[3] = 100;
                params[4] = .65;
                break;

            case "Clear":
                params[0] = 0.65;
                params[1] = 0.35;
                params[2] = 0.1;
                params[3] = 125;
                params[4] = 0.9;
                break;

            case "Clouds":
                params[0] = 0.4;
                params[1] = 0.5;
                params[2] = 0.3;
                params[3] = 100;
                params[4] = 0.37;
                break;

            default:
                params[0] = 0.5;
                params[1] = 0.5;
                params[2] = 0.85;
                params[3] = 110;
                params[4] = 0.4;
                break;
        }

        if (currentTemp < 32) {
            params[6] = 0;
        } else if (currentTemp > 33 && currentTemp < 40) {
            params[6] = 0.1;
        } else if (currentTemp > 39 && currentTemp < 60) {
            params[6] = 0.3;
        } else if (currentTemp > 59 && currentTemp < 70) {
            params[6] = 0.5;
        } 
        else if(currentTemp > 69 && currentTemp < 90){
            params[6] = 0.9;
        }
        else if (currentTemp > 89) {
            params[6] = 0.75;
        } else {
            params[6] = 0.5;
        }

        // generate get recommendations
        recs = spotifyApi.getRecommendations().seed_artists("06HL4z0CvFAxyc27GXpf02,3TVXtAsR1Inumwj472S9r4")
                // default limit is 10
                .limit((int) params[7])
                // defaul genre is pop
                .seed_genres(genreSeedString)
                .seed_tracks("4vHNeBWDQpVCmGbaccrRzi").target_energy((float) params[0])
                .target_acousticness((float) params[1]).target_instrumentalness((float) params[2])
                .target_tempo((float) params[3])
                .target_valence((float) params[4]).target_popularity((int) params[5])
                .target_danceability((float) params[6])
                .build().execute();
        //System.out.println(recs);

        model.addAttribute("recs", new Gson().toJson(recs));

       
        return "/generate";

    }

}
