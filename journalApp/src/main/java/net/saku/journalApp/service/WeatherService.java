package net.saku.journalApp.service;

import net.saku.journalApp.api.response.WeatherResponse;
import net.saku.journalApp.cache.AppCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apikey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppCache appCache;

    @Autowired
    private RedisService redisService;

    public WeatherResponse getWeather(String city){
        WeatherResponse weatherResponse = redisService.get("weather_of_" + city, WeatherResponse.class);
        if (weatherResponse != null) {
            return weatherResponse;
        }
        else {
            String finalAPI=appCache.app_Cache.get(AppCache.keys.WEATHER_API.toString()).replace("<city>",city).replace("<apikey>",apikey);
            ResponseEntity<WeatherResponse> response=restTemplate.exchange(finalAPI, HttpMethod.GET,null, WeatherResponse.class);
            WeatherResponse body=response.getBody();
            if(body!=null){
                redisService.set("weather_of_" + city, body, 300L); // Cache for 5 minutes
            }
            return body;
        }
    }
}
