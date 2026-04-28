import { useEffect, useState } from "react";
import api from "../services/api.js";

export default function CreateGroup({ onCreated }) {
    const [users, setUsers] = useState([]);
    const [selected, setSelected] = useState([]);

    const [name, setName] = useState("");
    const [location, setLocation] = useState("");
    const [tripPlan, setTripPlan] = useState("");

    useEffect(() => {
        api.get("/users").then(res => setUsers(res.data));
    }, []);

    const toggleUser = (id) => {
        setSelected(prev =>
            prev.includes(id)
                ? prev.filter(x => x !== id)
                : [...prev, id]
        );
    };

    const create = async (e) => {
        e.preventDefault();

        const res = await api.post("/groups", {
            name,
            location,
            tripPlan,
            memberIds: selected
        });

        onCreated(res.data);
    };

    return (
        <div className="group-create-panel">
            <form onSubmit={create} className="group-create-form">
                <div className="group-form-grid">
                <input
                    value={name}
                    onChange={e => setName(e.target.value)}
                    placeholder="Group name"
                    required
                />

                <input
                    value={location}
                    onChange={e => setLocation(e.target.value)}
                    placeholder="Location"
                />

                <textarea
                    value={tripPlan}
                    onChange={e => setTripPlan(e.target.value)}
                    placeholder="Trip plan"
                    rows={4}
                />
                </div>

                <div className="member-select-head">
                    <h4>Select Members</h4>
                    <span>{selected.length} selected</span>
                </div>
                <div className="user-select">
                    {users.map(u => (
                        <label key={u.id} className="member-option">
                            <input
                                type="checkbox"
                                className="member-checkbox"
                                checked={selected.includes(u.id)}
                                onChange={() => toggleUser(u.id)}
                            />
                            <span>{u.username}</span>
                        </label>
                    ))}
                </div>

                <button type="submit">Create Group</button>
            </form>
        </div>
    );
}