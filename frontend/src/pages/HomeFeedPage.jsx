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
      {posts.map(p => (
        <PostCard key={p.id} post={p} />
      ))}
    </div>
  );
}

