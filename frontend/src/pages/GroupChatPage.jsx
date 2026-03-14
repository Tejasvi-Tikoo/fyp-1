import { useContext, useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import api from "../services/api.js";
import { AuthContext } from "../context/AuthContext.jsx";

export default function GroupChatPage() {
  const { id } = useParams();
  const groupId = Number(id);
  const { user } = useContext(AuthContext);
  const [group, setGroup] = useState(null);
  const [members, setMembers] = useState([]);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const clientRef = useRef(null);

  useEffect(() => {
    const load = async () => {
      const [gRes, mRes] = await Promise.all([
        api.get(`/groups/${groupId}`),
        api.get(`/messages/groups/${groupId}`)
      ]);
      setGroup(gRes.data.group);
      setMembers(gRes.data.members);
      setMessages(mRes.data || []);
    };
    load();
  }, [groupId]);

  useEffect(() => {
    if (!user) return;
    const socket = new SockJS(import.meta.env.VITE_WS_URL || "http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/groups/${groupId}`, msg => {
          const body = JSON.parse(msg.body);
          setMessages(prev => [...prev, body]);
        });
      }
    });
    client.activate();
    clientRef.current = client;
    return () => client.deactivate();
  }, [groupId, user]);

  const send = e => {
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
    setMessages(prev => [...prev, { ...payload, timestamp: new Date().toISOString() }]);
    setText("");
  };

  if (!group) return <div className="page">Loading...</div>;

  return (
    <div className="page group-chat-page">
      <h2>{group.name}</h2>
      <p>{group.location}</p>
      <p>{group.tripPlan}</p>
      <div className="group-layout">
        <aside className="group-sidebar">
          <h4>Members</h4>
          {members.map(m => (
            <div key={m.id}>{m.user.username}</div>
          ))}
        </aside>
        <main className="group-main">
          <div className="chat-messages">
            {messages.map((m, i) => (
              <div
                key={i}
                className={
                  m.senderId === user.userId ? "message own" : "message"
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
              placeholder="Message group..."
            />
            <button type="submit">Send</button>
          </form>
        </main>
      </div>
    </div>
  );
}


