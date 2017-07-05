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

package de.maibornwolff.hipchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.maibornwolff.hipchat.executors.fields.PipelineToRoomMapping;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PluginSettings {
    private static final Gson GSON = new GsonBuilder().
            excludeFieldsWithoutExposeAnnotation().
            create();

    @Expose
    @SerializedName("gocd_server_url")
    private String gocdServerUrl;

    @Expose
    @SerializedName("hipchat_server_url")
    private String hipchatServerUrl;

    @Expose
    @SerializedName("pipelineConfig")
    private String pipelineConfig;

    private List<PipelineToRoomMapping> pipelineToRoomMappings = Collections.emptyList();


    public static PluginSettings fromJSON(String json) {
        PluginSettings pluginSettings = GSON.fromJson(json, PluginSettings.class);
        PipelineToRoomMapping[] mappings = GSON.fromJson(pluginSettings.getPipelineConfig(), PipelineToRoomMapping[].class);
        if (mappings!= null) {
            pluginSettings.pipelineToRoomMappings = new ArrayList<>(Arrays.asList(mappings));
        }
        return pluginSettings;
    }

    public boolean isConfigured() {
        return StringUtils.isNotEmpty(getHipchatServerUrl()) && StringUtils.isNotEmpty(getGocdServerUrl());
    }

    public String getPipelineConfig() {
        return pipelineConfig;
    }

    public String getHipchatServerUrl() {
        return hipchatServerUrl;
    }

    public List<PipelineToRoomMapping> getPipelineToRoomMappings() {
        return pipelineToRoomMappings;
    }

    public String getGocdServerUrl() {
        return gocdServerUrl;
    }
}
