package de.maibornwolff.hipchat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HipChatAPIClient {
    private static final Gson GSON = new GsonBuilder().create();

    private final String hipchatServer;
    private final String room;
    private final String token;

    public HipChatAPIClient(String hipchatServer, String room, String token) {
        this.hipchatServer = hipchatServer;
        this.room = room;
        this.token = token;
    }

    public void postPipelineError(String pipeline, String stage) {
        Map properties = new HashMap<>();
        properties.put("color", "red");
        properties.put("message", String.format("Pipeline %s failed at stage %s", pipeline, stage));
        properties.put("notify", "true");
        properties.put("mesage_format", "text");

        performPOSTRequest(String.format("%s/v2/room/%s/notification", hipchatServer, room), GSON.toJson(properties), token);
    }

    public void postPipelineSuccess(String pipeline, String stage) {
        Map properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("message", String.format("Pipeline %s passed", pipeline, stage));
        properties.put("notify", "true");
        properties.put("mesage_format", "text");
        performPOSTRequest(String.format("%s/v2/room/%s/notification", hipchatServer, room), GSON.toJson(properties), token);
    }

    public static String performPOSTRequest(String urlString, String requestBody, String token) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            connection.setRequestProperty("Content-Length",
                Integer.toString(requestBody.getBytes().length));
            connection.setRequestProperty("Authorization", "Bearer " + token);

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(requestBody);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuffer response = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to HipChat API server at " + urlString, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
