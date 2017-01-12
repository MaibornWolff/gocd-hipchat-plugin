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

package de.maibornwolff.hipchat.executors.fields;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLField extends Field {
    public URLField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
    }

    @Override
    public String doValidate(String input) {
        if (StringUtils.isBlank(input)) {
            return this.displayName + " must not be blank.";
        }
        try {
            new URL(input);
        } catch (MalformedURLException e) {
            return this.displayName + " is not a valid URL.";
        }
        return null;
    }

}
