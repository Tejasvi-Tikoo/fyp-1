import { Link } from "react-router-dom";
import api from "../services/api.js";
import { useState } from "react";
import CommentSection from "./CommentSection.jsx";

const BASE_URL = "http://localhost:8080";

export default function PostCard({ post }) {
  // 🔥 LOCAL STATE (IMPORTANT - otherwise UI won’t update)
  const [liked, setLiked] = useState(post.liked || false);
  const [likeCount, setLikeCount] = useState(post.likeCount || 0);
  const handleLike = async () => {
    try {
      const res = await api.post(`/posts/${post.id}/like`);
      if (res.data === "liked") {
        setLiked(true);
        setLikeCount(prev => prev + 1);
      } else {
        setLiked(false);
        setLikeCount(prev => prev - 1);
      }
    } catch (err) {
      console.error("Like failed", err);
    }
  };

  return (
    <div className="post-card">
      {/* HEADER */}
      <div className="post-header">
        {post.user && (
          <Link to={`/profile/${post.userId}`} className="username">
            @{post.username}
          </Link>
        )}
      </div>

      {/* IMAGE */}
      <img
        src={
          post.imageUrl
            ? `${BASE_URL}${post.imageUrl}`
            : "https://picsum.photos/500"
        }
        alt={post.title}
      />

      {/* BODY */}
      <div className="post-body">
        <h3>{post.title}</h3>
        <p>{post.story}</p>

        {post.location && (
          <div className="post-meta">
            📍 {post.location}
          </div>
        )}

        {/* ACTIONS */}
        <div className="post-actions">
          <button onClick={handleLike}>
            {liked ? "💔 Unlike" : "❤️ Like"} ({likeCount})
          </button>
          <Link to={`/posts/${post.id}`}>Open</Link>
        </div>

        {/* COMMENTS */}
        <CommentSection postId={post.id} comments={post.comments || []} />
      </div>
    </div>
  );
}