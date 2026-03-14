import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../services/api.js";
import PostCard from "../components/PostCard.jsx";

export default function PostDetailPage() {
  const { id } = useParams();
  const [post, setPost] = useState(null);

  useEffect(() => {
    api.get(`/posts/${id}`).then(res => setPost(res.data));
  }, [id]);

  if (!post) return <div className="page">Loading...</div>;
  return (
    <div className="page">
      <PostCard post={post} />
    </div>
  );
}

