import { useState } from "react";
import api from "../services/api.js";
import { useNavigate } from "react-router-dom";

export default function CreatePostPage() {
  const [title, setTitle] = useState("");
  const [story, setStory] = useState("");
  const [location, setLocation] = useState("");
  const [tags, setTags] = useState("");
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);

  const navigate = useNavigate();

  const handleImageChange = e => {
    const file = e.target.files[0];
    setImage(file);

    if (file) {
      const url = URL.createObjectURL(file);
      setPreview(url);
    }
  };

  const submit = async e => {
    e.preventDefault();

    const form = new FormData();
    form.append("title", title);
    form.append("story", story);
    form.append("location", location);
    form.append("tags", tags);

    if (image) {
      form.append("image", image);
    }

    try {
      await api.post("/posts", form, {
        headers: { "Content-Type": "multipart/form-data" }
      });

      navigate("/");
    } catch (err) {
      console.error("Error creating post:", err);
      alert("Failed to create post");
    }
  };

  return (
    <div className="page create-post-page">
      <h2>Create Travel Post</h2>

      <form onSubmit={submit} className="post-form">
        {/* Title */}
        <input
          type="text"
          value={title}
          onChange={e => setTitle(e.target.value)}
          placeholder="Title"
          required
        />

        {/* Story */}
        <textarea
          value={story}
          onChange={e => setStory(e.target.value)}
          placeholder="Share your travel experience..."
          rows={6}
          required
        />

        {/* Location */}
        <input
          type="text"
          value={location}
          onChange={e => setLocation(e.target.value)}
          placeholder="Location (e.g., Manali, India)"
        />

        {/* Tags */}
        <input
          type="text"
          value={tags}
          onChange={e => setTags(e.target.value)}
          placeholder="Tags (e.g., mountains, hiking, solo)"
        />

        {/* Image Upload */}
        <label className="file-input">
          Upload Image
          <input type="file" accept="image/*" onChange={handleImageChange} />
        </label>

        {/* Image Preview */}
        {preview && (
          <div className="image-preview">
            <img src={preview} alt="Preview" />
          </div>
        )}

        {/* Submit */}
        <button type="submit">Post</button>
      </form>
    </div>
  );
}