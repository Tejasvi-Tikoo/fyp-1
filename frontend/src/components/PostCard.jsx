import { Link } from "react-router-dom";
import api from "../services/api.js";
import CommentSection from "./CommentSection.jsx";
import MapView from "./MapView.jsx";

export default function PostCard({ post }) {
  const handleLike = async () => {
    await api.post(`/posts/${post.id}/like`);
  };

  return (
    <div className="post-card">
      <div className="post-header">
        {post.user && (
          <Link to={`/profile/${post.user.id}`}>{post.user.username}</Link>
        )}
      </div>
      {post.imageUrl && (
        <img src={post.imageUrl} alt={post.title} className="post-image" />
      )}
      <div className="post-body">
        <h3>{post.title}</h3>
        <p>{post.story}</p>
        {post.route && (post.route.startLat != null || post.route.location) && (
          <div style={{ marginTop: "0.5rem" }}>
            <MapView route={post.route} />
          </div>
        )}
        <div className="post-meta">
          <span>{post.location}</span>
        </div>
        <div className="post-actions">
          <button onClick={handleLike}>Like</button>
          <Link to={`/posts/${post.id}`}>Open</Link>
        </div>
        <CommentSection postId={post.id} comments={post.comments || []} />
      </div>
    </div>
  );
}


