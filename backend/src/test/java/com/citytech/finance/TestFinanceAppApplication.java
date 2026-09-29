package com.citytech.finance;

import org.springframework.boot.SpringApplication;

public class TestFinanceAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(FinanceAppApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
