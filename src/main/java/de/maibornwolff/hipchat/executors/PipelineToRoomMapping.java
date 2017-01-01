package de.maibornwolff.hipchat.executors;

import com.google.gson.annotations.SerializedName;

public class PipelineToRoomMapping {
    @SerializedName("pipeline")
    private String pipeline;
    @SerializedName("room")
    private String room;
    @SerializedName("token")
    private String token;

    public String getPipeline() {
        return pipeline;
    }

    public String getRoom() {
        return room;
    }

    public String getToken() {
        return token;
    }
}
