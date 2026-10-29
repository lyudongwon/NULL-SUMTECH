package com.nullteam.foodcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nullteam.foodcheck.global.config.DatabaseUrl;

@SpringBootApplication
public class FoodcheckApplication {

	public static void main(String[] args) {
		DatabaseUrl.apply(System.getenv());
		SpringApplication.run(FoodcheckApplication.class, args);
	}

}
