package functionalTests.activeobject.webservices;

public class WeatherService {
    private Weather weather;

    public WeatherService() {
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public Weather getWeather() {
        return this.weather;
    }
}
