import { useEffect, useState } from "react";
import api from "../services/api.js";
import MapView from "../components/MapView.jsx";

export default function ExploreRoutesPage() {
  const [routes, setRoutes] = useState([]);
  const [activityType, setActivityType] = useState("");
  const [difficulty, setDifficulty] = useState("");
  const [location, setLocation] = useState("");

  const search = async e => {
    if (e) e.preventDefault();
    const params = {};
    if (activityType) params.activityType = activityType;
    if (difficulty) params.difficulty = difficulty;
    if (location) params.location = location;
    const res = await api.get("/routes/search", { params });
    setRoutes(res.data);
  };

  useEffect(() => {
    search();
  }, []);

  return (
    <div className="page explore-page">
      <div className="page-banner">
        <img src="https://picsum.photos/seed/explore-mountains/1400/500" alt="Explore routes banner" />
        <div className="page-banner-overlay">
          <h2>Explore Routes</h2>
          <p>Filter trails by activity, difficulty, and location.</p>
        </div>
      </div>
      <form onSubmit={search} className="filter-form">
        <input
          value={activityType}
          onChange={e => setActivityType(e.target.value)}
          placeholder="Activity"
        />
        <select value={difficulty} onChange={e => setDifficulty(e.target.value)}>
          <option value="">Any difficulty</option>
          <option value="Easy">Easy</option>
          <option value="Moderate">Moderate</option>
          <option value="Strenuous">Strenuous</option>
          <option value="Expert">Expert</option>
        </select>
        <input
          value={location}
          onChange={e => setLocation(e.target.value)}
          placeholder="Location"
        />
        <button type="submit">Search</button>
      </form>
      <div className="routes-list">
        {routes.map(r => (
          <div key={r.id} className="route-card">
            <img
              className="route-cover"
              src={`https://picsum.photos/seed/route-${r.id}/900/350`}
              alt={`${r.activityType} route`}
            />
            <h3>{r.activityType}</h3>
            <p>{r.location}</p>
            <p>
              {r.distanceKm} km, {r.elevationGainM} m gain
            </p>
            <p>Difficulty: {r.difficultyCategory}</p>
            {r.startLat != null && (
              <div style={{ marginTop: "0.5rem" }}>
                <MapView route={r} height={200} />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}


