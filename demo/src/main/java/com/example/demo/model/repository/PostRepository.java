package com.example.demo.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.demo.model.domain.Member;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 로그인한 회원이 작성한 글만 최신순으로 조회
    List<Post> findByAuthor_UsernameOrderByIdDesc(String username);

    // [10주차 연습문제 ①] 내가 쓴 글 페이징
    Page<Post> findByAuthor_Username(String username, Pageable pageable);

    // [10주차] 단계 2 : 제목 또는 내용에 검색어가 들어간 글
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    // [10주차 연습문제 ②] 작성자 이름으로 검색
    @Query("SELECT p FROM Post p WHERE p.author.name LIKE %:name%")
    Page<Post> searchByWriter(@Param("name") String name, Pageable pageable);

    boolean existsByAuthor(Member author);

}