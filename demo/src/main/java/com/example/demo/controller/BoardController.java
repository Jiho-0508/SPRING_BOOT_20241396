
package com.example.demo.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.PostForm;
import com.example.demo.model.service.CommentService;
import com.example.demo.model.service.PostService;

@Controller // 게시판 화면 컨트롤러
@RequestMapping("/board") // 모든 URL 앞에 /board
public class BoardController {

    @Autowired
    PostService postService;

    @Autowired
    CommentService commentService;

    // 게시글 목록 및 검색 : GET /board?page=0&keyword=검색어&type=all
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String type,
            Model model) {

        // 검색 조건에 따라 게시글 조회
        Page<Post> posts = postService.findPage(keyword, type, page);

        // 현재 페이지의 게시글 목록
        model.addAttribute("posts", posts.getContent());

        // 페이징 정보
        model.addAttribute("page", posts);

        // 검색어와 검색 조건 유지
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);

        return "board/list";
    }

    // 내가 쓴 글 페이징 : GET /board/my?page=0 (로그인 필요)
    @GetMapping("/my")
    public String myPosts(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Post> posts = postService.findMyPostsPage(
                principal.getName(), page);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("pageUrl", "/board/my");
        model.addAttribute("listTitle", "내가 쓴 글");

        return "board/list";
    }

    // 글쓰기 화면 : GET /board/write (로그인 필요)
    @GetMapping("/write")
    public String writeForm() {
        return "board/form";
    }

    // 글쓰기 처리 : POST /board/write
    @PostMapping("/write")
    public String write(
            PostForm postForm,
            Principal principal,
            Model model) {

        try {
            Post post = postService.write(
                    postForm, principal.getName());

            return "redirect:/board/" + post.getId();

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "board/form";
        }
    }

    // 상세 : GET /board/3 (누구나)
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        Post post = postService.read(id);

        model.addAttribute("post", post);
        model.addAttribute(
                "comments",
                commentService.findByPost(post));

        return "board/detail";
    }

    // 수정 화면 : GET /board/3/edit
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        Post post = postService.findForEdit(id);

        PostForm postForm = new PostForm();
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());

        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", id);

        return "board/form";
    }

    // 수정 처리 : POST /board/3/edit
    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            PostForm postForm,
            Model model) {

        try {
            postService.edit(id, postForm);
            return "redirect:/board/" + id;

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("postId", id);

            return "board/form";
        }
    }

    // 삭제 : POST /board/3/delete
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {

        postService.delete(id);

        return "redirect:/board?deleted";
    }
}
