package toy.lsd.board.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import toy.lsd.board.config.JwtUtil;
import toy.lsd.board.member.service.MemberService;

import java.util.List;

/**
 * 🔐 간소화된 인증 컨트롤러 - Method Security 적용
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 🔑 로그인 - 모든 사용자 허용
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest request) {
        return memberService.findByUsername(request.id())
            .cast(UserDetails.class)
            .flatMap(userDetails -> {
                if (passwordEncoder.matches(request.password(), userDetails.getPassword())) {
                    List<String> roles = userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .toList();
                    
                    String token = jwtUtil.generateToken(userDetails.getUsername(), roles);
                    return Mono.just(ResponseEntity.ok(new LoginResponse(token, userDetails.getUsername(), roles, null)));
                } else {
                    return Mono.just(ResponseEntity.status(401)
                        .body(new LoginResponse(null, null, null, "Invalid credentials")));
                }
            })
            .switchIfEmpty(
                Mono.just(ResponseEntity.status(401)
                    .body(new LoginResponse(null, null, null, "User not found")))
            );
    }

    /**
     * 📝 회원가입 - 모든 사용자 허용
     */
    @PostMapping("/register")
    public Mono<ResponseEntity<String>> register(@RequestBody RegisterRequest request) {
        return memberService.createMember(request.id(), request.name(), request.password(), "USER")
            .map(member -> ResponseEntity.ok("회원가입 성공: " + member.getName() + " (ID: " + member.getId() + ")"))
            .onErrorReturn(ResponseEntity.badRequest().body("회원가입 실패"));
    }

    public record LoginRequest(String id, String password) {}
    public record LoginResponse(String token, String id, List<String> roles, String error) {}
    public record RegisterRequest(String id, String name, String password) {}
} 