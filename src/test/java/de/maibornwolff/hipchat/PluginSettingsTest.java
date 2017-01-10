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

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Ignore
public class PluginSettingsTest {
    @Test
    public void shouldDeserializeFromJSON() throws Exception {
        PluginSettings pluginSettings = PluginSettings.fromJSON("{" +
                "\"hipchat_server_url\": \"https://hipchat.example.com/\", " +
                "\"default_room\": \"Myroom\", " +
                "\"pipelineConfig\": \"[{\\\"foo\\\":\\\"bar\\\",\\\"foobar\\\":\\\"baz\\\"}]\" " +
                "}");

        assertThat(pluginSettings.getHipchatServerUrl(), is("https://hipchat.example.com/"));
        assertThat(pluginSettings.getPipelineConfig(), is("[{\"foo\":\"bar\",\"foobar\":\"baz\"}]"));
    }
}
