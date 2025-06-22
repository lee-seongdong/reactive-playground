import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * 🔐 로그인 페이지
 * 
 * 기능:
 * - ID/비밀번호 입력
 * - JWT 토큰 로그인
 * - 토큰을 localStorage에 저장
 * - 로그인 성공 시 홈으로 리다이렉트
 */
function LoginPage() {
    const [formData, setFormData] = useState({
        id: '',
        password: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    // 입력값 변경 핸들러
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // 로그인 요청
    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const data = await response.json();
                
                // JWT 토큰을 localStorage에 저장
                localStorage.setItem('authToken', data.token);
                localStorage.setItem('userId', data.id);
                localStorage.setItem('userRoles', JSON.stringify(data.roles));
                
                console.log('🎉 로그인 성공:', data.id, data.roles);
                
                // 홈페이지로 리다이렉트
                navigate('/');
            } else {
                const errorData = await response.json();
                setError(errorData.error || '로그인에 실패했습니다.');
            }
        } catch (err) {
            console.error('로그인 에러:', err);
            setError('서버 연결에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 테스트 계정 자동 입력
    const fillTestAccount = (accountType) => {
        if (accountType === 'admin') {
            setFormData({ id: 'admin', password: 'admin' });
        } else {
            setFormData({ id: 'user', password: 'password' });
        }
    };

    return (
        <div className="login-page">
            <div className="login-container">
                <div className="login-header">
                    <h1>🔐 로그인</h1>
                    <p>JWT 기반 인증 시스템</p>
                </div>

                <form onSubmit={handleSubmit} className="login-form">
                    <div className="form-group">
                        <label htmlFor="id">로그인 ID</label>
                        <input
                            type="text"
                            id="id"
                            name="id"
                            value={formData.id}
                            onChange={handleChange}
                            placeholder="ID를 입력하세요"
                            required
                            disabled={loading}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">비밀번호</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="비밀번호를 입력하세요"
                            required
                            disabled={loading}
                        />
                    </div>

                    {error && (
                        <div className="error-message">
                            ❌ {error}
                        </div>
                    )}

                    <button 
                        type="submit" 
                        className="login-btn"
                        disabled={loading || !formData.id || !formData.password}
                    >
                        {loading ? '로그인 중...' : '🚀 로그인'}
                    </button>
                </form>

                <div className="test-accounts">
                    <h3>🧪 테스트 계정</h3>
                    <div className="test-buttons">
                        <button 
                            type="button" 
                            onClick={() => fillTestAccount('admin')}
                            className="test-btn admin"
                            disabled={loading}
                        >
                            👑 관리자 (admin/admin)
                        </button>
                        <button 
                            type="button" 
                            onClick={() => fillTestAccount('user')}
                            className="test-btn user"
                            disabled={loading}
                        >
                            👤 일반사용자 (user/password)
                        </button>
                    </div>
                </div>

                <div className="login-info">
                    <h4>💡 로그인 후 이용 가능한 기능</h4>
                    <ul>
                        <li><strong>👤 일반사용자</strong>: 게시글 상세 조회, 댓글 보기</li>
                        <li><strong>👑 관리자</strong>: 게시글 작성/수정/삭제, 댓글 관리</li>
                    </ul>
                </div>
            </div>
        </div>
    );
}

export default LoginPage; 