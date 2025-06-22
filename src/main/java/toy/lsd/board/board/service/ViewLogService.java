package toy.lsd.board.board.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import toy.lsd.board.board.entity.BoardViewLog;
import toy.lsd.board.board.repository.BoardViewLogRepository;

@Service
@RequiredArgsConstructor
public class ViewLogService {
	private final BoardViewLogRepository boardViewLogRepository;

	// 조회수 증가
	public Mono<Void> incrementViewCount(Long boardId) {
		BoardViewLog viewLog = new BoardViewLog();
		viewLog.setBoardId(boardId);

		return boardViewLogRepository.save(viewLog)
			.contextWrite(Context.of("entityType", "board_view_log"))
			.then(); // 결과값은 무시하고 완료 신호만 반환
	}
}