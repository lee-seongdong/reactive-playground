package toy.lsd.board.member.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import toy.lsd.board.member.entity.Member;

@Repository
public interface MemberRepository extends ReactiveCrudRepository<Member, String> {
    
} 