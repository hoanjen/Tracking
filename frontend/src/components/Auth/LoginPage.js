import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { useAuth } from '../../hooks/useAuth';
import { authService } from '../../services/api';
import './LoginPage.css';

function LoginPage() {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard');
  }, [isAuthenticated, navigate]);

  const handleLoginSuccess = async (credentialResponse) => {
    try {
      const response = await authService.login(credentialResponse.credential);
      login(response.data.accessToken, response.data.user);
      navigate('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
      alert('Login failed. Please try again.');
    }
  };

  return (
    <GoogleOAuthProvider clientId={process.env.REACT_APP_GOOGLE_CLIENT_ID}>
      <div className="login-container">
        <div className="login-card">
          <span className="login-logo">📱</span>
          <h1>Follower Tracker</h1>
          <p>Monitor your TikTok audience.<br />Track who follows and unfollows.</p>

          <div className="login-divider"><span>Sign in to continue</span></div>

          <div className="login-button">
            <div className="login-button-wrap">
              <GoogleLogin
                onSuccess={handleLoginSuccess}
                onError={() => alert('Login Failed')}
                theme="filled_black"
                shape="pill"
                size="large"
              />
            </div>
          </div>

          <p className="login-footer">
            By signing in you agree to our Terms of Service.<br />
            Your data is stored securely and never shared.
          </p>
        </div>
      </div>
    </GoogleOAuthProvider>
  );
}

export default LoginPage;
