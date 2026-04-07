import { useContext, useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import api from "../services/api.js";
import { AuthContext } from "../context/AuthContext.jsx";

export default function GroupChatPage({ groupId, onDeleted }) {
  const { user } = useContext(AuthContext);

  const [group, setGroup] = useState(null);
  const [members, setMembers] = useState([]);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");

  const clientRef = useRef(null);

  // ✅ LOAD GROUP DATA
  useEffect(() => {
    const load = async () => {
      try {
        const [gRes, mRes] = await Promise.all([
          api.get(`/groups/${groupId}`),
          api.get(`/messages/groups/${groupId}`)
        ]);

        console.log("GROUP DETAIL:", gRes.data);

        setGroup(gRes.data.group || gRes.data);
        setMembers(gRes.data.members || []);
        setMessages(mRes.data || []);
      } catch (err) {
        console.error("Failed loading group", err);
      }
    };

    load();
  }, [groupId]);

  // ✅ SOCKET CONNECTION
  useEffect(() => {
    if (!user) return;

    const socket = new SockJS("http://localhost:8080/ws");

    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,

      onConnect: () => {
        console.log("Connected to group:", groupId);

        client.subscribe(`/topic/groups/${groupId}`, msg => {
          const body = JSON.parse(msg.body);

          setMessages(prev => [...prev, body]);
        });
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [groupId, user]);

  // ✅ SEND MESSAGE
  const send = (e) => {
    e.preventDefault();

    if (!text.trim()) return;

    const payload = {
      senderId: user.userId,
      groupId,
      content: text,
      type: "GROUP"
    };

    clientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify(payload)
    });

    setText(""); // no optimistic update
  };

  // ✅ DELETE GROUP (clean)
  const deleteGroup = async () => {
    if (!confirm("Delete this group?")) return;

    try {
      await api.delete(`/groups/${groupId}`);

      if (onDeleted) onDeleted(); // 🔥 notify parent
    } catch (err) {
      console.error("Delete failed", err);
    }
  };

  if (!group) return <p>Loading...</p>;

  return (
    <div className="group-chat">

      {/* HEADER */}
      <div className="group-header">
        <h3>{group.name}</h3>

        <button className="danger" onClick={deleteGroup}>
          Delete
        </button>
      </div>

      <p>{group.location}</p>
      <p>{group.tripPlan}</p>

      <div className="group-layout">

        {/* MEMBERS */}
        <aside className="group-sidebar">
          <h4>Members</h4>
          {members.length === 0 ? (
            <p>No members</p>
          ) : (
            members.map(m => (
              <div key={m.id}>
                {m.username || "Unknown"}
              </div>
            ))
          )}
        </aside>

        {/* CHAT */}
        <main className="group-main">

          <div className="chat-messages">
            {messages.map((m, i) => (
              <div
                key={i}
                className={
                  m.senderId === user.userId
                    ? "message own"
                    : "message"
                }
              >
                {m.content}
              </div>
            ))}
          </div>

          <form className="chat-input" onSubmit={send}>
            <input
              value={text}
              onChange={e => setText(e.target.value)}
              placeholder="Message..."
            />
            <button type="submit">Send</button>
          </form>

        </main>

      </div>
    </div>
  );
}