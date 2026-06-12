package com.diellabs.frexa.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FearGreedData {
    public String name;
    public List<Entry> data;

    public static class Entry {
        public String value;
        @SerializedName("value_classification") public String valueClassification;
    }
}
