import { useState } from "react";
import api from "../services/api.js";

export default function CommentSection({ postId, comments: initialComments }) {
  const [comments, setComments] = useState(initialComments || []);
  const [text, setText] = useState("");

  const submit = async e => {
    e.preventDefault();
    if (!text.trim()) return;
    const res = await api.post(`/posts/${postId}/comment`, { content: text });
    setComments(prev => [...prev, res.data]);
    setText("");
  };

  return (
    <div className="comments">
      <div className="comments-list">
        {comments.map(c => (
          <div key={c.id} className="comment">
            <strong>{c.user?.username}</strong> {c.content}
          </div>
        ))}
      </div>
      <form onSubmit={submit} className="comment-form">
        <input
          value={text}
          onChange={e => setText(e.target.value)}
          placeholder="Add a comment..."
        />
        <button type="submit">Post</button>
      </form>
    </div>
  );
}

