import React, { useState } from "react";
import { Link } from "react-router-dom";
import { FaEye, FaEyeSlash } from "react-icons/fa"; // Import eye icons
import Popup from "../components/Popup";
import { getBaseUrl, loginUser } from "../apiRequest/AuthRequest";
import { generateCodePair } from "../apiRequest/PKCRUtils";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showPopup, setShowPopup] = useState(false);
  const [popupMessage, setPopupMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleEmailLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const data = await loginUser(email, password);
      console.log("login response : " + data);
      localStorage.setItem("accessToken", data.accessToken);
      localStorage.setItem("refreshToken", data.refreshToken);
      window.location.href = "/dashboard";
    } catch (exception) {
      setPopupMessage("Invalid username/password. Please try again.");
      setShowPopup(true);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLogin = async () => {
    try {
        const { codeVerifier, codeChallenge } = await generateCodePair() || {};
        if (!codeVerifier || !codeChallenge) {
            console.error("PKCE generation failed.");
            setPopupMessage("Code generation failed. Please try again.");
            setShowPopup(true);
            return;
        }

        localStorage.setItem("verifier", codeVerifier);
        const response = await fetch(getBaseUrl() + "/oauth2/google/login");

        if (!response.ok) {
            console.error("Google login API call failed:", response.status, response.statusText);
            //Show a message on the screen, or a popup.
            return;
        }

        const data = await response.json();
        const authUrl = data.authorizationUrl.replace(
            "CODE_CHALLENGE_FROM_FRONTEND",
            codeChallenge
        );

        window.location.href = authUrl;
    } catch (error) {
        console.error("Google login error:", error);
        //Show a message on the screen, or a popup.
    }
};

  const closePopup = () => {
    setShowPopup(false);
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-base-200">
      <div className="card w-96 bg-base-100 shadow-xl">
        <div className="card-body">
          <h2 className="text-2xl font-bold text-center mb-4">Login</h2>
          <form onSubmit={handleEmailLogin} className="space-y-4">
            <input
              type="email"
              placeholder="Email"
              className="input input-bordered w-full"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            <div className="relative">
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Password"
                className="input input-bordered w-full pr-10" // Add padding for icon
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <button
                type="button"
                className="absolute inset-y-0 right-0 px-3 flex items-center"
                onClick={togglePasswordVisibility}
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </button>
            </div>

            <button
              type="submit"
              className={`btn btn-primary w-full`}
              disabled={loading}
            >
              {`${loading ? "Logging in..." : "Login"}`}
            </button>
          </form>
          <div className="text-center mt-2">
            <Link to="/forgot-password" className="text-sm link link-hover">
              Forgot Password?
            </Link>
          </div>

          <div className="divider">OR</div>

          <div className="flex flex-col space-y-2">
            <button className="btn btn-outline" onClick={handleGoogleLogin}>
              <img
                src="/google-logo.png"
                alt="Google"
                className="w-6 h-6 mr-2"
              />
              Login with Google
            </button>
            <button className="btn btn-outline">
              <img
                src="/facebook-logo.png"
                alt="Facebook"
                className="w-6 h-6 mr-2"
              />
              Login with Facebook
            </button>
          </div>

          <div className="text-center mt-4">
            <p>
              Don't have an account?{" "}
              <Link to="/register" className="link link-hover">
                Register
              </Link>
            </p>
          </div>
        </div>
      </div>
      {showPopup && <Popup message={popupMessage} onClose={closePopup} />}
    </div>
  );
};

export default LoginPage;
