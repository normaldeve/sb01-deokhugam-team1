package com.codeit.duckhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DuckhuApplication {

  public static void main(String[] args) {
    SpringApplication.run(DuckhuApplication.class, args);
  }
}
