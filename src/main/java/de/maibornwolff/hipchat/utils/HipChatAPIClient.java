package de.maibornwolff.hipchat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import de.maibornwolff.hipchat.HipChatPlugin;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.commons.lang3.ArrayUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class HipChatAPIClient {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Logger LOG = Logger.getLoggerFor(HipChatPlugin.class);

    private final String hipChatServer;
    private final String room;
    private final String token;

    public HipChatAPIClient(String hipchatServer, String room, String token) {
        this.hipChatServer = hipchatServer;
        this.room = room;
        this.token = token;
    }

    public void postPipelineError(String pipeline, String stage) {
        Map properties = new HashMap<>();
        properties.put("color", "red");
        properties.put("message", String.format("Pipeline %s failed at stage %s", pipeline, stage));
        properties.put("notify", "true");
        properties.put("mesage_format", "text");

        performPOSTRequest(GSON.toJson(properties));
    }

    public void postPipelineSuccess(String pipeline, String stage) {
        Map properties = new HashMap<>();
        properties.put("color", "green");
        properties.put("message", String.format("Pipeline %s passed", pipeline, stage));
        properties.put("notify", "true");
        properties.put("mesage_format", "text");
        performPOSTRequest(GSON.toJson(properties));
    }

    public String performPOSTRequest(final String body) {
        RetryPolicy retryPolicy = new RetryPolicy()
            .retryOn(RuntimeException.class)
            .withMaxRetries(5)
            .withBackoff(1, 30, TimeUnit.SECONDS);

        return Failsafe.with(retryPolicy).get(new Callable<String>() {
            @Override
            public String call() throws Exception {
                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(null,null,null);

                SSLSocketFactory factory = (SSLSocketFactory)context.getSocketFactory();
                SSLSocket socket = (SSLSocket)factory.createSocket();
                String[] protocols = socket.getEnabledProtocols();

                if (!ArrayUtils.contains(protocols,"TLSv1.2")){
                    LOG.warn("TLS 1.2 is not enabled in this Java process. HipChat refuses any connection with TLS Versions below 1.2");
                    LOG.warn("Please enable TLS 1.2 if you experience 'Connection reset' issues.");
                }

                WebTarget target = ClientBuilder.newClient()
                    .target(hipChatServer)
                    .path("v2")
                    .path("room")
                    .path(room)
                    .path("notification");
                Response response = target
                    .request()
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .post(Entity.entity(body, MediaType.APPLICATION_JSON));

                LOG.info(String.format("Notified %s with status code %d", room, response.getStatus()));
                return "Status: " + response.getStatus();
            }
        });
    }
}
