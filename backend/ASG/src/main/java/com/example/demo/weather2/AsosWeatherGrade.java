package com.example.demo.weather2;

public enum AsosWeatherGrade {
    SUNNY("맑음", "#FFD700"),
    CLOUDY("흐림", "#A0AEC0"),
    RAINY("비",   "#3B82F6"),
    SNOWY("눈",   "#93C5FD");

    private final String label;
    private final String color;

    AsosWeatherGrade(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() { return label; }
    public String getColor() { return color; }
}
