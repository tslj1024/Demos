package top.tslj.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.tslj.dsb2s.IService;
// import top.tslj.dsb2s.EnableDemo;

@SpringBootApplication
@RestController
// @EnableDemo
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        SpringApplication.run(Main.class, args);
        System.out.println("Hello world!");
    }

    @Autowired
    private IService iService;

    @GetMapping("/hello")
    public String hello() {
        return iService.showTime();
    }
}