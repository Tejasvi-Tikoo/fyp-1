import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../services/api.js";
import PostCard from "../components/PostCard.jsx";

export default function ProfilePage() {
  const { id } = useParams();
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    api.get(`/users/${id}`).then(res => setProfile(res.data));
  }, [id]);

  if (!profile) return <div className="page">Loading...</div>;

  const { user, posts, followersCount, followingCount } = profile;

  return (
    <div className="page profile-page">
      <div className="profile-header">
        <img
          src={user.profilePhotoUrl || "https://via.placeholder.com/80"}
          alt={user.username}
          className="profile-photo"
        />
        <div>
          <h2>{user.username}</h2>
          <p>{user.bio}</p>
          <p>{user.travelInterests}</p>
          <div className="profile-stats">
            <span>{posts.length} posts</span>
            <span>{followersCount} followers</span>
            <span>{followingCount} following</span>
          </div>
        </div>
      </div>
      <div className="profile-posts">
        {posts.map(p => (
          <PostCard key={p.id} post={p} />
        ))}
      </div>
    </div>
  );
}

