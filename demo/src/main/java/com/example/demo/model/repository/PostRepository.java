package com.example.demo.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.Post;

import java.util.List;

@Repository // 게시글 리포지토리 : save, findAll, findById, delete 는 이미 있다
public interface PostRepository extends JpaRepository<Post, Long> {

    // 로그인한 회원이 작성한 글만 최신순으로 조회
    List<Post> findByAuthor_UsernameOrderByIdDesc(String username);
}