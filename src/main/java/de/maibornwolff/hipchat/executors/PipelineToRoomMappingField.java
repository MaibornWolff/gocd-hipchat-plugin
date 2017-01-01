package de.maibornwolff.hipchat.executors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PipelineToRoomMappingField extends Field{
    public PipelineToRoomMappingField(String key, String displayName, String defaultValue, Boolean required, Boolean secure, String displayOrder) {
        super(key, displayName, defaultValue, required, secure, displayOrder);
    }


    @Override
    protected String doValidate(String input) {
        if (StringUtils.isEmpty(input)) return null;
        return null;
    }

}
