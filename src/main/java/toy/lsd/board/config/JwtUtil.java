package toy.lsd.board.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 🎫 JWT 토큰을 생성하고 검증하는 유틸리티 클래스
 * 
 * JWT란? JSON Web Token - 사용자 정보를 JSON 형태로 안전하게 전송하는 방식
 * 구조: Header.Payload.Signature
 * 
 * 왜 JWT를 사용하나?
 * - Stateless: 서버에 세션을 저장하지 않아도 됨
 * - 확장성: 여러 서버에서 동일한 토큰으로 인증 가능
 * - 모바일 앱에서 사용하기 좋음
 */
@Component
public class JwtUtil {
    
    // 🔑 JWT 서명에 사용할 비밀키 (실제 운영에서는 환경변수로 관리)
    private static final String SECRET = "mySecretKey123!@#ForLearning";
    
    // ⏰ 토큰 유효시간 (1시간)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour
    
    private final Algorithm algorithm = Algorithm.HMAC256(SECRET);
    
    /**
     * 🎫 JWT 토큰 생성 메소드
     * @param username 사용자명
     * @param roles 사용자 권한 목록
     * @return JWT 토큰 문자열
     */
    public String generateToken(String username, List<String> roles) {
        return JWT.create()
            .withSubject(username)                                    // 토큰 주체 (사용자명)
            .withClaim("roles", roles)                               // 사용자 권한
            .withIssuedAt(new Date())                                // 발급 시간
            .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간
            .withIssuer("toy-board-app")                             // 토큰 발급자
            .sign(algorithm);
    }
    
    /**
     * 🔍 JWT 토큰에서 사용자명 추출
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
    
    /**
     * 🎭 JWT 토큰에서 권한 목록 추출
     * @param token JWT 토큰
     * @return 권한 목록
     */
    public List<String> getRolesFromToken(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getClaim("roles").asList(String.class);
        } catch (JWTVerificationException e) {
            return List.of();
        }
    }
    
    /**
     * ✅ JWT 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            verifyToken(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }
    
    /**
     * 🔐 JWT 토큰 검증 및 디코딩
     * @param token JWT 토큰
     * @return 디코딩된 JWT 객체
     * @throws JWTVerificationException 토큰이 유효하지 않은 경우
     */
    private DecodedJWT verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer("toy-board-app")
            .build();
        return verifier.verify(token);
    }
} 