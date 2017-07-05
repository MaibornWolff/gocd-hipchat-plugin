package de.maibornwolff.hipchat.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import de.maibornwolff.hipchat.HipChatPlugin;
import de.maibornwolff.hipchat.requests.StageStatusRequest;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class HipChatAPIClient {
    private static final Gson GSON = new GsonBuilder().create();
    public static final Logger LOG = Logger.getLoggerFor(HipChatPlugin.class);

    private final String gocdServerUrl;
    private final String hipChatServer;
    private final String room;
    private final String token;

    public HipChatAPIClient(String gocdServerUrl, String hipChatServer, String room, String token) {
        this.gocdServerUrl = gocdServerUrl;
        this.hipChatServer = hipChatServer;
        this.room = room;
        this.token = token;
    }

    public void postPipelineError(StageStatusRequest stageStatus) {
        Map properties = new HashMap<>();
        properties.put("from", "HipChat plugin for GoCD");
        properties.put("color", "red");
        properties.put("message", htmlErrorMessage(stageStatus));
        properties.put("notify", "true");
        Map card = new HashMap<>();
        card.put("style", "application");
        card.put("url", urlForStage(stageStatus));
        card.put("format", "compact");
        card.put("id", UUID.randomUUID().toString());
        card.put("title", "GoCD pipeline error");
        Map description = new HashMap<>();
        description.put("format", "html");
        description.put("value", htmlErrorMessage(stageStatus));
        card.put("description", description);
        HashMap<String, String> icon = new HashMap<>();
        icon.put("url", "https://raw.githubusercontent.com/grundic/yagocd/master/img/gocd_logo.png");
        card.put("icon", icon);
        card.put("attributes", Arrays.asList());
        properties.put("card", card);
        tryToPostNotificationToHipChat(GSON.toJson(properties));
    }

    private String htmlErrorMessage(StageStatusRequest stageStatus) {
        return String.format("Pipeline <strong>%s</strong> failed at stage <a href=\"%s/\">" +
            "<strong>%s</strong></a>.", stageStatus.pipeline.name, urlForStage(stageStatus), stageStatus.pipeline.stage.name);
    }

    private String fixMessage(StageStatusRequest stageStatus) {
        return String.format("Pipeline <strong>%s</strong> passed stage <a href=\"%s/\">" +
            "<strong>%s</strong></a>.", stageStatus.pipeline.name, urlForStage(stageStatus), stageStatus.pipeline.stage.name);
    }

    private String urlForStage(StageStatusRequest stageStatus) {
        return String.format("%s/pipelines/%s/%s/%s/%s", gocdServerUrl,
            stageStatus.pipeline.name, stageStatus.pipeline.counter,
            stageStatus.pipeline.stage.name, stageStatus.pipeline.stage.counter);
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
        if (response.getStatus() != 204){
            LOG.error("HipChat Server replied with error code.");
        }
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

    public void postPipelineFixed(StageStatusRequest stageStatus) {
        Map properties = new HashMap<>();
        properties.put("from", "HipChat plugin for GoCD");
        properties.put("color", "green");
        properties.put("message", fixMessage(stageStatus));
        properties.put("notify", "true");
        Map card = new HashMap<>();
        card.put("style", "application");
        card.put("url", urlForStage(stageStatus));
        card.put("format", "compact");
        card.put("id", UUID.randomUUID().toString());
        card.put("title", "GoCD pipeline fixed");
        Map description = new HashMap<>();
        description.put("format", "html");
        description.put("value", fixMessage(stageStatus));
        card.put("description", description);
        HashMap<String, String> icon = new HashMap<>();
        icon.put("url", "https://raw.githubusercontent.com/grundic/yagocd/master/img/gocd_logo.png");
        card.put("icon", icon);
        card.put("attributes", Arrays.asList());
        properties.put("card", card);
        tryToPostNotificationToHipChat(GSON.toJson(properties));
    }
}
