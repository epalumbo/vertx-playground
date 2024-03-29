package com.waes.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

class WaesHero {

    @JsonProperty("_id")
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
