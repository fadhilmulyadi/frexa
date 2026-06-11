package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CryptoCompareHistominute {

    @SerializedName("Response")
    public String response;

    @SerializedName("Data")
    public DataWrapper data;

    public static class DataWrapper {
        @SerializedName("Data")
        public List<Candle> data;
    }

    public static class Candle {
        @SerializedName("time")  public long time;    // Unix seconds — kalikan 1000 untuk ms
        @SerializedName("open")  public double open;
        @SerializedName("high")  public double high;
        @SerializedName("low")   public double low;
        @SerializedName("close") public double close;
    }
}
