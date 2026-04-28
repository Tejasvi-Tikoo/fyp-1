import { useContext, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { motion } from "framer-motion";
import { FiUser, FiMail, FiLock } from "react-icons/fi";
import api from "../services/api.js";
import { AuthContext } from "../context/AuthContext.jsx";

export default function RegisterPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleSubmit = async e => {
    e.preventDefault();
    setError("");
    try {
      const res = await api.post("/auth/register", { username, email, password });
      const data = res.data;
      login(data.token, { userId: data.userId, username: data.username });
      navigate("/");
    } catch (err) {
      console.error("Registration error:", err);
      console.error("Response status:", err.response?.status);
      console.error("Response data:", err.response?.data);
      console.error("Error message:", err.message);
      if (err.response?.status === 400) {
        setError("Username or email already exists");
      } else if (err.response?.status === 500) {
        setError("Server error: " + (err.response?.data?.message || "Please check console for details"));
      } else {
        setError(err.response?.data?.message || err.message || "Registration failed");
      }
    }
  };

  return (
    <div className="auth-layout auth-layout-register">
      <motion.aside
        className="auth-hero"
        initial={{ x: -60, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.7 }}
      >
        <div className="auth-hero-overlay" />
        <div className="auth-hero-content">
          <h1>Join Wild Routes</h1>
          <p>Build your profile, post your routes, and connect with people who love adventure.</p>
          <div className="auth-feature-list">
            <div className="auth-feature-item">
              <img src="https://images.unsplash.com/photo-1470770841072-f978cf4d019e?w=300&q=80" alt="Travel story" />
              <span>Post stories with photos</span>
            </div>
            <div className="auth-feature-item">
              <img src="https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=300&q=80" alt="Community routes" />
              <span>Build your adventure network</span>
            </div>
          </div>
        </div>
      </motion.aside>

      <motion.main
        className="auth-panel"
        initial={{ x: 60, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.7 }}
      >
        <div className="auth-card">
          <div className="auth-head">
            <span className="auth-badge">Get Started</span>
            <h2>Create account</h2>
            <p>Sign up in a few quick steps.</p>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <label className="field-label">Username</label>
            <div className="auth-input-wrap">
              <FiUser />
              <input
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Choose a username"
                required
              />
            </div>

            <label className="field-label">Email</label>
            <div className="auth-input-wrap">
              <FiMail />
              <input
                type="email"
                value={email}
                onChange={e => setEmail(e.target.value)}
                placeholder="Enter your email"
                required
              />
            </div>

            <label className="field-label">Password</label>
            <div className="auth-input-wrap">
              <FiLock />
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="Create a password"
                required
              />
            </div>

            {error && <div className="auth-error">{error}</div>}

            <motion.button type="submit" whileHover={{ scale: 1.01 }} whileTap={{ scale: 0.98 }}>
              Create Account
            </motion.button>
          </form>

          <p className="auth-switch">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </div>
      </motion.main>
    </div>
  );
}

