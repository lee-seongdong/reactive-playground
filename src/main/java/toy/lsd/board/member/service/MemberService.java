package toy.lsd.board.member.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import toy.lsd.board.member.entity.Member;
import toy.lsd.board.member.repository.MemberRepository;

/**
 * 👤 회원 서비스 - 로그인/회원가입 기능만 제공
 * ReactiveUserDetailsService 구현
 */
@Service
public class MemberService implements ReactiveUserDetailsService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 🔍 Spring Security용 사용자 조회
     * 로그인 시 이 메소드가 자동으로 호출됩니다.
     * 
     * @param username 로그인 ID (Member.id)
     * @return UserDetails
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return memberRepository.findById(username)  // id(로그인ID)로 조회
            .cast(UserDetails.class)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }
    
    /**
     * 👤 Member 객체로 조회 (컨트롤러에서 사용)
     */
    public Mono<Member> findMemberById(String id) {
        return memberRepository.findById(id)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + id)));
    }
    
    /**
     * 📝 회원 가입
     */
    public Mono<Member> createMember(String id, String name, String password, String role) {
        Member member = new Member(
            id,    // 로그인 ID
            name,  // 사용자 실제 이름
            passwordEncoder.encode(password), // 비밀번호 암호화
            role
        );
        return memberRepository.save(member);
    }
} 