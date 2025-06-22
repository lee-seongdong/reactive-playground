package toy.lsd.board.board.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import toy.lsd.board.member.entity.Member;

/**
 * 📋 게시판 컨트롤러 - Method Security 적용
 * 
 * @PreAuthorize 어노테이션으로 메소드별 권한 제어
 */
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
	private final BoardService boardService;

	/**
	 * 🆕 무한스크롤용 게시글 목록 조회 (모든 사용자 허용)
	 */
	@GetMapping
	public Flux<Board> getBoards(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {
		System.out.println("📄 게시글 페이징 조회 - page: " + page + ", size: " + size);
		return boardService.getBoards(page, size);
	}

	/**
	 * 🆕 새 게시글 실시간 알림 (모든 사용자 허용)
	 */
	@GetMapping(value = "/new-posts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Board> getNewPostsStream() {
		System.out.println("📡 새 게시글 실시간 스트림 연결 시작");
		return boardService.getNewPostStream();
	}

	/**
	 * 📝 게시글 생성 - ADMIN 권한만 허용
	 */
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")  // 👑 ADMIN 권한만 허용
	public Mono<Board> createBoard(
			@AuthenticationPrincipal Member member,
			@RequestBody BoardCreateRequest boardRequest) {
		
		System.out.println("✍️ 새 게시글 생성: " + boardRequest.title());
		System.out.println("👤 게시글 작성자: " + member.getName());
		
		Board board = new Board();
		board.setTitle(boardRequest.title());
		board.setContent(boardRequest.content());
		board.setRegistrant(member.getName());  // 로그인된 Member의 이름 사용
		board.setModifier(member.getName());
		board.setMemo(boardRequest.memo());
		
		return boardService.createBoard(board);
	}

	/**
	 * 게시글 상세 조회 - 로그인된 사용자만 허용
	 */
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")  // 👤 로그인 필요
	public Mono<Board> getBoard(@PathVariable Long id) {
		System.out.println("🔍 게시글 상세 조회: " + id);
		return boardService.getBoardById(id);
	}

	/**
	 * 게시글 삭제 - ADMIN 권한만 허용
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")  // 👑 ADMIN 권한만 허용
	public Mono<Void> deleteBoard(@PathVariable Long id) {
		System.out.println("🗑️ 게시글 삭제: " + id);
		return boardService.deleteBoard(id);
	}

	/**
	 * 📋 게시글 생성 요청 DTO
	 */
	public record BoardCreateRequest(String title, String content, String memo) {}
}
