package com.example.demo.APItest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

public class test {
	
	// 단기예보
	@RestController
	@RequestMapping("/test/weather1")
	@RequiredArgsConstructor
	public class testWeather {
		
		
		
		
	}
	
	
	// 중기예보
	@RestController
	@RequestMapping("/test/weather2")
	@RequiredArgsConstructor
	public class testCafe {
		
		
		
		
	}
}