package com.example.recipes.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

@Converter(autoApply = false)
public class JsonbConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            PGobject jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(attribute);
            return jsonb.toString();
        } catch (Exception e) {
            throw new RuntimeException("JSONB conversion error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return dbData;
    }
}