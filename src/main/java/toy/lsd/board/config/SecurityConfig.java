package toy.lsd.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity  // 🔑 Method Security 활성화
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    /**
     * 🛡️ 간소화된 보안 설정 - Method Security 사용
     * 
     * URL 기반 권한 체크 제거 → Controller 어노테이션으로 이동
     * 
     * 장점:
     * - URL 패턴과 메소드가 분리되지 않아 유지보수 쉬움
     * - 메소드별 세밀한 권한 제어 가능
     * - 코드가 더 명확함
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // 🚫 CSRF 보호 비활성화 (JWT 기반 API 서버용)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            // 🎫 JWT 인증 필터 추가 (요청 처리 전에 JWT 토큰 검증)
            .addFilterBefore(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            // 🌍 모든 요청 허용 - 권한 체크는 Method Security로 처리
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()  // 모든 요청 허용, 권한은 @PreAuthorize로 체크
            )
            // ❌ HTTP Basic 인증 비활성화 (JWT 사용하므로 불필요)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            // ❌ 폼 로그인 비활성화 (API 서버이므로 불필요)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
} 