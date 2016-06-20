package com.bruno.dunbar.jrep;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EntityToJsonTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldConvertBasicEntityToJson() throws IOException {

        BasicEntity entity = createValid().build();

        JRepSerializer serializer = new JRepSerializer();
        String json = serializer.toJson(BasicEntity.COMPLETE, entity);

        assertNotNull(json);

        BasicEntity fromJson = objectMapper.readValue(json, BasicEntity.class);

        assertNotNull(fromJson);
        assertEquals(entity.getId(), fromJson.getId());
        assertEquals(entity.getName(), fromJson.getName());
        assertEquals(null, fromJson.getAlive());
    }

    private BasicEntity.Builder createValid() {
        return BasicEntity.Builder.create()
                .id(1L)
                .alive(true)
                .name("John Doe");
    }

}
