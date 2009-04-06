package functionalTests.component.webservices;

public class WeatherServiceComponent implements WeatherServiceItf{
    private Weather weather;

    public WeatherServiceComponent() {
    }

    public void setWeather(Weather weather){
        this.weather = weather;
    }

    public Weather getWeather(){
        return this.weather;
    }
}

