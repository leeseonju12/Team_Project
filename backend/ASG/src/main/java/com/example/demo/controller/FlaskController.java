//package com.example.demo.controller;
//
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.demo.dto.EchoRequest;
//import com.example.demo.dto.FlaskHelloResponse;
//import com.example.demo.service.FlaskService;
//
//@RestController
//@RequestMapping("/test")
//public class FlaskController {
//
//    @Autowired
//    private FlaskService flaskService;
//
//    // ✅ GET 테스트
//    // http://localhost:8080/test/hello
//    @GetMapping("/hello")
//    public FlaskHelloResponse testGet() {
//        return flaskService.getHello();
//    }
//
//    // ✅ POST 테스트
//    // http://localhost:8080/test/echo
//    @PostMapping("/echo")
//    public Map testPost(@RequestBody EchoRequest request) {
//        return flaskService.postEcho(request);
//    }
//}