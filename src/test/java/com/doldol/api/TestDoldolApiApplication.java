package com.doldol.api;

import org.springframework.boot.SpringApplication;

public class TestDoldolApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(DoldolApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
