import { useEffect, useState } from "react";
import api from "../services/api.js";
import GroupChatPage from "./GroupChatPage.jsx";
import CreateGroup from "../pages/CreateGroup.jsx";

export default function GroupsPage() {
  const [groups, setGroups] = useState([]);
  const [activeGroup, setActiveGroup] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState("");

  const loadGroups = async () => {
    try {
      const res = await api.get("/groups");

      console.log("GROUPS API:", res.data);

      // 🔥 normalize backend response safely
      const list = res.data.map(g => g.group || g);

      setGroups(list);
    } catch (err) {
      console.error("Failed to load groups", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGroups();
  }, []);

  const filteredGroups = groups.filter(g =>
    (g.name || "").toLowerCase().includes(query.toLowerCase())
  );

  return (
    <div className="page chat-page">
      <div className="page-banner">
        <img src="https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1400&q=80" alt="Groups banner" />
        <div className="page-banner-overlay">
          <h2>Groups</h2>
          <p>Create community rooms and coordinate routes together.</p>
        </div>
      </div>

      <div className="chat-layout">
        {/* LEFT SIDEBAR */}
        <aside className="chat-sidebar">
          <div className="sidebar-head">
            <button onClick={() => setShowCreate(true)}>
              + Create Group
            </button>
          </div>
          <input
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="Search groups..."
          />

          {loading ? (
            <p>Loading...</p>
          ) : filteredGroups.length === 0 ? (
            <p>No groups yet</p>
          ) : (
            filteredGroups.map(g => (
              <button
                key={g.id}
                className={
                  activeGroup?.id === g.id ? "peer active" : "peer"
                }
                onClick={() => {
                  setActiveGroup(g);
                  setShowCreate(false);
                }}
              >
                {g.name}
              </button>
            ))
          )}

        </aside>

        {/* RIGHT PANEL */}
        <main className="chat-main">
          <div className="chat-main-head">
            <h3>
              {showCreate ? "Create New Group" : activeGroup ? activeGroup.name : "Pick a Group"}
            </h3>
            {!showCreate && !activeGroup && <p>Select any group from the left panel to open chat.</p>}
          </div>

          {showCreate ? (
            <CreateGroup
              onCreated={(g) => {
                setShowCreate(false);
                loadGroups();
                setActiveGroup(g);
              }}
            />
          ) : activeGroup ? (
            <GroupChatPage
              groupId={activeGroup.id}
              onDeleted={() => {
                setActiveGroup(null);
                loadGroups();
              }}
            />
          ) : (
            <div className="group-empty-state">
              <img src="https://images.unsplash.com/photo-1529156069898-49953e39b3ac?w=800&q=80" alt="Group chat placeholder" />
              <p>Start a group conversation and plan routes with your friends.</p>
            </div>
          )}

        </main>

      </div>
    </div>
  );
}