package Environment;

public class Environment {
    private WeatherCondition weatherCondition = WeatherCondition.NORMAL;

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(WeatherCondition weatherCondition) {
        if (weatherCondition == null) throw new IllegalArgumentException("Weather condition cannot be null");
        this.weatherCondition = weatherCondition;
    }

    public boolean isDustStorm() {
        return weatherCondition == WeatherCondition.DUST_STORM;
    }

    public boolean isCommDisruption() {
        return weatherCondition == WeatherCondition.COMMUNICATION_DISRUPTION;
    }
}
