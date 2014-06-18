package com.handwin.util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * User: roger
 * Date: 13-12-2 下午5:56
 */
public class Jackson {
    private final static Logger LOG = LoggerFactory.getLogger(Jackson.class);
    private static ObjectMapper mapper;
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /*public static ObjectMapper newObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        return mapper;
    }*/

    public static ObjectMapper mapper() {
        return mapper;
    }

    public static String writeValueAsString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            return null;
        }
    }

    public static JsonNode readTree(String value) {
        try {
            return mapper.readTree(value);
        } catch (IOException e) {
            LOG.error("readTree", e);
            return null;
        }
    }

    public static JsonNode readTree(byte[] data) {
        try {
            return mapper.readTree(data);
        } catch (IOException e) {
            LOG.error("readTree", e);
            return null;
        }
    }


    public static <T> T readValue(JsonNode root, Class<T> clazz) {
        try {
            return mapper.readValue(root.traverse(), clazz);
        }catch (Exception e) {
            LOG.error("readValue", e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        return clazz.equals(String.class) ? (T) json : mapper.readValue(json, clazz);
    }

    public static <T> T fromJson(String json, TypeReference<?> typeReference) throws IOException {
        return (T) (typeReference.getType().equals(String.class) ? json : mapper.readValue(json, typeReference));
    }

    public static <T> T fromJson(JsonNode json, TypeReference<?> typeReference) throws IOException {
        return (T) (typeReference.getType().equals(String.class) ? json : mapper.readValue(json.traverse(), typeReference));
    }

}
