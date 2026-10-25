package com.example.demo.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.repository.MemberRepository;

@Service
public class MemberService implements UserDetailsService {

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 관리자가 부여할 수 있는 권한
    private static final List<String> ROLES =
            List.of("USER", "MANAGER", "ADMIN");

    // 회원가입
    public Member signup(MemberForm form) {

        if (isBlank(form.getUsername())
                || isBlank(form.getPassword())
                || isBlank(form.getName())) {
            throw new IllegalArgumentException(
                    "아이디, 비밀번호, 이름은 필수입니다.");
        }

        // 비밀번호 확인 검증
        if (form.getPasswordConfirm() == null
                || !form.getPassword().equals(form.getPasswordConfirm())) {
            throw new IllegalArgumentException(
                    "비밀번호가 일치하지 않습니다.");
        }

        // 아이디 중복 검사
        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 아이디입니다.");
        }

        Member member = new Member();
        member.setUsername(form.getUsername());
        member.setPassword(passwordEncoder.encode(form.getPassword()));
        member.setName(form.getName());
        member.setRole("USER"); // 기본 권한

        return memberRepository.save(member);
    }

    // 로그인 시 스프링 시큐리티가 호출
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "회원 없음 : " + username));

        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())
                .roles(member.getRole())
                .build();
    }

    // 아이디로 회원 조회
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원이 존재하지 않습니다 : " + username));
    }

    // 전체 회원 조회
    public List<Member> findAll() {
        return memberRepository.findAll(Sort.by("id"));
    }
    // 회원 검색: 아이디 또는 이름에 검색어가 포함된 회원 조회
    public List<Member> searchMembers(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
    }

    return memberRepository
            .findByUsernameContainingOrNameContaining(
                    keyword, keyword);
    }

    // 현재 로그인한 사용자의 아이디 조회
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    // 회원 권한 변경
    public Member changeRole(Long id, String role) {

        // 허용된 권한인지 검사
        if (!ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "허용되지 않는 권한입니다 : " + role);
        }

        Member member = memberRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원이 존재하지 않습니다."));

        // 관리자 본인의 권한 변경 방지
        if (member.getUsername().equals(getCurrentUsername())) {
            throw new IllegalArgumentException(
                    "관리자 본인의 권한은 변경할 수 없습니다.");
        }

        member.setRole(role);
        return memberRepository.save(member);
    }

    // 관리자만 회원 삭제 가능
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteMember(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "회원이 존재하지 않습니다."));

        // 관리자 본인 계정 삭제 방지
        if (member.getUsername().equals(getCurrentUsername())) {
            throw new IllegalArgumentException(
                    "관리자 본인 계정은 삭제할 수 없습니다.");
        }

        memberRepository.delete(member);
    }

    // ID로 회원 조회
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }
}