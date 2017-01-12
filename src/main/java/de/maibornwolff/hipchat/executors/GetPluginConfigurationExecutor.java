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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import de.maibornwolff.hipchat.RequestExecutor;
import de.maibornwolff.hipchat.executors.fields.Field;
import de.maibornwolff.hipchat.executors.fields.PipelineConfigField;
import de.maibornwolff.hipchat.executors.fields.URLField;

import java.util.LinkedHashMap;
import java.util.Map;

public class GetPluginConfigurationExecutor implements RequestExecutor {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    public static final Field HIPCHAT_SERVER_URL = new URLField("hipchat_server_url",
            "HipChat Server URL", null, true, false, "0");

    public static final Field PIPELINE_TO_ROOM_MAPPING = new PipelineConfigField("pipelineConfig", "Pipeline Config",
            null, false, true, "1");

    public static final Map<String, Field> FIELDS = new LinkedHashMap<>();

    static {
        FIELDS.put(HIPCHAT_SERVER_URL.key(), HIPCHAT_SERVER_URL);
        FIELDS.put(PIPELINE_TO_ROOM_MAPPING.key(), PIPELINE_TO_ROOM_MAPPING);
    }

    public GoPluginApiResponse execute() {
        return new DefaultGoPluginApiResponse(200, GSON.toJson(FIELDS));
    }
}
