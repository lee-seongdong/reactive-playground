import {Link, Route, Routes, useLocation} from 'react-router-dom'
import { useState, useEffect } from 'react'
import './App.css'
import HomePage from './pages/HomePage'
import BoardDetailPage from './pages/BoardDetailPage'
import DashboardPage from './pages/DashboardPage'
import LoginPage from './pages/LoginPage'

function App() {
    const location = useLocation();
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userInfo, setUserInfo] = useState(null);

    // 로그인 상태 확인
    useEffect(() => {
        const token = localStorage.getItem('authToken');
        const userId = localStorage.getItem('userId');
        const userRoles = localStorage.getItem('userRoles');
        
        if (token && userId) {
            setIsLoggedIn(true);
            setUserInfo({
                id: userId,
                roles: userRoles ? JSON.parse(userRoles) : []
            });
        }
    }, []);

    // 로그아웃 처리
    const handleLogout = () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userId');
        localStorage.removeItem('userRoles');
        setIsLoggedIn(false);
        setUserInfo(null);
        window.location.href = '/';
    };

    // 관리자 권한 확인
    const isAdmin = userInfo?.roles?.includes('ADMIN');

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
                        
                        {/* 로그인 상태에 따른 네비게이션 */}
                        {isLoggedIn ? (
                            <div className="user-menu">
                                <span className="user-info">
                                    {isAdmin ? '👑' : '👤'} {userInfo.id}
                                    {isAdmin && <span className="admin-badge">ADMIN</span>}
                                </span>
                                <button onClick={handleLogout} className="logout-btn">
                                    🚪 로그아웃
                                </button>
                            </div>
                        ) : (
                            <Link 
                                to="/login" 
                                className={`nav-link login-nav ${location.pathname === '/login' ? 'active' : ''}`}
                            >
                                🔐 로그인
                            </Link>
                        )}
                    </div>
                </div>
            </nav>

            {/* 라우트 컨텐츠 */}
            <main className="main-content">
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/board/:boardId" element={<BoardDetailPage />} />
                    <Route path="/dashboard" element={<DashboardPage />} />
                    <Route path="/login" element={<LoginPage />} />
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