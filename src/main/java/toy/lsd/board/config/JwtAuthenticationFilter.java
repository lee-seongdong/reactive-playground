package toy.lsd.board.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import toy.lsd.board.member.entity.Member;
import toy.lsd.board.member.service.MemberService;

/**
 * 🛡️ JWT 토큰을 검증하고 Member 객체를 인증 컨텍스트에 설정하는 필터
 * 
 * 수정된 흐름:
 * 1. JWT 토큰에서 username 추출
 * 2. username으로 Member 객체 조회
 * 3. Member 객체를 principal로 하는 Authentication 생성
 * 4. SecurityContext에 저장
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;
    private final MemberService memberService;  // Member 조회용 추가

    public JwtAuthenticationFilter(JwtUtil jwtUtil, MemberService memberService) {
        this.jwtUtil = jwtUtil;
        this.memberService = memberService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractTokenFromRequest(exchange);
        
        // 🎫 토큰이 있고 유효한 경우에만 인증 정보 설정
        if (token != null && jwtUtil.validateToken(token)) {
            return authenticateWithToken(token)
                .flatMap(auth -> chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                )
                .onErrorResume(error -> {
                    // Member 조회 실패 시 인증 없이 진행
                    System.err.println("Member 조회 실패: " + error.getMessage());
                    return chain.filter(exchange);
                });
        }
        
        // 토큰이 없거나 유효하지 않으면 그냥 다음 필터로
        return chain.filter(exchange);
    }

    /**
     * 🔍 HTTP 요청에서 JWT 토큰 추출
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     */
    private String extractTokenFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // "Bearer " 제거
        }
        
        return null;
    }

    /**
     * 🔐 JWT 토큰으로부터 Member 객체를 조회하여 인증 객체 생성
     * 
     * 핵심: Member 객체를 principal로 설정하여 @AuthenticationPrincipal이 작동하도록 함
     */
    private Mono<Authentication> authenticateWithToken(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        
        // 1. JWT에서 username 추출
        // 2. username으로 Member 객체 조회
        // 3. Member 객체를 principal로 하는 Authentication 생성
        return memberService.findMemberById(username)
            .map(member -> {
                System.out.println("🎯 JWT 인증 성공: " + member.getName() + " (" + member.getId() + ")");
                
                // Member 객체를 principal로 설정 (핵심!)
                return new UsernamePasswordAuthenticationToken(
                    member,           // ← Member 객체를 principal로 설정
                    null,             // credentials (JWT에서는 불필요)
                    member.getAuthorities()  // Member에서 권한 직접 가져오기
                );
            });
    }
} 