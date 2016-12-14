package h2l.se.uit.placesaroundme;

/**
 * Created by Luantm on 12/14/2016.
 */

public class WeatherDTO {
    public String getWind() {
        return wind;
    }
private  String humidity;

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getMin() {

        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    private String wind = "8 Km/h";
    private String temp = "31 Celsius ";
    private String min = "28 Celsius ";
    private String max = "34 Celsius";
    private String tip = "Bring your umbrella and some water";
    private String descripton = "It's clear and hot";

    public String getDescripton() {
        return descripton;
    }

    public void setDescripton(String descripton) {
        this.descripton = descripton;
    }
}
