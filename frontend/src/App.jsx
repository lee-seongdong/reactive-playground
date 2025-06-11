import { useState, useEffect } from 'react'
import './App.css'

function App() {
    const [boards, setBoards] = useState([]);
    const [error, setError] = useState(null);
    
    // 게시글 등록 폼 상태
    const [newBoard, setNewBoard] = useState({
        title: '',
        content: ''
    });

    useEffect(() => {
        // 초기 게시글 목록 로드
        fetch('/api/boards')
            .then(response => response.json())
            .then(data => setBoards(data))
            .catch(error => {
                console.error('게시글 로드 실패:', error);
                setError('게시글을 불러올 수 없습니다.');
            });

        // 실시간 게시글 스트림 연결
        const eventSource = new EventSource('/api/boards?delay=1000');

        eventSource.onmessage = (event) => {
            try {
                console.log('새 게시글 수신:', event);
                const newBoardData = JSON.parse(event.data);
                setBoards(prevBoards => {
                    // 중복 방지: 이미 존재하는 게시글인지 확인
                    if (prevBoards.some(board => board.id === newBoardData.id)) {
                        return prevBoards;
                    }
                    // 새 게시글을 맨 앞에 추가 (최신순)
                    return [newBoardData, ...prevBoards];
                });
            } catch (error) {
                console.error('스트림 데이터 파싱 실패:', error);
            }
        };

        eventSource.onerror = (error) => {
            console.log('EventSource 상태:', eventSource.readyState);
            
            // 완료와 실제 에러 구분
            if (eventSource.readyState === EventSource.CLOSED) {
                console.log('✅ 스트림이 정상적으로 완료되었습니다.');
                // 완료 상태는 에러로 처리하지 않음
            } else if (eventSource.readyState === EventSource.CONNECTING) {
                console.log('🔄 재연결 시도 중...');
            } else {
                console.error('❌ 실시간 스트림 에러:', error);
                setError('실시간 업데이트 연결에 실패했습니다.');
            }
        };

        eventSource.onopen = () => {
            console.log('✅ 실시간 스트림 연결 성공!');
            setError(null); // 연결 성공 시 에러 메시지 제거
        };

        return () => eventSource.close();
    }, []);

    // 게시글 등록
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!newBoard.title.trim() || !newBoard.content.trim()) {
            alert('제목과 내용을 모두 입력해주세요.');
            return;
        }

        try {
            const response = await fetch('/api/boards', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newBoard)
            });

            console.log('등록 응답 상태:', response.status);
            if (response.ok) {
                // 등록 성공 시 폼만 초기화 (실시간 스트림에서 자동으로 추가됨)
                setNewBoard({
                    title: '',
                    content: ''
                });
                alert('게시글이 등록되었습니다!');
            } else {
                throw new Error('게시글 등록 실패');
            }
        } catch (error) {
            console.error('게시글 등록 실패:', error);
            alert('게시글 등록에 실패했습니다.');
        }
    };

    // 폼 입력 핸들러
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewBoard(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <div id="board">
            {error && <div className="error-message">에러: {error}</div>}
            
            <h1>📋 실시간 게시판</h1>
            <p className="board-info">
                총 {boards.length}개의 게시글
            </p>
            
            {/* 게시글 목록 */}
            <div className="board-list">
                {boards.length === 0 ? (
                    <div className="no-boards">게시글이 없습니다.</div>
                ) : (
                    boards.map((board) => (
                        <div key={board.id} className="board-item">
                            <div className="board-header">
                                <h3 className="board-title">{board.title}</h3>
                                <div className="board-meta">
                                    <span className="board-id">#{board.id}</span>
                                    <span className="board-author">{board.registrant}</span>
                                    <span className="board-date">
                                        {new Date(board.registeredDateTime).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>
                            <div className="board-content">{board.content}</div>
                        </div>
                    ))
                )}
            </div>

            {/* 게시글 등록 폼 */}
            <div className="board-form-section">
                <h2>✍️ 새 게시글 작성</h2>
                <form onSubmit={handleSubmit} className="board-form">
                    <div className="form-group">
                        <label htmlFor="title">제목</label>
                        <input
                            type="text"
                            id="title"
                            name="title"
                            value={newBoard.title}
                            onChange={handleInputChange}
                            placeholder="게시글 제목을 입력하세요"
                            maxLength="100"
                        />
                    </div>
                    
                    <div className="form-group">
                        <label htmlFor="content">내용</label>
                        <textarea
                            id="content"
                            name="content"
                            value={newBoard.content}
                            onChange={handleInputChange}
                            placeholder="게시글 내용을 입력하세요"
                            rows="6"
                            maxLength="1000"
                        />
                    </div>
                    
                    <button type="submit" className="submit-btn">
                        🚀 게시글 등록
                    </button>
                </form>
            </div>
        </div>
    )
}

export default App