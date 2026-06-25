package com.example.sb4jackson.model;

import com.example.sb4jackson.serialization.v3.V3DateDeserializer;
import com.example.sb4jackson.serialization.v3.V3DateSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

public class TestModelV3a {

    private String stringField;

    @JsonProperty("integerField")
    private Integer intField;

    @JsonSerialize(using = V3DateSerializer.class)
    @JsonDeserialize(using = V3DateDeserializer.class)
    private Date dateFiled;

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public Integer getIntField() {
        return intField;
    }

    public void setIntField(Integer intField) {
        this.intField = intField;
    }

    public Date getDateFiled() {
        return dateFiled;
    }

    public void setDateFiled(Date dateFiled) {
        this.dateFiled = dateFiled;
    }
}
