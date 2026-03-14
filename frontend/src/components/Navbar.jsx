import { Link, useNavigate } from "react-router-dom";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext.jsx";

export default function Navbar() {
  const { isAuthenticated, user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

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
            <Link to={`/profile/${user.userId}`}>{user.username}</Link>
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

