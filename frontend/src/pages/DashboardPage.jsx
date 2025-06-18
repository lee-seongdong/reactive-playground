import {useNavigate} from 'react-router-dom';

const DashboardPage = () => {
    const navigate = useNavigate();

    const handleBackToHome = () => {
        navigate('/');
    };

    return (
        <div id="board">
            <button onClick={handleBackToHome} className="back-btn">
                ← 홈으로 돌아가기
            </button>

            <h1>📊 실시간 대시보드</h1>
            <p className="board-info">실시간 통계 정보</p>

            <div className="dashboard-container">
                <div className="coming-soon">
                    <h2>🚧 개발 중...</h2>
                    <p>실시간 통계 대시보드가 곧 완성됩니다!</p>
                </div>
            </div>
        </div>
    );
};

export default DashboardPage; 