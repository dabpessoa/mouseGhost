package me.dabpessoa.mouseghost.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class SpringDefaultContextListener implements ApplicationListener<ContextRefreshedEvent> {
	
	private static Integer contextCount = 0;
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		log.info("Inicializando Spring...");
		verificarQuantidadeDeContextos();
	}

	private static void verificarQuantidadeDeContextos() {
		contextCount++;
		if (contextCount > 1) {
			String message = "Houve uma tentativa de se criar um novo contexto do spring, porém já existe um ativo. Favor verificar o código pois esse erro pode causar instabilidades.";
			log.error(message);
			throw new RuntimeException(message);
		}
	}

}