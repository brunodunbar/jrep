package com.bruno.dunbar.jrep;

import java.time.LocalDate;

public class BasicEntity {

    private Long id;

    private String name;

    private Boolean alive;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Boolean getAlive() {
        return alive;
    }

    public static class Builder {

        private BasicEntity entity;

        private Builder(BasicEntity entity) {
            this.entity = entity;
        }

        public static Builder create() {
            return new Builder(new BasicEntity());
        }

        public static Builder from(BasicEntity entity) {
            return new Builder(entity);
        }

        public Builder id(Long id) {
            entity.id = id;
            return this;
        }

        public Builder name(String name) {
            entity.name = name;
            return this;
        }

        public Builder alive(Boolean alive) {
            entity.alive = alive;
            return this;
        }

        public BasicEntity build() {
            return entity;
        }
    }

    public static final JRepConfiguration<BasicEntity, BasicEntity.Builder> COMPLETE = JRepConfiguration.Builder.create(BasicEntity.class, BasicEntity.Builder.class)
            .addProperty(Long.class, property -> property.name("id").identifier())
            .addProperty(String.class, property -> property.name("name"))
            .build();
}
