//package com.example.demo.service;
//
//import java.util.Map;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestClient;
//import com.example.demo.dto.EchoRequest;
//import com.example.demo.dto.FlaskHelloResponse;
//
//@Service
//public class FlaskService {
//
//    private final RestClient restClient;
//
//    // ✅ @Autowired 대신 생성자 주입 (Spring 권장 방식)
//    public FlaskService(RestClient restClient) {
//        this.restClient = restClient;
//    }
//
//    // ✅ GET 요청 — getForEntity() 대체
//    public FlaskHelloResponse getHello() {
//        FlaskHelloResponse response = restClient.get()
//                .uri("/api/hello")
//                .retrieve()
//                .body(FlaskHelloResponse.class);
//
//        System.out.println("[Spring] 응답 데이터: " + response);
//        return response;
//    }
//
//    // ✅ POST 요청 — postForEntity() 대체 (HttpEntity, HttpHeaders 불필요)
//    @SuppressWarnings("unchecked")
//    public Map<String, Object> postEcho(EchoRequest requestBody) {
//        Map<String, Object> response = restClient.post()
//                .uri("/api/echo")
//                .body(requestBody)           // Content-Type은 AppConfig에서 이미 설정
//                .retrieve()
//                .body(Map.class);
//
//        System.out.println("[Spring] 응답 데이터: " + response);
//        return response;
//    }
//}