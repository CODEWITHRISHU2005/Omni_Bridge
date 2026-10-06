package com.CODEWITHRISHU.Omni_Bridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OmniBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmniBridgeApplication.class, args);
	}

}