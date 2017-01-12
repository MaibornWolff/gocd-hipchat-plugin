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
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

    public HipChatAPIClient(String hipChatServer, String room, String token) {
        this.hipChatServer = hipChatServer;
        this.room = room;
        this.token = token;
    }

    public void postPipelineError(String pipeline, String stage) {
        Map properties = new HashMap<>();
        properties.put("from", "HipChat plugin for GoCD");
        properties.put("color", "red");
        properties.put("message", String.format("Pipeline <strong>%s</strong> failed at stage <strong>%s</strong>", pipeline, stage));
        properties.put("notify", "true");
        properties.put("message_format", "html");
        tryToPostNotificationToHipChat(GSON.toJson(properties));
    }

    public void tryToPostNotificationToHipChat(final String body) {
        RetryPolicy retryPolicy = new RetryPolicy()
            .retryOn(RuntimeException.class)
            .withMaxRetries(5)
            .withBackoff(1, 30, TimeUnit.SECONDS);

        Failsafe.with(retryPolicy).get(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                checkForTLSV12();
                sendNotificationToHipChat(body);
                return null;
            }
        });
    }

    private void sendNotificationToHipChat(String body) {
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
    }

    private void checkForTLSV12() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(null,null,null);

        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket)factory.createSocket();
        String[] enabledProtocols = socket.getEnabledProtocols();

        if (!ArrayUtils.contains(enabledProtocols,"TLSv1.2")){
            LOG.warn("TLS 1.2 is not enabled in this Java process. HipChat may refuse connections with TLS 1.1 and below.");
            LOG.warn("Please enable TLS 1.2 (-Dhttps.protocols=TLSv1.1,TLSv1.2) if you experience 'Connection reset' issues.");
        }
    }
}
