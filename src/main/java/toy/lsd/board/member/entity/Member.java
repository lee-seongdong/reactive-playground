package toy.lsd.board.member.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 👤 간단한 회원 엔티티 - Spring Security UserDetails 구현
 * 
 * id: 로그인 ID (예: "admin", "user123")
 * name: 사용자 실제 이름 (예: "홍길동", "관리자")
 */
@Table("member")
public class Member implements UserDetails {
    
    @Id
    private String id;          // 로그인 ID (PK)
    private String name;        // 사용자 실제 이름
    private String password;    // 암호화된 비밀번호
    private String role;        // 권한 (USER, ADMIN)
    
    // 기본 생성자
    public Member() {}
    
    // 생성자
    public Member(String id, String name, String password, String role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }
    
    // === Spring Security UserDetails 구현 ===
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role이 "ADMIN,USER" 형태라면 콤마로 분리
        String[] roles = role.split(",");
        return List.of(roles).stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim()))
            .toList();
    }
    
    @Override
    public String getUsername() {
        return id;  // 로그인 ID를 username으로 사용
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    // === Getters and Setters ===
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
} 