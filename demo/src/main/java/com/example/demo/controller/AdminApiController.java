package com.example.demo.controller;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.dto.MemberResponse;
import com.example.demo.model.dto.RoleRequest;
import com.example.demo.model.service.MemberService;

@RestController // 관리자 회원 관리 API (JSON)
@RequestMapping("/api/admin/members")
public class AdminApiController {

    @Autowired
    MemberService memberService;

    // 회원 목록 및 검색
    // GET /api/admin/members
    // GET /api/admin/members?keyword=검색어
    @GetMapping
    public List<MemberResponse> list(
            @RequestParam(required = false) String keyword) {

        List<Member> members = memberService.searchMembers(keyword);

        return members.stream()
                .map(MemberResponse::from)
                .toList();
    }

    // 회원 한 명 조회
    // GET /api/admin/members/3
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> get(
            @PathVariable Long id) {

        return memberService.findById(id)
                .map(member ->
                        ResponseEntity.ok(MemberResponse.from(member)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 회원 등록
    // POST /api/admin/members
    @PostMapping
    public ResponseEntity<MemberResponse> create(
            @RequestBody MemberForm form) {

        Member saved = memberService.signup(form);

        return ResponseEntity
                .created(URI.create("/api/admin/members/" + saved.getId()))
                .body(MemberResponse.from(saved));
    }

    // 회원 권한 변경
    // PATCH /api/admin/members/3/role
    @PatchMapping("/{id}/role")
    public ResponseEntity<MemberResponse> changeRole(
            @PathVariable Long id,
            @RequestBody RoleRequest request) {

        Member member = memberService.changeRole(id, request.role());

        return ResponseEntity.ok(MemberResponse.from(member));
    }

    // 회원 삭제
    // DELETE /api/admin/members/3
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        memberService.deleteMember(id);

        return ResponseEntity.noContent().build();
    }
}