package io.scriptor.chainsaw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Constants {

    private Constants() {
    }

    public static final Gson GSON = new GsonBuilder()
            // .setPrettyPrinting()
            .serializeNulls()
            .create();
}
