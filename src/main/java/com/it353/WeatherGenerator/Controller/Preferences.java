package com.it353.WeatherGenerator.Controller;

public class Preferences {
    private String[] genres;
    private String length;

    Preferences(String[] g, String l){
        genres = g;
        length = l;
    }

    /**
     * @return String[] return the genres
     */
    public String[] getGenres() {
        return genres;
    }

    /**
     * @param genres the genres to set
     */
    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    /**
     * @return String return the length
     */
    public String getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(String length) {
        this.length = length;
    }

}