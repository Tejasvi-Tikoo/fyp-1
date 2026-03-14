import { useState } from "react";
import api from "../services/api.js";
import { useNavigate } from "react-router-dom";

export default function CreatePostPage() {
  const [title, setTitle] = useState("");
  const [story, setStory] = useState("");
  const [location, setLocation] = useState("");
  const [activityType, setActivityType] = useState("");
  const [distanceKm, setDistanceKm] = useState("");
  const [elevationGainM, setElevationGainM] = useState("");
  const [terrainVariance, setTerrainVariance] = useState("");
  const [startLat, setStartLat] = useState("");
  const [startLng, setStartLng] = useState("");
  const [endLat, setEndLat] = useState("");
  const [endLng, setEndLng] = useState("");
  const [image, setImage] = useState(null);
  const [routeFile, setRouteFile] = useState(null);
  const navigate = useNavigate();

  const submit = async e => {
    e.preventDefault();
    const form = new FormData();
    form.append("title", title);
    form.append("story", story);
    form.append("location", location);
    form.append("activityType", activityType);
    if (distanceKm) form.append("distanceKm", distanceKm);
    if (elevationGainM) form.append("elevationGainM", elevationGainM);
    if (terrainVariance) form.append("terrainVariance", terrainVariance);
    if (startLat) form.append("startLat", startLat);
    if (startLng) form.append("startLng", startLng);
    if (endLat) form.append("endLat", endLat);
    if (endLng) form.append("endLng", endLng);
    if (image) form.append("image", image);
    if (routeFile) form.append("routeFile", routeFile);
    await api.post("/posts", form, {
      headers: { "Content-Type": "multipart/form-data" }
    });
    navigate("/");
  };

  return (
    <div className="page create-post-page">
      <h2>Create Travel Post</h2>
      <form onSubmit={submit} className="post-form">
        <input
          value={title}
          onChange={e => setTitle(e.target.value)}
          placeholder="Title"
        />
        <textarea
          value={story}
          onChange={e => setStory(e.target.value)}
          placeholder="Your travel story"
        />
        <input
          value={location}
          onChange={e => setLocation(e.target.value)}
          placeholder="Location"
        />
        <input
          value={activityType}
          onChange={e => setActivityType(e.target.value)}
          placeholder="Activity type (hiking, cycling...)"
        />
        <input
          value={distanceKm}
          onChange={e => setDistanceKm(e.target.value)}
          placeholder="Distance (km)"
        />
        <input
          value={elevationGainM}
          onChange={e => setElevationGainM(e.target.value)}
          placeholder="Elevation gain (m)"
        />
        <input
          value={terrainVariance}
          onChange={e => setTerrainVariance(e.target.value)}
          placeholder="Terrain variance"
        />
        <div className="coords-grid">
          <input
            value={startLat}
            onChange={e => setStartLat(e.target.value)}
            placeholder="Start lat"
          />
          <input
            value={startLng}
            onChange={e => setStartLng(e.target.value)}
            placeholder="Start lng"
          />
          <input
            value={endLat}
            onChange={e => setEndLat(e.target.value)}
            placeholder="End lat"
          />
          <input
            value={endLng}
            onChange={e => setEndLng(e.target.value)}
            placeholder="End lng"
          />
        </div>
        <label>
          Image
          <input type="file" onChange={e => setImage(e.target.files[0])} />
        </label>
        <label>
          Route file (GPX/KML)
          <input type="file" onChange={e => setRouteFile(e.target.files[0])} />
        </label>
        <button type="submit">Post</button>
      </form>
    </div>
  );
}

