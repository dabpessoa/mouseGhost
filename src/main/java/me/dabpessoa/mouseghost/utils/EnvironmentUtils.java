package me.dabpessoa.mouseghost.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

public final class EnvironmentUtils {

	private EnvironmentUtils() {
		// Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
	}
	
	public static Map<String, Object> getPropertiesMap(Environment environment) {
		Map<String, Object> map = new HashMap<>();
		for (Iterator<PropertySource<?>> it = ((AbstractEnvironment) environment).getPropertySources().iterator(); it.hasNext();) {
			PropertySource<?> propertySource = it.next();
			if (propertySource instanceof MapPropertySource) {
				map.putAll(((MapPropertySource) propertySource).getSource());
			}
		}
		return map;
	}
	
	public static Properties getProperties(Environment environment) {
		Properties props = new Properties();
		
		MutablePropertySources propSrcs = ((AbstractEnvironment) environment).getPropertySources();
		StreamSupport.stream(propSrcs.spliterator(), false)
			.filter(EnumerablePropertySource.class::isInstance)
			.map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
			.flatMap(Arrays::<String>stream)
			.forEach(propName -> props.setProperty(propName, environment.getProperty(propName)));
		
		return props;
	}
	
}
