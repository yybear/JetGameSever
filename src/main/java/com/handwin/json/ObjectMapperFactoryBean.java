package com.handwin.json;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.beans.factory.FactoryBean;

import java.io.IOException;
import java.util.HashMap;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-17 下午7:48
 */
public class ObjectMapperFactoryBean implements FactoryBean<ObjectMapper> {
    private ObjectMapper objectMapper = null;

    public ObjectMapperFactoryBean() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        this.objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondary = new JaxbAnnotationIntrospector();
        AnnotationIntrospector pair = new AnnotationIntrospector.Pair(primary, secondary);

        this.objectMapper.setAnnotationIntrospector(pair);
    }

    @Override
    public ObjectMapper getObject() throws Exception {
        return objectMapper;
    }

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
    public static void main(String[] args) throws IOException {
        ObjectMapperFactoryBean bean = new ObjectMapperFactoryBean();
        System.out.println(bean.objectMapper.writeValueAsString(new HashMap(){{put("1",1);put("2",2);}}));
    }
}
