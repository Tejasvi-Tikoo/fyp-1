import { useEffect, useState } from "react";
import api from "../services/api.js";
import GroupChatPage from "./GroupChatPage.jsx";
import CreateGroup from "../pages/CreateGroup.jsx";

export default function GroupsPage() {
  const [groups, setGroups] = useState([]);
  const [activeGroup, setActiveGroup] = useState(null);
  const [showCreate, setShowCreate] = useState(false);
  const [loading, setLoading] = useState(true);

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

  return (
    <div className="page chat-page">
      <h2>Groups</h2>

      <div className="chat-layout">

        {/* LEFT SIDEBAR */}
        <aside className="chat-sidebar">

          <button onClick={() => setShowCreate(true)}>
            + Create Group
          </button>

          {loading ? (
            <p>Loading...</p>
          ) : groups.length === 0 ? (
            <p>No groups yet</p>
          ) : (
            groups.map(g => (
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
            <p>Select a group</p>
          )}

        </main>

      </div>
    </div>
  );
}