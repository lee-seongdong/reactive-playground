import React from 'react';

const CommentSection = ({ comments, newComment, onCommentChange, onCommentSubmit }) => {
    return (
        <div className="comments-section">
            <h3>💬 댓글 ({comments.length}개)</h3>
            
            {/* 댓글 목록 */}
            <div className="comments-list">
                {comments.map((comment) => (
                    <div key={comment.id} className="comment-item">
                        <div className="comment-author">{comment.registrant}</div>
                        <div className="comment-content">{comment.content}</div>
                        <div className="comment-date">
                            {new Date(comment.registeredDateTime).toLocaleString()}
                        </div>
                    </div>
                ))}
            </div>

            {/* 댓글 작성 폼 */}
            <form onSubmit={onCommentSubmit} className="comment-form">
                <textarea
                    value={newComment}
                    onChange={onCommentChange}
                    placeholder="댓글을 입력하세요..."
                    rows="3"
                />
                <button type="submit" className="comment-submit-btn">
                    댓글 작성
                </button>
            </form>
        </div>
    );
};

export default CommentSection; 