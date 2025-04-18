import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { getBaseUrl } from '../apiRequest/AuthRequest';

function OAuthCallback() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    (async () => { 
        debugger;
      const params = new URLSearchParams(location.search);
      const code = params.get('code');
      const codeVerifier = localStorage.getItem('verifier');

      if (code && codeVerifier) {
        try {
          const response = await fetch(getBaseUrl() + '/oauth2/google/token', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({ code, codeVerifier }),
          });
          debugger;

          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const data = await response.json();
          console.log('Backend response:', data);

          if (data.accessToken) {
            localStorage.setItem('accessToken', data.accessToken);
          }
          if (data.refreshToken) {
            localStorage.setItem('refreshToken', data.refreshToken);
          }

          localStorage.removeItem('verifier');
          navigate('/dashboard');
        } catch (error) {
          debugger;
          console.error('Error exchanging code for tokens:', error);
          localStorage.removeItem('verifier');
          navigate('/login');
        }
      } else {
        console.error('Code or verifier missing from callback URL.');
        localStorage.removeItem('verifier');
        navigate('/login');
      }
    })(); // Immediately invoke the function
  }, [location, navigate]);

  return (
    <div>
      <h1>Processing Google Login...</h1>
    </div>
  );
}

export default OAuthCallback;