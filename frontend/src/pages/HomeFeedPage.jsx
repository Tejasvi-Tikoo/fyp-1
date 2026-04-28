import { useEffect, useState } from "react";
import api from "../services/api.js";
import PostCard from "../components/PostCard.jsx";

export default function HomeFeedPage() {
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    let mounted = true;

    api.get("/feed")
      .then(res => {
        if (mounted) setPosts(res.data);
      })
      .catch(err => {
        console.error("Feed error:", err);
      });

    return () => { mounted = false };
  }, []);

  return (
    <div className="page feed-page">
      <div className="page-banner">
        <img src="https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=1400&q=80" alt="Feed banner" />
        <div className="page-banner-overlay">
          <h2>Adventure Feed</h2>
          <p>Fresh stories, route snapshots, and moments from the community.</p>
        </div>
      </div>

      <div className="page-header">
        <h1>Your Feed</h1>
        <p>See new travel stories and routes from your community.</p>
      </div>

      {posts.length === 0 ? (
        <div className="empty-state">
          <h3>No posts yet</h3>
          <p>Once your network shares new routes, they will appear here.</p>
        </div>
      ) : (
        posts.map(p => (
          <PostCard key={p.id} post={p} />
        ))
      )}
    </div>
  );
}

