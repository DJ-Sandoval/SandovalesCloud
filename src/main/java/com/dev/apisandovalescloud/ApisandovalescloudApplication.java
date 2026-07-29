package com.dev.apisandovalescloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApisandovalescloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApisandovalescloudApplication.class, args);
	}

}
