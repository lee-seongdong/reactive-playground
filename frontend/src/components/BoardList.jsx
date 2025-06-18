import React from 'react';

const BoardList = ({ boards, onBoardClick }) => {
    return (
        <div className="board-list">
            {boards.length === 0 ? (
                <div className="no-boards">게시글이 없습니다.</div>
            ) : (
                boards.map((board) => (
                    <div 
                        key={board.id} 
                        className="board-item clickable"
                        onClick={() => onBoardClick(board.id)}
                    >
                        <div className="board-header">
                            <h3 className="board-title">{board.title}</h3>
                            <div className="board-meta">
                                <span className="board-id">#{board.id}</span>
                                <span className="board-view-count">👁 {board.viewCount || 0}</span>
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
    );
};

export default BoardList; 