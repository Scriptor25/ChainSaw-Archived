package io.scriptor.chainsaw;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class Constants {

        private Constants() {
        }

        public static final Gson GSON = new GsonBuilder()
                        .serializeNulls()
                        .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                        .create();

        public static final Gson PGSON = new GsonBuilder()
                        .setPrettyPrinting()
                        .serializeNulls()
                        .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                        .create();
}
