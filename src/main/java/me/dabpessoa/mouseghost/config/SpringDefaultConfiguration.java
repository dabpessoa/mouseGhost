package me.dabpessoa.mouseghost.config;

import me.dabpessoa.mouseghost.utils.EnvironmentUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.util.Map;
import java.util.Properties;

@Configuration
public class SpringDefaultConfiguration {
	
	/**
	 * Método utilitário
	 * @param environment
	 * @return
	 */
	@Bean
	@Lazy
	public Map<String, Object> getPropertiesMap(Environment environment) {
		return EnvironmentUtils.getPropertiesMap(environment);
	}
	
	/**
	 * Método utilitário
	 * @param environment
	 * @return
	 */
	@Bean
	@Lazy
	public Properties getProperties(Environment environment) {
		return EnvironmentUtils.getProperties(environment);
	}

}