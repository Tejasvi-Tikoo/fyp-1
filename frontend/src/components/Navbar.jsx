import { Link, useLocation, useNavigate } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext.jsx";

export default function Navbar() {
  const { isAuthenticated, user, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const location = useLocation();

  const isAuthScreen = location.pathname === "/login" || location.pathname === "/register";

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  if (isAuthScreen) return null;

  return (
    <nav className="navbar">
      <div className="navbar-left">
        <Link to="/" className="logo">
          Wild Routes
        </Link>
      </div>
      {isAuthenticated && (
        <div className="navbar-center">
          <Link to="/">Feed</Link>
          <Link to="/explore">Explore</Link>
          <Link to="/posts/new">Create</Link>
          <Link to="/chat">Chat</Link>
          <Link to="/groups">Groups</Link>
        </div>
      )}
      <div className="navbar-right">
        {isAuthenticated && user ? (
          <>
            <Link to={`/profile/${user.userId}`} className="profile-pill">
              {user.username}
            </Link>
            <button onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}

