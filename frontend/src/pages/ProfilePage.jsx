import { useContext, useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../services/api.js";
import PostCard from "../components/PostCard.jsx";
import { AuthContext } from "../context/AuthContext.jsx";

const BASE_URL = "http://localhost:8080";

export default function ProfilePage() {
  const { id } = useParams();
  const { user: authUser } = useContext(AuthContext);

  const [profile, setProfile] = useState(null);
  const [error, setError] = useState(null);
  const [editMode, setEditMode] = useState(false);

  const [formData, setFormData] = useState({
    fullName: "",
    mobileNumber: "",
    email: "",
    bio: "",
    image: null
  });

  // ✅ LOAD PROFILE
  useEffect(() => {
    let mounted = true;

    api.get(`/users/${id}`)
      .then(res => {
        if (!mounted) return;
        setProfile(res.data);
      })
      .catch(err => {
        console.error(err);
        setError("Failed to load profile");
      });

    return () => { mounted = false };
  }, [id]);

  if (error) return <div className="page">{error}</div>;
  if (!profile) return <div className="page">Loading...</div>;

  const { user, posts, followersCount, followingCount } = profile;
  const isOwnProfile = authUser && String(authUser.userId) === String(id);

  // ✅ OPEN EDIT (prefill form)
  const handleEditClick = () => {
    setFormData({
      fullName: user?.fullName || "",
      mobileNumber: user?.mobileNumber || "",
      email: user?.email || "",
      bio: user?.bio || "",
      image: null
    });
    setEditMode(true);
  };

  // ✅ HANDLE INPUT CHANGE
  const handleChange = e => {
    if (e.target.name === "image") {
      setFormData(prev => ({ ...prev, image: e.target.files[0] }));
    } else {
      setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
    }
  };

  // ✅ SAVE PROFILE
  const handleSave = async e => {
    e.preventDefault();

    try {
      const data = new FormData();

      data.append("fullName", formData.fullName);
      data.append("mobileNumber", formData.mobileNumber);
      data.append("email", formData.email);
      data.append("bio", formData.bio);

      if (formData.image) {
        data.append("image", formData.image);
      }

      const res = await api.put("/users/me", data, {
        headers: { "Content-Type": "multipart/form-data" }
      });

      setProfile(prev => ({ ...prev, user: res.data }));
      setEditMode(false);

    } catch (err) {
      console.error(err);
      setError("Update failed");
    }
  };

  return (
    <div className="page profile-page">

      {/* HEADER */}
      <div className="profile-header">

        <img
          src={
            user?.profilePhotoUrl
              ? `${BASE_URL}${user.profilePhotoUrl}`
              : "https://picsum.photos/100"
          }
          alt="profile"
          className="profile-photo"
        />

        <div className="profile-info">
          <h2>{user?.fullName || user?.username}</h2>
          <p>@{user?.username}</p>

          {user?.bio && <p className="bio">{user.bio}</p>}

          <div className="profile-stats">
            <span><strong>{posts?.length || 0}</strong> posts</span>
            <span><strong>{followersCount}</strong> followers</span>
            <span><strong>{followingCount}</strong> following</span>
          </div>

          {isOwnProfile && !editMode && (
            <button onClick={handleEditClick}>
              Edit Profile
            </button>
          )}
        </div>
      </div>

      {/* EDIT FORM */}
      {editMode && (
        <form onSubmit={handleSave} className="profile-edit-form">

          <input type="file" name="image" onChange={handleChange} />

          <input
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            placeholder="Full name"
          />

          <input
            name="mobileNumber"
            value={formData.mobileNumber}
            onChange={handleChange}
            placeholder="Mobile number"
          />

          <input
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="Email"
          />

          <textarea
            name="bio"
            value={formData.bio}
            onChange={handleChange}
            placeholder="Bio"
          />

          <div className="button-row">
            <button type="submit">Save</button>
            <button type="button" onClick={() => setEditMode(false)}>
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* POSTS */}
      <div className="profile-posts">
        {posts && posts.length > 0 ? (
          posts.map(p => <PostCard key={p.id} post={p} />)
        ) : (
          <p>No posts yet</p>
        )}
      </div>

    </div>
  );
}