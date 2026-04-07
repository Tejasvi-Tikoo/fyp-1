import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../context/AuthContext.jsx";
import api from "../services/api.js";
import ChatWindow from "../components/ChatWindow.jsx";

export default function ChatPage() {
  const { user } = useContext(AuthContext);

  const [peers, setPeers] = useState([]);
  const [activePeer, setActivePeer] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    const loadUsers = async () => {
      try {
        const res = await api.get("/users");

        console.log("USERS API:", res.data);
        console.log("CURRENT USER:", user);

        // 🔥 Normalize backend response (handles multiple formats)
        const normalized = res.data
          .map(u => ({
            userId: u.id,
            username: u.username
          }))
          .filter(u => u.userId && u.userId !== user.userId);

        setPeers(normalized);
      } catch (err) {
        console.error("Failed to load users", err);
      } finally {
        setLoading(false);
      }
    };

    loadUsers();
  }, [user]);

  if (!user) return null;

  return (
    <div className="page chat-page">
      <h2>Direct Messages</h2>

      <div className="chat-layout">

        {/* LEFT SIDEBAR */}
        <aside className="chat-sidebar">
          {loading ? (
            <p>Loading users...</p>
          ) : peers.length === 0 ? (
            <p>No users found</p>
          ) : (
            peers.map(p => (
              <button
                key={p.userId}
                className={
                  activePeer?.userId === p.userId
                    ? "peer active"
                    : "peer"
                }
                onClick={() => setActivePeer(p)}
              >
                {p.username}
              </button>
            ))
          )}
        </aside>

        {/* RIGHT PANEL */}
        <main className="chat-main">
          {activePeer ? (
            <ChatWindow
              currentUser={user}
              peerUser={activePeer}
            />
          ) : (
            <p>Select a user to start chatting</p>
          )}
        </main>

      </div>
    </div>
  );
}