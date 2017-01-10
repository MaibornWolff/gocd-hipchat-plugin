package de.maibornwolff.hipchat.executors.fields;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PipelineConfigField extends Field {
    private static final Gson GSON = new Gson();

    public PipelineConfigField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
    }

    @Override
    protected String doValidate(String input) {
        Type listType = new TypeToken<ArrayList<PipelineToRoomMapping>>(){}.getType();
        try {
            GSON.fromJson(input, listType);
        } catch (JsonSyntaxException e){
            return "No valid JSON received for PipelineConfigField " + key;
        }
        return null;
    }
}
