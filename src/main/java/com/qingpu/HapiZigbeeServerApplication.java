package com.qingpu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class HapiZigbeeServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HapiZigbeeServerApplication.class, args);
	}

}
