package com.bruno.dunbar.jrep;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JRepProperty<C, B, F, T> {

    private final Class<C> objectClass;

    private final Class<B> builderClass;

    private final Class<F> fromType;

    private final Class<T> toType;

    private JRepConfiguration<F, ?> configuration;

    private String name;

    private boolean identifier;

    private BiConsumer<B, F> setterMethod;

    private Function<C, F> getterMethod;

    public JRepProperty(Class<C> objectClass, Class<B> builderClass, Class<F> fromType, Class<T> toType) {
        this.objectClass = objectClass;
        this.builderClass = builderClass;
        this.fromType = fromType;
        this.toType = toType;
    }

    public String getName() {
        return name;
    }

    public Class<F> getFromType() {
        return fromType;
    }

    public Class<T> getToType() {
        return toType;
    }

    public boolean isIdentifier() {
        return identifier;
    }

    public Optional<BiConsumer<B, F>> getSetterMethod() {
        return Optional.ofNullable(setterMethod);
    }

    public Optional<Function<C, F>> getGetterMethod() {
        return Optional.ofNullable(getterMethod);
    }

    public JRepConfiguration<F, ?> getConfiguration() {
        return configuration;
    }

    public static class Builder<C, B, F, T> {

        private JRepProperty<C, B, F, T> property;

        public Builder(Class<C> objectClass, Class<B> builderClass, Class<F> fromType, Class<T> toType) {
            Objects.requireNonNull(objectClass, "object class is required");
            Objects.requireNonNull(builderClass, "builder class is required");
            Objects.requireNonNull(fromType, "from type is required");
            Objects.requireNonNull(toType, "to type is required");

            property = new JRepProperty<>(objectClass, builderClass, fromType, toType);
        }

        public Builder<C, B, F, T> name(String name) {
            property.name = name;
            return this;
        }

        public Builder<C, B, F, T> identifier() {
            property.identifier = true;
            return this;
        }

        public Builder<C, B, F, T> config(JRepConfiguration<F, ?> configuration) {
            property.configuration = configuration;
            return this;
        }

        JRepProperty<C, B, F, T> build() {
            Objects.requireNonNull(property.name, "property name is required");

            if (property.setterMethod == null) {
                Method method = MethodUtils.getMatchingAccessibleMethod(property.builderClass, property.name, property.fromType);
                if (method != null) {
                    property.setterMethod = (target, value) -> {
                        try {
                            method.invoke(target, value);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("unable to set value in builder", e);
                        }
                    };
                }
            }

            if (property.getterMethod == null) {
                Method method = MethodUtils.getMatchingAccessibleMethod(property.objectClass, "get" + StringUtils.capitalize(property.name));
                if (method != null) {
                    property.getterMethod = (target) -> {
                        try {
                            return (F) method.invoke(target);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException("unable to get value from object", e);
                        }
                    };
                }
            }

            return property;
        }
    }
}
