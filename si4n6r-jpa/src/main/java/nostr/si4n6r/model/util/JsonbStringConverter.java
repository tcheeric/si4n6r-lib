package nostr.si4n6r.model.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.java.Log;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

@Converter
@Log
@Deprecated(forRemoval = true)
public class JsonbStringConverter implements AttributeConverter<String, PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            PGobject po = new PGobject();
            po.setType("jsonb");
            po.setValue(attribute);
            return po;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to convert string to PGobject: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertToEntityAttribute(PGobject dbData) {
        if (dbData == null) {
            return null;
        }

        return dbData.getValue();
    }
}