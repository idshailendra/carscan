package com.technospan.carscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.technospan.carscan.config.FileStorageProperties;


@SpringBootApplication
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class CarscanApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarscanApplication.class, args);
	}

}
