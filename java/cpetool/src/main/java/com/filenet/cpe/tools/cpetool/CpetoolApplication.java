package com.filenet.cpe.tools.cpetool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class CpetoolApplication {

	private static Logger log = LoggerFactory.getLogger(CpetoolApplication.class);
	
	//CommandLineRunner Bean
	@Bean
    public cpetool cpetoolStartupRunner() {
        return new cpetool();
    }
	
	public static void main(String[] args) {
		SpringApplication.run(CpetoolApplication.class, args);
	}

}
