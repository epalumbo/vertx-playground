package com.waes.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

class WaesHero {

    @JsonProperty("_id")
    public String id;

    public String name;

}
