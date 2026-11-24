
package com.example.demo.model.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.PostForm;
import com.example.demo.model.repository.MemberRepository;
import com.example.demo.model.repository.PostRepository;
import com.example.demo.model.repository.EnrollmentRepository;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private FileStore fileStore;

    // 전체 게시글 조회
    public List<Post> findAll() {
        return postRepository.findAll(
                Sort.by(Sort.Direction.DESC, "id"));
    }

    // 내가 쓴 글 조회
    public List<Post> findMyPosts(String username) {
        return postRepository.findByAuthor_UsernameOrderByIdDesc(username);
    }

    // 내가 쓴 글 페이징
    public Page<Post> findMyPostsPage(String username, int page) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id"));

        return postRepository.findByAuthor_Username(username, pageable);
    }

    // 게시글 작성
    public Post write(PostForm form, String username) {
        validate(form);

        Member author = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        Post post = new Post();
        post.setTitle(form.getTitle().trim());
        post.setContent(form.getContent());
        post.setAuthor(author);
        post.setCreatedAt(LocalDateTime.now());
        post.setCapacity(form.getCapacity());
        post.setPosterFile(fileStore.save(form.getPoster()));

        return postRepository.save(post);
    }

    // 게시글 입력값 검증
    private void validate(PostForm form) {
        if (form.getCapacity() != null
                && (form.getCapacity() < 1 || form.getCapacity() > 100)) {
            throw new IllegalArgumentException("모집 인원은 1~100명으로 입력하세요.");
        }

        if (form.getTitle() == null || form.getTitle().isBlank()
                || form.getContent() == null || form.getContent().isBlank()) {
            throw new IllegalArgumentException("제목과 내용을 모두 입력하세요.");
        }

        if (form.getTitle().trim().length() > 100) {
            throw new IllegalArgumentException("제목은 100자까지 입력할 수 있습니다.");
        }
    }

    // 페이지당 게시글 수
    public static final int PAGE_SIZE = 10;

    // 게시글 검색 및 페이징
    public Page<Post> findPage(String keyword, String type, int page) {

        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id"));

        // 검색어가 없으면 전체 게시글 조회
        if (keyword == null || keyword.isBlank()) {
            return postRepository.findAll(pageable);
        }

        keyword = keyword.trim();

        // 작성자 이름으로 검색
        if ("writer".equals(type)) {
            return postRepository.searchByWriter(keyword, pageable);
        }

        // 제목 또는 내용으로 검색
        return postRepository.search(keyword, pageable);
    }

    // 기존 컨트롤러 호출과의 호환을 위한 메서드
    public Page<Post> findPage(String keyword, int page) {
        return findPage(keyword, "all", page);
    }

    // ID 목록으로 게시글 조회
    public List<Post> findAllById(Collection<Long> ids) {
        return postRepository.findAllById(ids);
    }

    // ID로 게시글 조회
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "글이 존재하지 않습니다."));
    }

    // 게시글 상세 조회 및 조회수 증가
    public Post read(Long id) {
        Post post = findById(id);
        post.setViewCount(post.getViewCount() + 1);

        return postRepository.save(post);
    }

    // 수정 전 작성자 확인
    public Post findForEdit(Long id) {
        Post post = findById(id);
        checkWriter(post);

        return post;
    }

    // 게시글 수정
    public Post edit(Long id, PostForm form) {
        Post post = findForEdit(id);
        validate(form);

        post.setTitle(form.getTitle().trim());
        post.setContent(form.getContent());
        post.setUpdatedAt(LocalDateTime.now());

        return postRepository.save(post);
    }

    // [11주차 연습문제 ②] 게시글 삭제
    public void delete(Long id) {
        Post post = findById(id);

        // 관리자 또는 작성자만 삭제 가능
        if (!isAdmin()) {
            checkWriter(post);
        }

        // 신청 내역이 있으면 삭제할 수 없다.
        if (enrollmentRepository.existsByPost(post)) {
            throw new IllegalStateException(
                    "신청자가 있는 모집글은 삭제할 수 없습니다.");
        }

        // 신청 내역이 없으면 포스터 파일 삭제
        fileStore.delete(post.getPosterFile());

        // 게시글 삭제
        postRepository.delete(post);
    }

    // 게시글 작성자 확인
    private void checkWriter(Post post) {
        String loginUsername = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        if (!post.getAuthor().getUsername().equals(loginUsername)) {
            throw new AccessDeniedException("본인이 쓴 글만 수정·삭제할 수 있습니다.");
        }
    }

    // 관리자 여부 확인
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();

        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
