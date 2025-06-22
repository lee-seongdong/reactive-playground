import {useCallback, useEffect, useRef, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import BoardList from '../components/BoardList';
import BoardForm from '../components/BoardForm';

const HomePage = () => {
    const navigate = useNavigate();
    const [boards, setBoards] = useState([]);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [page, setPage] = useState(0);
    const eventSourceRef = useRef(null);

    // 🔐 인증 상태 관리
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userInfo, setUserInfo] = useState(null);
    const [authToken, setAuthToken] = useState(null);

    const [newBoard, setNewBoard] = useState({
        title: '',
        content: '',
        memo: ''
    });

    const PAGE_SIZE = 5;

    useEffect(() => {
        // 🔐 인증 상태 확인
        checkAuthStatus();
        
        // 1️⃣ 초기 데이터 로드 (REST API)
        loadInitialBoards();
        
        // 2️⃣ 실시간 새 게시글 스트림 연결 (SSE)
        setupNewPostStream();
        
        // Cleanup
        return () => {
            if (eventSourceRef.current) {
                eventSourceRef.current.close();
            }
        };
    }, []);

    // 🔐 인증 상태 확인
    const checkAuthStatus = () => {
        const token = localStorage.getItem('authToken');
        const userId = localStorage.getItem('userId');
        const userRoles = localStorage.getItem('userRoles');
        
        if (token && userId) {
            setIsLoggedIn(true);
            setAuthToken(token);
            setUserInfo({
                id: userId,
                roles: userRoles ? JSON.parse(userRoles) : []
            });
        }
    };

    // 관리자 권한 확인
    const isAdmin = userInfo?.roles?.includes('ADMIN');

    // 📄 초기 게시글 로드 (REST API)
    const loadInitialBoards = async () => {
        try {
            setIsLoading(true);
            const response = await fetch(`/api/boards?page=0&size=${PAGE_SIZE}`);
            
            if (response.ok) {
                const initialBoards = await response.json();
                setBoards(initialBoards);
                setPage(1); // 다음 페이지 준비
                setHasMore(initialBoards.length === PAGE_SIZE); // 더 있는지 확인
                console.log(`📄 초기 게시글 로드 완료: ${initialBoards.length}개`);
            } else {
                setError('게시글을 불러올 수 없습니다.');
            }
        } catch (error) {
            console.error('초기 로딩 실패:', error);
            setError('게시글 로딩에 실패했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    // 📜 더 많은 게시글 로드 (무한스크롤)
    const loadMoreBoards = useCallback(async () => {
        if (isLoadingMore || !hasMore) return;

        try {
            setIsLoadingMore(true);
            const response = await fetch(`/api/boards?page=${page}&size=${PAGE_SIZE}`);
            
            if (response.ok) {
                const moreBoards = await response.json();
                
                if (moreBoards.length > 0) {
                    setBoards(prev => [...prev, ...moreBoards]);
                    setPage(prev => prev + 1);
                    setHasMore(moreBoards.length === PAGE_SIZE);
                    console.log(`📜 페이지 ${page} 로드: ${moreBoards.length}개 추가`);
                } else {
                    setHasMore(false);
                    console.log('📜 더 이상 게시글이 없습니다');
                }
            }
        } catch (error) {
            console.error('추가 로딩 실패:', error);
        } finally {
            setIsLoadingMore(false);
        }
    }, [page, isLoadingMore, hasMore]);

    // 📡 새 게시글 실시간 스트림 (SSE)
    const setupNewPostStream = () => {
        const eventSource = new EventSource('/api/boards/new-posts');
        eventSourceRef.current = eventSource;

        eventSource.onopen = () => {
            console.log('📡 실시간 새 게시글 스트림 연결 성공');
        };
        
        eventSource.onmessage = (event) => {
            const newPost = JSON.parse(event.data);
            console.log('📡 새 게시글 수신:', newPost.title);
            
            // 새 게시글을 맨 앞에 추가
            setBoards(prev => [newPost, ...prev]);
        };

        eventSource.onerror = (error) => {
            console.error('📡 실시간 연결 실패:', error);
        };
    };

    // 🔄 무한스크롤 감지
    const handleScroll = useCallback(() => {
        if (window.innerHeight + document.documentElement.scrollTop >= 
            document.documentElement.offsetHeight - 1000) {
            loadMoreBoards();
        }
    }, [loadMoreBoards]);

    useEffect(() => {
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, [handleScroll]);

    // 게시글 클릭 → 상세 페이지로 라우팅
    const handleBoardClick = (boardId) => {
        navigate(`/board/${boardId}`);
    };

    // ✍️ 게시글 등록 (JWT 토큰 포함)
    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!newBoard.title.trim() || !newBoard.content.trim()) {
            alert('제목과 내용을 모두 입력해주세요.');
            return;
        }

        if (!isAdmin) {
            alert('게시글 작성은 관리자만 가능합니다.');
            return;
        }

        try {
            const response = await fetch('/api/boards', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${authToken}` // 🔑 JWT 토큰 포함
                },
                body: JSON.stringify(newBoard)
            });

            if (response.ok) {
                setNewBoard({ title: '', content: '', memo: '' });
                alert('게시글이 등록되었습니다!');
                // 새 게시글은 SSE를 통해 자동으로 추가됨
            } else if (response.status === 401) {
                alert('인증이 만료되었습니다. 다시 로그인해주세요.');
                localStorage.clear();
                navigate('/login');
            } else if (response.status === 403) {
                alert('권한이 없습니다. 관리자만 게시글을 작성할 수 있습니다.');
            } else {
                alert('게시글 등록에 실패했습니다.');
            }
        } catch (error) {
            console.error('게시글 등록 실패:', error);
            alert('게시글 등록에 실패했습니다.');
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setNewBoard(prev => ({ ...prev, [name]: value }));
    };

    if (isLoading) {
        return (
            <div id="board">
                <div className="loading">
                    <h2>📄 게시글 로딩 중...</h2>
                    <p>잠시만 기다려주세요.</p>
                </div>
            </div>
        );
    }

    return (
        <div id="board">
            {error && <div className="error-message">에러: {error}</div>}

            <h1>📋 실시간 게시판 (JWT 인증)</h1>
            <p className="board-info">
                총 {boards.length}개의 게시글 
                📄 REST API + 📡 실시간 SSE
                {isLoggedIn && (
                    <span className="auth-status">
                        | 🔐 {userInfo.id} ({isAdmin ? '👑 관리자' : '👤 일반사용자'})
                    </span>
                )}
            </p>

            <BoardList
                boards={boards}
                onBoardClick={handleBoardClick}
            />

            {/* 무한스크롤 로딩 */}
            {isLoadingMore && (
                <div className="loading">
                    <p>📜 더 많은 게시글을 불러오는 중...</p>
                </div>
            )}

            {/* 더 이상 게시글이 없음 */}
            {!hasMore && boards.length > 0 && (
                <div className="loading">
                    <p>📜 모든 게시글을 불러왔습니다.</p>
                </div>
            )}

            {/* 🔐 게시글 작성 권한 체크 */}
            {isLoggedIn ? (
                isAdmin ? (
                    <BoardForm
                        newBoard={newBoard}
                        onInputChange={handleInputChange}
                        onSubmit={handleSubmit}
                    />
                ) : (
                    <div className="auth-message user-message">
                        <h3>👤 일반사용자 계정</h3>
                        <p>게시글 작성은 관리자만 가능합니다.</p>
                        <p>게시글 조회와 댓글 확인은 자유롭게 이용하세요!</p>
                    </div>
                )
            ) : (
                <div className="auth-message login-required">
                    <h3>🔐 로그인이 필요합니다</h3>
                    <p>게시글 작성 및 상세 조회를 위해 로그인해주세요.</p>
                    <button 
                        onClick={() => navigate('/login')} 
                        className="login-redirect-btn"
                    >
                        🚀 로그인하러 가기
                    </button>
                    <div className="test-account-info">
                        <p><strong>📝 테스트 계정</strong></p>
                        <p>👑 관리자: admin / admin</p>
                        <p>👤 일반사용자: user / password</p>
                    </div>
                </div>
            )}
        </div>
    );
};

export default HomePage; 