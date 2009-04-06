package functionalTests.component.webservices;

import java.io.Serializable;

public class Weather implements Serializable {
    private float temperature;
    private String forecast;
    private boolean rain;
    private float howMuchRain;

    public Weather() {
    }

    public void setTemperature(float temp){
        temperature = temp;
    }

    public float getTemperature(){
        return temperature;
    }

    public void setForecast(String fore){
        forecast = fore;
    }

    public String getForecast(){
        return forecast;
    }

    public void setRain(boolean r){
        rain = r;
    }

    public boolean getRain(){
        return rain;
    }

    public void setHowMuchRain(float howMuch){
        howMuchRain = howMuch;
    }

    public float getHowMuchRain(){
        return howMuchRain;
    }
}

