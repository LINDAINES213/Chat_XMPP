package com.proyecto1redes.demoproyecto1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.stereotype.Controller;

/*
 * Main class
 */

@Controller
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Demoproyecto1Application {

    public static void main(String[] args) {
        SpringApplication.run( Demoproyecto1Application.class, args);
    }

}