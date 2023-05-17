package com.otsohelper.utils;

import com.google.gson.Gson;
import com.otsohelper.utils.DataClasses.RequesrData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class NetworkUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger("otsohelper");
    public static RequesrData getOtsoUser(UUID uuid){
        LOGGER.info(uuid.toString());
        Gson g = new Gson();
        return g.fromJson(getHTML("http://127.0.0.1:5000/api/hasotso?uuid="+uuid.toString()), RequesrData.class);
    }

    public static String getHTML(String urlToRead){
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                for (String line; (line = reader.readLine()) != null; ) {
                    result.append(line);
                }
            }
            return result.toString();
        }catch (Exception e){
            return null;
        }

    }
}
