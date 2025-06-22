package toy.lsd.board.board.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import toy.lsd.board.board.repository.dto.BoardWithViewCount;

@Repository
public interface BoardWithViewCountRepository extends R2dbcRepository<BoardWithViewCount, Long> {
    @Query("""
        SELECT 
            b.id,
            b.title,
            b.content,
            b.registrant,
            b.registered_ymdt,
            b.modifier,
            b.modified_ymdt,
            (SELECT COUNT(*) FROM board_view_log WHERE board_id = b.id) AS viewCount
        FROM board b
        ORDER BY b.id DESC
        """)
    Flux<BoardWithViewCount> findAllWithViewCount();

    @Query("""
        SELECT 
            b.id,
            b.title,
            b.content,
            b.registrant,
            b.registered_ymdt,
            b.modifier,
            b.modified_ymdt,
            (SELECT COUNT(*) FROM board_view_log WHERE board_id = b.id) AS viewCount
        FROM board b
        ORDER BY b.id DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<BoardWithViewCount> findAllWithViewCount(Long limit, Long offset);

    @Query("""
        SELECT 
            b.id,
            b.title,
            b.content,
            b.registrant,
            b.registered_ymdt,
            b.modifier,
            b.modified_ymdt,
            (SELECT COUNT(*) FROM board_view_log WHERE board_id = b.id) AS viewCount
        FROM board b
        WHERE b.id = :id
        """)
	Mono<BoardWithViewCount> findByIdWithViewCount(Long id);
}
