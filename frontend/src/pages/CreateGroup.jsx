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
        <div>
            <h3>Create Group</h3>

            <form onSubmit={create}>
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
                    placeholder="Trip plan"
                />

                <h4>Select Members</h4>
                <div className="user-select">
                    {users.map(u => (
                        <label key={u.id}>
                            <input
                                type="checkbox"
                                onChange={() => toggleUser(u.id)}
                            />
                            {u.username}
                        </label>
                    ))}
                </div>

                <button type="submit">Create</button>
            </form>
        </div>
    );
}