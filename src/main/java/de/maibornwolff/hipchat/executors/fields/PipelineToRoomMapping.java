package de.maibornwolff.hipchat.executors.fields;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PipelineToRoomMapping {
    @Expose
    @SerializedName("name")
    private String name = "";
    @Expose
    @SerializedName("room")
    private String room = "";
    @Expose
    @SerializedName("token")
    private String token = "";

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "PipelineToRoomMapping{" +
            "name='" + name + '\'' +
            ", room='" + room + '\'' +
            ", token='" + token + '\'' +
            '}';
    }
}
