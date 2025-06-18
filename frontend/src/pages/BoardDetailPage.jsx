import {useEffect, useState} from 'react';
import {useNavigate, useParams} from 'react-router-dom';
import CommentSection from '../components/CommentSection';

const BoardDetailPage = () => {
    const { boardId } = useParams();
    const navigate = useNavigate();
    const [board, setBoard] = useState(null);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        if (boardId) {
            loadBoard();
            loadComments();
            const cleanup = setupEventSource();
            return cleanup;
        }
    }, [boardId]);

    const loadBoard = async () => {
        try {
            setLoading(true);
            const response = await fetch(`/api/boards/${boardId}`);
            if (response.ok) {
                const boardData = await response.json();
                setBoard(boardData);
            } else {
                setError('게시글을 찾을 수 없습니다.');
            }
        } catch (error) {
            console.error('게시글 로드 실패:', error);
            setError('게시글을 불러올 수 없습니다.');
        } finally {
            setLoading(false);
        }
    };

    const loadComments = async () => {
        try {
            const response = await fetch(`/api/boards/${boardId}/comments`);
            const commentsData = await response.json();
            setComments(commentsData);
        } catch (error) {
            console.error('댓글 로드 실패:', error);
        }
    };

    const setupEventSource = () => {
        const eventSource = new EventSource(`/api/boards/${boardId}/comments/stream`);
        
        eventSource.onmessage = (event) => {
            const newCommentData = JSON.parse(event.data);
            
            setComments(prevComments => {
                const exists = prevComments.some(comment => comment.id === newCommentData.id);
                if (!exists) {
                    return [newCommentData, ...prevComments];
                }
                return prevComments;
            });
        };

        eventSource.onerror = (error) => {
            console.error('실시간 댓글 연결 실패:', error);
        };

        return () => {
            eventSource.close();
        };
    };

    const handleBackToList = () => {
        navigate('/');
    };

    const handleAddComment = async (e) => {
        e.preventDefault();
        
        if (!newComment.trim()) {
            alert('댓글을 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`/api/boards/${boardId}/comments`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    content: newComment,
                    registrant: 'user'
                })
            });

            if (response.ok) {
                setNewComment('');
            }
        } catch (error) {
            console.error('댓글 추가 실패:', error);
            alert('댓글 추가에 실패했습니다.');
        }
    };

    const handleCommentChange = (e) => {
        setNewComment(e.target.value);
    };

    if (loading) {
        return (
            <div id="board">
                <div className="loading">게시글을 불러오는 중...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div id="board">
                <div className="error-message">에러: {error}</div>
                <button onClick={handleBackToList} className="back-btn">
                    ← 목록으로 돌아가기
                </button>
            </div>
        );
    }

    if (!board) {
        return (
            <div id="board">
                <div className="error-message">게시글을 찾을 수 없습니다.</div>
                <button onClick={handleBackToList} className="back-btn">
                    ← 목록으로 돌아가기
                </button>
            </div>
        );
    }

    return (
        <div id="board">
            <div className="board-detail">
                <button onClick={handleBackToList} className="back-btn">
                    ← 목록으로 돌아가기
                </button>
                
                <div className="detail-content">
                    <h1>{board.title}</h1>
                    <div className="detail-meta">
                        <span>작성자: {board.registrant}</span>
                        <span>작성일: {new Date(board.registeredDateTime).toLocaleString()}</span>
                        <span>조회수: 👁 {board.viewCount || 0}</span>
                    </div>
                    <div className="detail-body">{board.content}</div>
                </div>

                <CommentSection
                    comments={comments}
                    newComment={newComment}
                    onCommentChange={handleCommentChange}
                    onCommentSubmit={handleAddComment}
                />
            </div>
        </div>
    );
};

export default BoardDetailPage; 