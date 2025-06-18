import React, {useEffect, useState} from 'react';
import CommentSection from './CommentSection';

const BoardDetail = ({ board, onBackToList }) => {
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');

    useEffect(() => {
        if (board) {
            loadComments();
            return setupEventSource();
        }
    }, [board]);

    const loadComments = async () => {
        try {
            const response = await fetch(`/api/boards/${board.id}/comments`);
            const commentsData = await response.json();
            setComments(commentsData);
        } catch (error) {
            console.error('댓글 로드 실패:', error);
        }
    };

    const setupEventSource = () => {
        const eventSource = new EventSource(`/api/boards/${board.id}/comments/stream`);
        
        eventSource.onmessage = (event) => {
            const newCommentData = JSON.parse(event.data);
            
            // 중복 방지: 이미 존재하는 댓글인지 확인
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

    // 댓글 추가
    const handleAddComment = async (e) => {
        e.preventDefault();
        
        if (!newComment.trim()) {
            alert('댓글을 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`/api/boards/${board.id}/comments`, {
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

    return (
        <div className="board-detail">
            <button onClick={onBackToList} className="back-btn">
                ← 목록으로 돌아가기
            </button>
            
            <div className="detail-content">
                <h1>{board.title}</h1>
                <div className="detail-meta">
                    <span>작성자: {board.registrant}</span>
                    <span>작성일: {new Date(board.registeredDateTime).toLocaleString()}</span>
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
    );
};

export default BoardDetail; 