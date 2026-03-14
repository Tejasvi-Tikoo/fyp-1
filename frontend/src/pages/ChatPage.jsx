import { useContext, useEffect, useState } from "react";
import { AuthContext } from "../context/AuthContext.jsx";
import api from "../services/api.js";
import ChatWindow from "../components/ChatWindow.jsx";

export default function ChatPage() {
  const { user } = useContext(AuthContext);
  const [peers, setPeers] = useState([]);
  const [activePeer, setActivePeer] = useState(null);

  useEffect(() => {
    const load = async () => {
      const res = await api.get("/users");
      const list = res.data
        .filter(u => u.id !== user.userId)
        .map(u => ({ userId: u.id, username: u.username }));
      setPeers(list);
    };
    if (user) load();
  }, [user]);

  if (!user) return null;

  return (
    <div className="page chat-page">
      <h2>Direct Messages</h2>
      <div className="chat-layout">
        <aside className="chat-sidebar">
          {peers.map(p => (
            <button
              key={p.userId}
              className={
                activePeer && activePeer.userId === p.userId ? "peer active" : "peer"
              }
              onClick={() => setActivePeer(p)}
            >
              {p.username}
            </button>
          ))}
        </aside>
        <main className="chat-main">
          {activePeer ? (
            <ChatWindow currentUser={user} peerUser={activePeer} />
          ) : (
            <p>Select a user to start chatting.</p>
          )}
        </main>
      </div>
    </div>
  );
}


