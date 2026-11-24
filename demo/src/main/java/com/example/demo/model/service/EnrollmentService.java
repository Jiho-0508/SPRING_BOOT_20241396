
package com.example.demo.model.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.example.demo.model.domain.Enrollment;
import com.example.demo.model.domain.Member;
import com.example.demo.model.domain.Post;
import com.example.demo.model.repository.EnrollmentRepository;
import com.example.demo.model.repository.MemberRepository;
import com.example.demo.model.repository.PostRepository;

@Service // [11주차] 단계 4 : 신청 서비스
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    // 내 신청 내역
    public List<Enrollment> findMine(String username) {
        return enrollmentRepository.findByMember_UsernameOrderByIdDesc(username);
    }

    // [11주차 연습문제 ①] 신청 취소
    @Transactional
    public void cancel(Long enrollmentId, String username) {

        // 신청 내역 조회
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "신청 내역을 찾을 수 없습니다."));

        // 본인의 신청인지 확인
        if (!enrollment.getMember().getUsername().equals(username)) {
            throw new AccessDeniedException("본인의 신청 내역만 취소할 수 있습니다.");
        }

        // 신청한 모집글 가져오기
        Post post = enrollment.getPost();

        // 신청 내역 삭제
        enrollmentRepository.delete(enrollment);

        // 모집글의 신청 인원 1명 감소
        // @Transactional에 의한 변경 감지로 자동 반영
        post.setAppliedCount(post.getAppliedCount() - 1);
    }

    // 단계 4 : 바구니의 모집글을 한 번에 신청 확정
    @Transactional // 단계 5 : 하나라도 실패하면 모두 취소 (롤백)
    public int confirm(String username, Collection<Long> postIds) {

        if (postIds.isEmpty()) {
            throw new IllegalStateException("바구니가 비어 있습니다.");
        }

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));

        for (Long postId : postIds) {

            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new IllegalStateException("삭제된 모집글이 바구니에 있습니다."));

            if (enrollmentRepository.existsByMemberAndPost(member, post)) {
                throw new IllegalStateException("이미 신청한 모집글입니다 : " + post.getTitle());
            }

            if (post.isFull()) {
                throw new IllegalStateException("정원이 마감되었습니다 : " + post.getTitle());
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setMember(member);
            enrollment.setPost(post);
            enrollment.setCreatedAt(LocalDateTime.now());

            enrollmentRepository.save(enrollment); // INSERT 신청 내역

            post.setAppliedCount(post.getAppliedCount() + 1);
            postRepository.save(post); // UPDATE 신청 인원 + 1
        }

        return postIds.size();
    }
}
