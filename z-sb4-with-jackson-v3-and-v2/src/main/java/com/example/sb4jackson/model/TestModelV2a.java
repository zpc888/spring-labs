package com.example.sb4jackson.model;

import com.example.sb4jackson.serialization.v2.V2DateDeserializer;
import com.example.sb4jackson.serialization.v2.V2DateSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;

public class TestModelV2a {

    private String stringField;

    @JsonProperty("integerField")
    private Integer intField;

    @JsonSerialize(using = V2DateSerializer.class)
    @JsonDeserialize(using = V2DateDeserializer.class)
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
