package net.danielsnet.indexer.neocities;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.danielsnet.indexer.UI;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class NeocitiesConnect {
    private static final String API_URL = "https://neocities.org/api/info?sitename=";
    private static final String NEOCITIES = "neocities";

    private final ObjectMapper mapper;
    private final NeocitiesAPI api;

    public NeocitiesConnect(String username) {
        this.mapper = new ObjectMapper();
        api = praseAPIData(downloadAPIData(API_URL + username), username);
    }

    public NeocitiesAPI getApi() {
        return api;
    }

    private String downloadAPIData(String url) {
        String data;
        URLConnection connection;

        try {
            connection = new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            scanner.useDelimiter("\\Z");
            data = scanner.next();
        } catch (IOException ex) {
            return null;
        }

        return data;
    }

    private NeocitiesAPI praseAPIData(String rawData, String username) {
        try {
            return mapper.readValue(rawData, NeocitiesAPI.class);
        } catch (Exception ex) {
            UI.log("Neocities Error: Neocities API returned bad data, site may be dead: " + username);
            return null;
        }
    }
}
