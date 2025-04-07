package edu.cit.spot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "edu.cit.spot.repository")
@EntityScan(basePackages = "edu.cit.spot.entity")
@ComponentScan(basePackages = "edu.cit.spot")
public class SpotApplication {
    private static final Logger log = LoggerFactory.getLogger(SpotApplication.class);

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(SpotApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logEndpoints() {
        log.info("==============================");
        log.info("Registered endpoints:");
        requestMappingHandlerMapping.getHandlerMethods().forEach((key, value) -> {
            log.info("{}", key);
            log.info("=> {}", value);
            log.info("=> Class: {}", value.getBeanType().getName());
            log.info("---");
        });
        log.info("==============================");

        // Log all beans
        log.info("Registered beans:");
        Arrays.stream(applicationContext.getBeanDefinitionNames())
            .filter(name -> name.toLowerCase().contains("controller") || 
                           name.toLowerCase().contains("service") || 
                           name.toLowerCase().contains("repository"))
            .forEach(name -> {
                log.info("Bean: {} => {}", name, applicationContext.getBean(name).getClass().getName());
            });
        log.info("==============================");
    }
}
