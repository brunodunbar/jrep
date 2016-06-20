package com.bruno.dunbar.jrep;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonToEntity {

    @Test
    public void shouldConvertJsonToBasicEntity() throws IOException {

        BasicEntity entity = createValid().build();

        JRepSerializer serializer = new JRepSerializer();
        String json = serializer.toJson(BasicEntity.COMPLETE, entity);

        JRepDeserializer deserializer = new JRepDeserializer();
        BasicEntity fromJson = deserializer.readObject(BasicEntity.COMPLETE, json).build();

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
