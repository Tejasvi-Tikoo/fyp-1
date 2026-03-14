import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api.js";

export default function GroupsPage() {
  const [memberships, setMemberships] = useState([]);
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [tripPlan, setTripPlan] = useState("");
  const navigate = useNavigate();

  const load = async () => {
    const res = await api.get("/groups");
    setMemberships(res.data);
  };

  useEffect(() => {
    load();
  }, []);

  const createGroup = async e => {
    e.preventDefault();
    const res = await api.post("/groups", { name, location, tripPlan });
    setName("");
    setLocation("");
    setTripPlan("");
    navigate(`/groups/${res.data.id}`);
  };

  return (
    <div className="page groups-page">
      <h2>Travel Groups</h2>
      <form className="post-form" onSubmit={createGroup}>
        <input
          value={name}
          onChange={e => setName(e.target.value)}
          placeholder="Group name"
        />
        <input
          value={location}
          onChange={e => setLocation(e.target.value)}
          placeholder="Location"
        />
        <textarea
          value={tripPlan}
          onChange={e => setTripPlan(e.target.value)}
          placeholder="Trip plan (dates, routes, notes)"
        />
        <button type="submit">Create Group</button>
      </form>

      <h3>Your Groups</h3>
      <div className="groups-list">
        {memberships.map(m => (
          <button
            key={m.id}
            className="group-card"
            onClick={() => navigate(`/groups/${m.group.id}`)}
          >
            <strong>{m.group.name}</strong>
            <div>{m.group.location}</div>
          </button>
        ))}
      </div>
    </div>
  );
}


