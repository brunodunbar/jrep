package com.bruno.dunbar.jrep;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Function;

public class JRepSerializer {

    public JRepSerializer() {
    }

    public <C, B> String toJson(JRepConfiguration<C, B> configuration, C object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        toStream(configuration, object, outputStream);
        return outputStream.toString("UTF-8");
    }

    public <C, B> void toStream(JRepConfiguration<C, B> configuration, C object, OutputStream outputStream) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream);

        writeObject(configuration, object, generator);

        generator.close();
    }

    private <C, B> void writeObject(JRepConfiguration<C, B> configuration, C object, JsonGenerator generator) {
        try {
            generator.writeStartObject();
            configuration.getProperties().values().stream()
                    .forEach(property -> {
                        writeProperty(property, object, generator);
                    });
            generator.writeEndObject();
        } catch (IOException e) {
            throw new RuntimeException("unable to write object");
        }
    }

    private <C> void writeProperty(JRepProperty property, C object, JsonGenerator generator) {
        Optional<Function<C, ?>> getterMethod = property.getGetterMethod();
        getterMethod.ifPresent(getter -> {
            try {
                generator.writeFieldName(property.getName());
                Object value = getter.apply(object);

                if (property.getConfiguration() != null) {
                    writeObject(property.getConfiguration(), value, generator);
                } else {
                    generator.writeObject(value);
                }
            } catch (IOException e) {
                throw new RuntimeException("unable to write property");
            }
        });
    }


}
