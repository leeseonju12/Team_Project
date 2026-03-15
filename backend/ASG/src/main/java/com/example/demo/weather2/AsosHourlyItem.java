package com.example.demo.weather2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AsosHourlyItem {

    @JsonProperty("tm")   private String tm;   // 시각 (yyyyMMddHH)
    @JsonProperty("stnId") private String stnId; // 지점번호
    @JsonProperty("stnNm") private String stnNm; // 지점명
    @JsonProperty("ta")   private String ta;   // 기온(℃)
    @JsonProperty("rn")   private String rn;   // 강수량(mm)
    @JsonProperty("ws")   private String ws;   // 풍속(m/s)
    @JsonProperty("wd")   private String wd;   // 풍향(16방위)
    @JsonProperty("hm")   private String hm;   // 상대습도(%)
    @JsonProperty("pa")   private String pa;   // 현지기압(hPa)
    @JsonProperty("ps")   private String ps;   // 해면기압(hPa)
    @JsonProperty("td")   private String td;   // 이슬점온도(℃)
    @JsonProperty("pv")   private String pv;   // 증기압(hPa)
    @JsonProperty("ts")   private String ts;   // 지면온도(℃)
    @JsonProperty("ss")   private String ss;   // 일조(hr)

    // Getters
    public String getTm()    { return tm; }
    public String getStnId() { return stnId; }
    public String getStnNm() { return stnNm; }
    public String getTa()    { return ta; }
    public String getRn()    { return rn; }
    public String getWs()    { return ws; }
    public String getWd()    { return wd; }
    public String getHm()    { return hm; }
    public String getPa()    { return pa; }
    public String getPs()    { return ps; }
    public String getTd()    { return td; }
    public String getPv()    { return pv; }
    public String getTs()    { return ts; }
    public String getSs()    { return ss; }
}
