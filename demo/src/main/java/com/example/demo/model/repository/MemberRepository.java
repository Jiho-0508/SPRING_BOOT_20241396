package com.example.demo.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.domain.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 아이디로 회원 조회
    Optional<Member> findByUsername(String username);

    // 아이디 중복 확인
    boolean existsByUsername(String username);

    // 아이디 또는 이름에 검색어가 포함된 회원 조회
    List<Member> findByUsernameContainingOrNameContaining(
            String username, String name
    );
}