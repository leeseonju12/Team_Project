package com.example.demo.weather2;

/**
 * 일자료 원본 데이터 + 가공 필드
 * - weatherGrade : 날씨 등급 (맑음/흐림/비/눈)
 * - comfortIndex : 쾌적지수 (0~100)
 * - comfortLabel : 쾌적지수 라벨 (매우좋음/좋음/보통/나쁨)
 */
public class AsosDailyProcessed {

    // ── 원본 필드 ──
    private String tm;
    private String stnNm;
    private String avgTa;
    private String minTa;
    private String maxTa;
    private String sumRn;
    private String avgWs;
    private String avgRhm;
    private String avgPa;
    private String ssDur;

    // ── 가공 필드 ──
    private String  weatherGradeLabel;  // 맑음/흐림/비/눈
    private String  weatherGradeColor;  // HEX 색상 (차트용)
    private int     comfortIndex;        // 0~100
    private String  comfortLabel;        // 매우좋음/좋음/보통/나쁨

    // Getters & Setters
    public String getTm()               { return tm; }
    public void setTm(String tm)        { this.tm = tm; }

    public String getStnNm()            { return stnNm; }
    public void setStnNm(String v)      { this.stnNm = v; }

    public String getAvgTa()            { return avgTa; }
    public void setAvgTa(String v)      { this.avgTa = v; }

    public String getMinTa()            { return minTa; }
    public void setMinTa(String v)      { this.minTa = v; }

    public String getMaxTa()            { return maxTa; }
    public void setMaxTa(String v)      { this.maxTa = v; }

    public String getSumRn()            { return sumRn; }
    public void setSumRn(String v)      { this.sumRn = v; }

    public String getAvgWs()            { return avgWs; }
    public void setAvgWs(String v)      { this.avgWs = v; }

    public String getAvgRhm()           { return avgRhm; }
    public void setAvgRhm(String v)     { this.avgRhm = v; }

    public String getAvgPa()            { return avgPa; }
    public void setAvgPa(String v)      { this.avgPa = v; }

    public String getSsDur()            { return ssDur; }
    public void setSsDur(String v)      { this.ssDur = v; }

    public String getWeatherGradeLabel()        { return weatherGradeLabel; }
    public void setWeatherGradeLabel(String v)  { this.weatherGradeLabel = v; }

    public String getWeatherGradeColor()        { return weatherGradeColor; }
    public void setWeatherGradeColor(String v)  { this.weatherGradeColor = v; }

    public int getComfortIndex()                { return comfortIndex; }
    public void setComfortIndex(int v)          { this.comfortIndex = v; }

    public String getComfortLabel()             { return comfortLabel; }
    public void setComfortLabel(String v)       { this.comfortLabel = v; }
}
