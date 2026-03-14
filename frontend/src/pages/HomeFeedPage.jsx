import { useEffect, useState } from "react";
import api from "../services/api.js";
import PostCard from "../components/PostCard.jsx";

export default function HomeFeedPage() {
  const [posts, setPosts] = useState([]);

  useEffect(() => {
    api.get("/feed").then(res => setPosts(res.data));
  }, []);

  return (
    <div className="page feed-page">
      {posts.map(p => (
        <PostCard key={p.id} post={p} />
      ))}
    </div>
  );
}

