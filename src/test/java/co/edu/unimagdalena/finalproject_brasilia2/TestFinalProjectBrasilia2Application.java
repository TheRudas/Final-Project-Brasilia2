package co.edu.unimagdalena.finalproject_brasilia2;

import org.springframework.boot.SpringApplication;

public class TestFinalProjectBrasilia2Application {

    public static void main(String[] args) {
        SpringApplication.from(FinalProjectBrasilia2Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
