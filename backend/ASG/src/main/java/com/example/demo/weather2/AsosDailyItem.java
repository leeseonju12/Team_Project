package com.example.demo.weather2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsosDailyItem {

    @JsonProperty("tm")       private String tm;       // 날짜
    @JsonProperty("stnId")    private String stnId;    // 지점번호
    @JsonProperty("stnNm")    private String stnNm;    // 지점명
    @JsonProperty("avgTa")    private String avgTa;    // 평균기온(℃)
    @JsonProperty("minTa")    private String minTa;    // 최저기온(℃)
    @JsonProperty("maxTa")    private String maxTa;    // 최고기온(℃)
    @JsonProperty("sumRn")    private String sumRn;    // 일강수량(mm)
    @JsonProperty("avgWs")    private String avgWs;    // 평균풍속(m/s)
    @JsonProperty("maxWs")    private String maxWs;    // 최대풍속(m/s)
    @JsonProperty("avgRhm")   private String avgRhm;   // 평균상대습도(%)
    @JsonProperty("avgPa")    private String avgPa;    // 평균현지기압(hPa)
    @JsonProperty("avgPs")    private String avgPs;    // 평균해면기압(hPa)
    @JsonProperty("ssDur")    private String ssDur;    // 합계일조시간(hr)
    @JsonProperty("sumSsHr")  private String sumSsHr;  // 합계일사량(MJ/m²)

    // Getters
    public String getTm()      { return tm; }
    public String getStnId()   { return stnId; }
    public String getStnNm()   { return stnNm; }
    public String getAvgTa()   { return avgTa; }
    public String getMinTa()   { return minTa; }
    public String getMaxTa()   { return maxTa; }
    public String getSumRn()   { return sumRn; }
    public String getAvgWs()   { return avgWs; }
    public String getMaxWs()   { return maxWs; }
    public String getAvgRhm()  { return avgRhm; }
    public String getAvgPa()   { return avgPa; }
    public String getAvgPs()   { return avgPs; }
    public String getSsDur()   { return ssDur; }
    public String getSumSsHr() { return sumSsHr; }
}
