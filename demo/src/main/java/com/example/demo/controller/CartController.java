
package com.example.demo.controller;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.domain.Post;
import com.example.demo.model.service.EnrollmentService;
import com.example.demo.model.service.PostService;

@Controller // [11주차] 단계 3 : 신청 바구니
@RequestMapping("/cart")
public class CartController {

    @Autowired
    PostService postService;

    @Autowired
    EnrollmentService enrollmentService; // 단계 4

    @GetMapping // 바구니 화면 : GET /cart
    public String cart(HttpSession session, Principal principal, Model model) {

        model.addAttribute("cartPosts",
                postService.findAllById(getCart(session)));

        model.addAttribute("enrollments",
                enrollmentService.findMine(principal.getName()));

        return "cart";
    }

    @PostMapping("/{postId}") // 담기 : POST /cart/3
    public String add(@PathVariable Long postId, HttpSession session) {

        Post post = postService.findById(postId);

        if (!post.isRecruit()) {
            return "redirect:/board/" + postId;
        }

        getCart(session).add(postId);

        return "redirect:/cart";
    }

    @PostMapping("/{postId}/remove") // 빼기 : POST /cart/3/remove
    public String remove(@PathVariable Long postId, HttpSession session) {

        getCart(session).remove(postId);

        return "redirect:/cart";
    }

    // 세션에서 바구니 꺼내기
    @SuppressWarnings("unchecked")
    private Set<Long> getCart(HttpSession session) {

        Set<Long> cart = (Set<Long>) session.getAttribute("cart");

        if (cart == null) {
            cart = new LinkedHashSet<>();
            session.setAttribute("cart", cart);
        }

        return cart;
    }

    @PostMapping("/confirm") // 단계 4 : 신청 확정
    public String confirm(
            HttpSession session,
            Principal principal,
            RedirectAttributes redirect) {

        Set<Long> cart = getCart(session);

        try {
            int count = enrollmentService.confirm(
                    principal.getName(), cart);

            cart.clear();

            redirect.addFlashAttribute(
                    "message",
                    count + "개 모집글 신청이 확정되었습니다.");

        } catch (IllegalStateException e) {

            redirect.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart";
    }

    // [11주차 연습문제 ①] 신청 취소
    @PostMapping("/enrollments/{id}/cancel")
    public String cancel(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirect) {

        enrollmentService.cancel(id, principal.getName());

        redirect.addFlashAttribute(
                "message",
                "신청이 취소되었습니다.");

        return "redirect:/cart";
    }
}
