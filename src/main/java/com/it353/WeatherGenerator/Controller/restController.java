package com.it353.WeatherGenerator.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.prominence.openweathermap.api.OpenWeatherMapClient;

@RestController
@RequestMapping("/api")
public class restController {

   

    OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient("1289d74b4b8369f314b56c857a2a6dde");

   

}
