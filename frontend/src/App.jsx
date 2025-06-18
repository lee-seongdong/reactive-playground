import {Link, Route, Routes, useLocation} from 'react-router-dom'
import './App.css'
import HomePage from './pages/HomePage'
import BoardDetailPage from './pages/BoardDetailPage'
import DashboardPage from './pages/DashboardPage'

function App() {
    const location = useLocation();

    return (
        <div className="app">
            {/* 네비게이션 바 */}
            <nav className="navbar">
                <div className="nav-container">
                    <Link to="/" className="nav-logo">
                        📋 실시간 게시판
                    </Link>
                    
                    <div className="nav-menu">
                        <Link 
                            to="/" 
                            className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}
                        >
                            🏠 홈
                        </Link>
                        <Link 
                            to="/dashboard" 
                            className={`nav-link ${location.pathname === '/dashboard' ? 'active' : ''}`}
                        >
                            📊 대시보드
                        </Link>
                    </div>
                </div>
            </nav>

            {/* 라우트 컨텐츠 */}
            <main className="main-content">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/board/:boardId" element={<BoardDetailPage />} />
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="*" element={
                        <div id="board">
                            <div className="error-message">
                                <h2>404 - 페이지를 찾을 수 없습니다</h2>
                                <p>요청하신 페이지가 존재하지 않습니다.</p>
                                <Link to="/" className="back-btn">
                                    ← 홈으로 돌아가기
                                </Link>
                            </div>
                        </div>
                    } />
                </Routes>
            </main>
        </div>
    );
}

export default App