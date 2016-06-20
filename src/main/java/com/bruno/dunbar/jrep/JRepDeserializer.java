package com.bruno.dunbar.jrep;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.BiConsumer;

public class JRepDeserializer {

    private ObjectMapper objectMapper;

    public JRepDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JRepDeserializer() {
        this(new ObjectMapper());
    }

    public <C, B> B readObject(JRepConfiguration<C, B> configuration, String json) throws IOException {
        JsonNode node = objectMapper.getFactory().createParser(json).readValueAsTree();
        return readObject(configuration, node);

    }

    private <C, B> B readObject(JRepConfiguration<C, B> configuration, JsonNode node) {
        if (node.isNull()) {
            return null;
        }

        if (!node.isObject()) {
            throw new IllegalArgumentException("invalid object");
        }

        B builder = configuration.getIdentifier()
                .map(identifier -> readIdentifier(identifier, node))
                .map(identifier -> (C) null) //TODO: find object by id
                .map(object -> configuration.getFromMethod().map(from -> from.apply(object)).orElse(null))
                .orElseGet(() -> configuration.getCreateMethod().get());


        node.fields().forEachRemaining(entry -> {
            String propertyName = entry.getKey();
            JRepProperty<C, B, ?, ?> property = configuration.getProperties().get(propertyName);
            if (property == null) {
                throw new IllegalArgumentException("property " + propertyName + " was not found in " + configuration.getObjectClass() + " configuration");
            }

            readProperty(property, builder, entry.getValue());
        });

        return builder;
    }

    private <C, B> void readProperty(JRepProperty<C, B, ?, ?> property, B builder, JsonNode node) {
        Object value = readProperty(property, node);
        property.getSetterMethod().ifPresent(setter -> ((BiConsumer) setter).accept(builder, value));
    }

    private <C, B> Object readProperty(JRepProperty<C, B, ?, ?> property, JsonNode node) {
        if (property.getConfiguration() != null) {
            return readObject(property.getConfiguration(), node);
        }
        return readSimpleValue(property, node);
    }

    private <C, B> Object readSimpleValue(JRepProperty<C, B, ?, ?> property, JsonNode node) {
        try {
            return node.traverse(objectMapper).readValueAs(property.getToType());
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to read property value", e);
        }
    }

    private <C, B> Object readIdentifier(JRepProperty<C, B, ?, ?> property, JsonNode node) {
        if (!node.has(property.getName())) {
            return null;
        }

        return readProperty(property, node.get(property.getName()));
    }
}
