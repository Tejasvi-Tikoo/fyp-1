import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export default function ChatWindow({ currentUser, peerUser }) {
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const clientRef = useRef(null);

  useEffect(() => {
    const socket = new SockJS(import.meta.env.VITE_WS_URL || "http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/queue/messages/${currentUser.userId}`, msg => {
          const body = JSON.parse(msg.body);
          if (
            body.senderId === peerUser.userId ||
            body.receiverId === peerUser.userId
          ) {
            setMessages(prev => [...prev, body]);
          }
        });
      }
    });
    client.activate();
    clientRef.current = client;
    return () => client.deactivate();
  }, [currentUser.userId, peerUser.userId]);

  const send = e => {
    e.preventDefault();
    if (!text.trim()) return;
    const payload = {
      senderId: currentUser.userId,
      receiverId: peerUser.userId,
      content: text,
      type: "DIRECT"
    };
    clientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify(payload)
    });
    setMessages(prev => [...prev, { ...payload, timestamp: new Date().toISOString() }]);
    setText("");
  };

  return (
    <div className="chat-window">
      <div className="chat-header">{peerUser.username}</div>
      <div className="chat-messages">
        {messages.map((m, i) => (
          <div
            key={i}
            className={
              m.senderId === currentUser.userId ? "message own" : "message"
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
          placeholder="Type a message..."
        />
        <button type="submit">Send</button>
      </form>
    </div>
  );
}

