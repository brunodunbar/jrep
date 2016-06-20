package com.bruno.dunbar.jrep;

import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JRepConfiguration<C, B> {

    private final Class<C> objectClass;

    private final Class<B> builderClass;

    private final Map<String, JRepProperty<C, B, ?, ?>> properties;

    private JRepProperty<C, B, ?, ?> identifier;

    private Supplier<B> createMethod;

    private Function<C, B> fromMethod;

    private JRepConfiguration(Class<C> objectClass, Class<B> builderClass) {

        Objects.requireNonNull(objectClass, "object class is required");
        Objects.requireNonNull(builderClass, "builder class is required");

        this.objectClass = objectClass;
        this.builderClass = builderClass;
        this.properties = new HashMap<>();
    }

    public Class<C> getObjectClass() {
        return objectClass;
    }

    public Class<B> getBuilderClass() {
        return builderClass;
    }

    public Map<String, JRepProperty<C, B, ?, ?>> getProperties() {
        return properties;
    }

    public Optional<JRepProperty<C, B, ?, ?>> getIdentifier() {
        return Optional.ofNullable(identifier);
    }

    public Supplier<B> getCreateMethod() {
        return createMethod;
    }

    public Optional<Function<C, B>> getFromMethod() {
        return Optional.ofNullable(fromMethod);
    }

    public static class Builder<C, B> {

        private final JRepConfiguration<C, B> configuration;

        private Builder(Class<C> objectClass, Class<B> builderClass) {
            configuration = new JRepConfiguration<>(objectClass, builderClass);
        }

        public static <T, B> Builder<T, B> create(Class<T> objectClass, Class<B> builderClass) {
            return new Builder<>(objectClass, builderClass);
        }

        public <F> Builder<C, B> addProperty(Class<F> type, Consumer<JRepProperty.Builder<C, B, F, F>> consumer) {
            return addProperty(type, type, consumer);
        }

        public <F, T> Builder<C, B> addProperty(Class<F> fromType, Class<T> toType, Consumer<JRepProperty.Builder<C, B, F, T>> consumer) {
            JRepProperty.Builder<C, B, F, T> propertyBuilder = new JRepProperty.Builder<>(configuration.objectClass, configuration.builderClass, fromType, toType);
            consumer.accept(propertyBuilder);
            JRepProperty<C, B, F, T> property = propertyBuilder.build();
            configuration.properties.put(property.getName(), property);

            if (property.isIdentifier()) {
                configuration.identifier = property;
            }

            return this;
        }

        public Builder createMethod(Supplier<B> createMethod) {
            configuration.createMethod = createMethod;
            return this;
        }

        public Builder fromMethod(Function<C, B> fromMethod) {
            configuration.fromMethod = fromMethod;
            return this;
        }

        public JRepConfiguration<C, B> build() {
            if (configuration.createMethod == null) {
                Method method = MethodUtils.getMatchingAccessibleMethod(configuration.builderClass, "create");
                if (method != null) {
                    configuration.createMethod = () -> {
                        try {
                            return (B) method.invoke(null);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("unable to invoke builder create method", e);
                        }
                    };
                }
            }

            if (configuration.createMethod == null) {
                Method method = MethodUtils.getMatchingAccessibleMethod(configuration.builderClass, "from", configuration.objectClass);
                if (method != null) {
                    configuration.fromMethod = (value) -> {
                        try {
                            return (B) method.invoke(null, value);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("unable to invoke builder from method", e);
                        }
                    };
                } else {
                    throw new RuntimeException("builder class should have a static create method with no args");
                }
            }

            return configuration;
        }
    }

}
