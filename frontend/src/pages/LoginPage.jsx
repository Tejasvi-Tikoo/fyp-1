import { useContext, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { motion } from "framer-motion";
import { FiUser, FiLock } from "react-icons/fi";
import api from "../services/api.js";
import { AuthContext } from "../context/AuthContext.jsx";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const handleSubmit = async e => {
    e.preventDefault();
    setError("");
    try {
      const res = await api.post("/auth/login", { username, password });
      const data = res.data;
      login(data.token, { userId: data.userId, username: data.username });
      navigate("/");
    } catch (err) {
      setError("Invalid credentials");
    }
  };

  return (
    <div className="auth-layout auth-layout-login">
      <motion.aside
        className="auth-hero"
        initial={{ x: -60, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.7 }}
      >
        <div className="auth-hero-overlay" />
        <div className="auth-hero-content">
          <h1>Wild Routes</h1>
          <p>Share your travel moments and follow inspiring routes from explorers around you.</p>
          <div className="auth-feature-list">
            <div className="auth-feature-item">
              <img src="https://images.unsplash.com/photo-1541417904950-b855846fe074?w=300&q=80" alt="Mountain route" />
              <span>Discover trending routes</span>
            </div>
            <div className="auth-feature-item">
              <img src="https://images.unsplash.com/photo-1501555088652-021faa106b9b?w=300&q=80" alt="Adventure community" />
              <span>Chat with explorers</span>
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
            <span className="auth-badge">Welcome Back</span>
            <h2>Sign in</h2>
            <p>Continue your journey with your account.</p>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <label className="field-label">Username</label>
            <div className="auth-input-wrap">
              <FiUser />
              <input
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Enter your username"
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
                placeholder="Enter your password"
                required
              />
            </div>

            {error && <div className="auth-error">{error}</div>}

            <motion.button type="submit" whileHover={{ scale: 1.01 }} whileTap={{ scale: 0.98 }}>
              Sign In
            </motion.button>
          </form>

          <p className="auth-switch">
            Don't have an account? <Link to="/register">Create one</Link>
          </p>
        </div>
      </motion.main>
    </div>
  );
}

