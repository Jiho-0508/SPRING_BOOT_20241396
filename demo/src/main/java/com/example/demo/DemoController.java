package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // 컨트롤러 어노테이션 명시 
public class DemoController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/hello") // 공백 제거 완료
    public String hello(Model model) {
        model.addAttribute("data", "반갑습니다."); // model 설정
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
}