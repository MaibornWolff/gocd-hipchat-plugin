/*
 * Copyright 2016 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.maibornwolff.hipchat.executors;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import de.maibornwolff.hipchat.HipChatPlugin;
import de.maibornwolff.hipchat.PluginRequest;
import de.maibornwolff.hipchat.PluginSettings;
import de.maibornwolff.hipchat.RequestExecutor;
import de.maibornwolff.hipchat.executors.fields.PipelineToRoomMapping;
import de.maibornwolff.hipchat.requests.StageStatusRequest;
import de.maibornwolff.hipchat.utils.HipChatAPIClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class StageStatusRequestExecutor implements RequestExecutor {
    private static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    public static final Logger LOG = Logger.getLoggerFor(HipChatPlugin.class);

    private final Set<String> pipelinesThatFailedBefore;
    private final StageStatusRequest stageStatus;
    private final PluginRequest pluginRequest;

    public StageStatusRequestExecutor(StageStatusRequest request, PluginRequest pluginRequest, Set<String> failedPipelines){
        this.pipelinesThatFailedBefore = failedPipelines;
        this.stageStatus = request;
        this.pluginRequest = pluginRequest;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        HashMap<String, Object> responseJson = new HashMap<>();
        try {
            sendNotification();
            responseJson.put("status", "success");
        } catch (Exception e) {
            responseJson.put("status", "failure");
            LOG.error("Unexpected exception occurred", e);
            responseJson.put("messages", Arrays.asList(e.getMessage(), "Check plugin logfile for details."));
        }
        return new DefaultGoPluginApiResponse(200, GSON.toJson(responseJson));
    }

    private String stageAndPipelineName() {
        return stageStatus.pipeline.name + stageStatus.pipeline.stage.name;
    }

    protected void sendNotification() throws Exception {
        PluginSettings pluginSettings = pluginRequest.getPluginSettings();
        LOG.info(String.format("HipChat URL set to %s, GoCD URL is set to: %s", pluginSettings.getHipchatServerUrl(), pluginSettings.getGocdServerUrl()));
        if (!pluginSettings.isConfigured()) {
            LOG.warn("HipChat plugin is not configured. Please configure it by clicking on the Gears icon on the plugin admin page.");
            return;
        }
        for (PipelineToRoomMapping mapping : pluginSettings.getPipelineToRoomMappings()){
            if (stageStatus.pipeline.name.equals(mapping.getName())) {
                if ("Failed".equals(stageStatus.pipeline.stage.state)) {
                    markPipelineAsFailed(stageAndPipelineName());
                    notifyPipelineFailure(pluginSettings.getGocdServerUrl(),pluginSettings.getHipchatServerUrl(), mapping, stageStatus);
                } else if ("Passed".equals(stageStatus.pipeline.stage.state) && pipelinesThatFailedBefore.contains(stageAndPipelineName())){
                    markPipelineAsSucceeded(stageAndPipelineName());
                    notifyPipelineFixed(pluginSettings.getGocdServerUrl(),pluginSettings.getHipchatServerUrl(), mapping, stageStatus);
                }
            }
        }
    }

    private void markPipelineAsSucceeded(String name) {
        this.pipelinesThatFailedBefore.remove(name);
    }

    private void markPipelineAsFailed(String name) {
        this.pipelinesThatFailedBefore.add(name);
    }

    private void notifyPipelineFixed(String gocdServerUrl, String hipChatServerUrl, PipelineToRoomMapping mapping, StageStatusRequest stageStatus) {
        HipChatAPIClient client = new HipChatAPIClient(gocdServerUrl, hipChatServerUrl, mapping.getRoom(), mapping.getToken());
        client.postPipelineFixed(stageStatus);
    }

    private void notifyPipelineFailure(String gocdServerUrl, String hipChatServerUrl, PipelineToRoomMapping mapping, StageStatusRequest stageStatus) {
        HipChatAPIClient client = new HipChatAPIClient(gocdServerUrl, hipChatServerUrl, mapping.getRoom(), mapping.getToken());
        client.postPipelineError(stageStatus);
    }
}
