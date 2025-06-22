package toy.lsd.board.board.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import toy.lsd.board.board.entity.Board;
import toy.lsd.board.board.service.BoardService;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
	private final BoardService boardService;

	/**
	 * 🆕 무한스크롤용 게시글 목록 조회 (REST API)
	 * @param page 페이지 번호 (기본값: 0)
	 * @param size 페이지 크기 (기본값: 5)
	 * @return 페이징된 게시글 목록
	 */
	@GetMapping
	public Flux<Board> getBoards(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		System.out.println("📄 게시글 페이징 조회 - page: " + page + ", size: " + size);
		return boardService.getBoards(page, size);
	}

	/**
	 * 🆕 새 게시글 실시간 알림 (SSE)
	 * @return 새로 생성된 게시글만 스트리밍
	 */
	@GetMapping(value = "/new-posts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Board> getNewPostsStream() {
		System.out.println("📡 새 게시글 실시간 스트림 연결 시작");
		return boardService.getNewPostStream();
	}

	/**
	 * 게시글 생성 (실시간 알림 포함)
	 * @param board 생성할 게시글
	 * @return 생성된 게시글
	 */
	@PostMapping
	public Mono<Board> createBoard(@RequestBody Board board) {
		System.out.println("✍️ 새 게시글 생성: " + board.getTitle());
		return boardService.createBoard(board);
	}

	/**
	 * 게시글 상세 조회
	 * @param id 게시글 ID
	 * @return 조회수 포함된 게시글
	 */
	@GetMapping("/{id}")
	public Mono<Board> getBoard(@PathVariable Long id) {
		System.out.println("🔍 게시글 상세 조회: " + id);
		return boardService.getBoardById(id);
	}

	/**
	 * 게시글 삭제
	 * @param id 삭제할 게시글 ID
	 * @return 삭제 완료 신호
	 */
	@DeleteMapping("/{id}")
	public Mono<Void> deleteBoard(@PathVariable Long id) {
		System.out.println("🗑️ 게시글 삭제: " + id);
		return boardService.deleteBoard(id);
	}
}
