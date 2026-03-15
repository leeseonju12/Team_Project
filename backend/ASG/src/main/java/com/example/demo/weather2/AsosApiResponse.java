package com.example.demo.weather2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 기상청 ASOS API 공통 응답 구조 래퍼
 *
 * JSON 구조:
 * { "response": { "header": {...}, "body": { "items": { "item": [...] } } } }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsosApiResponse<T> {

    @JsonProperty("response")
    private Response<T> response;

    public Response<T> getResponse() { return response; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response<T> {
        @JsonProperty("header") private Header header;
        @JsonProperty("body")   private Body<T> body;

        public Header getHeader() { return header; }
        public Body<T> getBody() { return body; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        @JsonProperty("resultCode") private String resultCode;
        @JsonProperty("resultMsg")  private String resultMsg;

        public String getResultCode() { return resultCode; }
        public String getResultMsg()  { return resultMsg; }
        public boolean isSuccess() { return "00".equals(resultCode); }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body<T> {
        @JsonProperty("items")      private Items<T> items;
        @JsonProperty("pageNo")     private int pageNo;
        @JsonProperty("numOfRows")  private int numOfRows;
        @JsonProperty("totalCount") private int totalCount;

        public Items<T> getItems()   { return items; }
        public int getPageNo()       { return pageNo; }
        public int getNumOfRows()    { return numOfRows; }
        public int getTotalCount()   { return totalCount; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items<T> {
        @JsonProperty("item") private List<T> item;

        public List<T> getItem() { return item; }
    }
}
