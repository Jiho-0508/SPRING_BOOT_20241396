package com.example.demo.controller;

import java.util.List; // 1. List 임포트 추가

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.model.domain.TestDB;
import com.example.demo.model.service.TestService;

@Controller  
public class DemoController {
    @Autowired
    TestService testService; // DemoController 클래스 아래 객체 주입

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/hello") // 공백 제거 완료
    public String hello(Model model) {
        model.addAttribute("data", "반갑습니다."); 
        return "hello"; // hello.html 연결
    }

    // --- 여기부터 hello2 메서드 추가 ---
    @GetMapping("/hello2")
    public String hello2(Model model) {
        model.addAttribute("title", "김지호님.");
        model.addAttribute("author", "반갑습니다.");
        model.addAttribute("framework", "오늘.");
        model.addAttribute("viewEngine", "날씨는.");
        model.addAttribute("description", "매우 화창합니다.");
        
        return "hello2"; // hello2.html 연결
    }

    @GetMapping("/testdb")
    public String getAllTestDBs(Model model) {
        List<TestDB> testList = testService.findAll();
        
        model.addAttribute("testList", testList);
        
        System.out.println("조회된 데이터 수 : " + testList.size());
        
        return "testdb"; // testdb.html 파일을 리턴
    }
}