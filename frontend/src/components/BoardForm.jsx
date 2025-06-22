import React from 'react';

const BoardForm = ({ newBoard, onInputChange, onSubmit }) => {
    return (
        <div className="board-form-section">
            <h2>✍️ 새 게시글 작성 (👑 관리자 전용)</h2>
            <p className="form-description">
                🔐 JWT 토큰 인증이 적용된 게시글 작성 폼입니다.
            </p>
            
            <form onSubmit={onSubmit} className="board-form">
                <div className="form-group">
                    <label htmlFor="title">제목</label>
                    <input
                        type="text"
                        id="title"
                        name="title"
                        value={newBoard.title}
                        onChange={onInputChange}
                        placeholder="게시글 제목을 입력하세요"
                        required
                    />
                </div>
                
                <div className="form-group">
                    <label htmlFor="content">내용</label>
                    <textarea
                        id="content"
                        name="content"
                        value={newBoard.content}
                        onChange={onInputChange}
                        placeholder="게시글 내용을 입력하세요"
                        rows="6"
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="memo">메모 (선택사항)</label>
                    <input
                        type="text"
                        id="memo"
                        name="memo"
                        value={newBoard.memo || ''}
                        onChange={onInputChange}
                        placeholder="관리자 메모 (선택사항)"
                    />
                </div>
                
                <button type="submit" className="submit-btn">
                    🚀 게시글 등록 (JWT 인증)
                </button>
            </form>
        </div>
    );
};

export default BoardForm; 