package sa.gov.saip.workflow.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

public record ProcessVariableDto(
        Object value,
        String type
) {
    public static ProcessVariableDto from(Object value) {
        return new ProcessVariableDto(value, inferType(value));
    }

    private static String inferType(Object value) {
        if (value instanceof Boolean) {
            return "Boolean";
        }
        if (value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return "Integer";
        }
        if (value instanceof Long || value instanceof BigInteger) {
            return "Long";
        }
        if (value instanceof Float || value instanceof Double || value instanceof BigDecimal) {
            return "Double";
        }
        return "String";
    }
}
