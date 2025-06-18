package toy.lsd.playground.p4board.board.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.context.Context;
import toy.lsd.playground.p4board.board.entity.Board;
import toy.lsd.playground.p4board.board.repository.BoardCrudRepository;
import toy.lsd.playground.p4board.board.repository.BoardWithViewCountRepository;
import toy.lsd.playground.p4board.board.repository.dto.BoardWithViewCount;

@Service
@RequiredArgsConstructor
public class BoardService {
	private final BoardWithViewCountRepository boardWithViewCountRepository;
	private final BoardCrudRepository boardCrudRepository;

	private final Sinks.Many<Board> newPostSink = Sinks.many().replay().latest();
	private final ViewLogService viewLogService;

	/**
	 * 무한스크롤용 페이징 조회
	 * @param page 페이지 번호 (0부터 시작)
	 * @param size 페이지 크기
	 * @return 조회수 포함된 게시글 목록
	 */
	public Flux<Board> getBoards(int page, int size) {
		long offset = (long) page * size;
		return boardWithViewCountRepository.findAllWithViewCount((long)size, offset)
			.map(BoardWithViewCount::toBoard);
	}

	/**
	 * 게시글 생성 (실시간 알림 포함)
	 * @param board 생성할 게시글
	 * @return 생성된 게시글 (조회수 포함)
	 */
	public Mono<Board> createBoard(Board board) {
		return boardCrudRepository.save(board)
			.contextWrite(Context.of("entityType", "board"))
			.flatMap(savedBoard -> 
				// 조회수 포함한 완전한 Board 조회 후 실시간 알림 발송
				getBoardById(savedBoard.getId())
					.doOnNext(completeBoard -> {
						newPostSink.tryEmitNext(completeBoard);
						System.out.println("📡 새 게시글 실시간 알림 발송: " + completeBoard.getTitle());
					})
			);
	}

	/**
	 * 게시글 상세 조회 (조회수 포함)
	 * @param id 게시글 ID
	 * @return 조회수 포함된 게시글
	 */
	public Mono<Board> getBoardById(Long id) {
		return boardWithViewCountRepository.findByIdWithViewCount(id)
			.flatMap(boardWithViewCount -> 
				viewLogService.incrementViewCount(id)
					.onErrorResume(error -> {
						System.err.println("조회수 증가 실패: " + error.getMessage());
						return Mono.empty(); // 에러 무시하고 계속 진행
					})
					.then(Mono.just(boardWithViewCount.toBoard()))
			);
	}

	/**
	 * 새 게시글 실시간 스트림 (SSE용)
	 * @return 새로 생성된 게시글만 스트리밍
	 */
	public Flux<Board> getNewPostStream() {
		return newPostSink.asFlux()
			.doOnSubscribe(subscription -> 
				System.out.println("📡 새 게시글 실시간 스트림 구독 시작 (구독자 수: " + 
					newPostSink.currentSubscriberCount() + ")")
			)
			.doOnCancel(() -> 
				System.out.println("📡 새 게시글 실시간 스트림 구독 취소 (구독자 수: " + 
					newPostSink.currentSubscriberCount() + ")")
			);
	}

	/**
	 * 게시글 삭제
	 * @param id 삭제할 게시글 ID
	 * @return 삭제 완료 신호
	 */
	public Mono<Void> deleteBoard(Long id) {
		return boardCrudRepository.deleteById(id);
	}
}
