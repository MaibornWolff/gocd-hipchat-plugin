package de.maibornwolff.hipchat.executors;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class PipelineToRoomMappingFieldTest {

    @Test
    public void it_accepts_empty_json_arrays() throws Exception {
        PipelineToRoomMappingField mappingField = new PipelineToRoomMappingField(null, null, null, null, null, null);
        String error = mappingField.doValidate("[]");
        assertThat(error, isEmptyOrNullString());
    }

    @Test
    public void it_accepts_empty_strings() throws Exception {
        PipelineToRoomMappingField mappingField = new PipelineToRoomMappingField(null, null, null, null, null, null);
        String error = mappingField.doValidate("");
        assertThat(error, isEmptyOrNullString());
    }

    @Test
    public void it_accepts_valid_entries() throws Exception {
        PipelineToRoomMappingField mappingField = new PipelineToRoomMappingField(null, null, null, null, null, null);
        String error = mappingField.doValidate("[{\"pipeline\":\"FOO\",\"room\":\"FooRoom\",\"token\":\"Gibberish\"}]");
        assertThat(error, isEmptyOrNullString());
    }

    @Test
    public void it_rejects_missing_fields() throws Exception {
        PipelineToRoomMappingField mappingField = new PipelineToRoomMappingField(null, null, null, null, null, null);
        String error = mappingField.doValidate("[{\"room\":\"FooRoom\",\"token\":\"Gibberish\"}]");
        assertThat(error, is("Invalid JSON: 'pipeline' attribute is missing for entry 0."));
    }
}
