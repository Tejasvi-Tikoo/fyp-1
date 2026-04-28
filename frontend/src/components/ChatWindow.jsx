import { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import api from "../services/api.js";

export default function ChatWindow({ currentUser, peerUser }) {
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const clientRef = useRef(null);
  const messagesEndRef = useRef(null);

  const formatMessageTime = (timestamp) => {
    if (!timestamp) return "";
    const d = new Date(timestamp);
    if (Number.isNaN(d.getTime())) return "";
    return d.toLocaleString([], {
      day: "2-digit",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  // =====================
  // LOAD HISTORY
  // =====================
  useEffect(() => {
    if (!peerUser) return;

    api.get(`/messages/direct/${peerUser.userId}`)
      .then(res => setMessages(res.data || []))
      .catch(err => {
        console.error("Failed to load direct messages:", err.response?.data || err.message);
        setMessages([]);
      });
  }, [peerUser.userId]);

  // =====================
  // WEBSOCKET – real-time updates
  // =====================
  useEffect(() => {
    if (!peerUser) return;

    const socket = new SockJS(import.meta.env.VITE_WS_URL || "http://localhost:8080/ws");
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Connected to WebSocket");

        // Subscribe to your personal queue
        client.subscribe("/user/queue/messages", (msg) => {
          const body = JSON.parse(msg.body);

          // Only add messages related to current peer
          if (
            body.senderId === peerUser.userId ||
            body.receiverId === peerUser.userId
          ) {
            setMessages(prev => {
              // Replace a matching optimistic message so sent messages appear instantly
              const pendingIndex = prev.findIndex(
                m =>
                  m.pending &&
                  m.senderId === body.senderId &&
                  m.receiverId === body.receiverId &&
                  m.content === body.content
              );

              if (pendingIndex !== -1) {
                const next = [...prev];
                next[pendingIndex] = body;
                return next;
              }

              return [...prev, body];
            });
          }
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => client.deactivate();
  }, [peerUser.userId]);

  // =====================
  // SEND MESSAGE
  // =====================
  const send = (e) => {
    e.preventDefault();
    if (!text.trim() || !clientRef.current) return;

    const content = text.trim();

    const payload = {
      senderId: currentUser.userId,
      receiverId: peerUser.userId,
      content,
      type: "DIRECT",
    };

    setMessages(prev => [
      ...prev,
      {
        ...payload,
        pending: true,
      },
    ]);

    clientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify(payload),
    });

    setText("");
  };

  // =====================
  // AUTO SCROLL TO BOTTOM
  // =====================
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  return (
    <div className="chat-window">
      <div className="chat-header">{peerUser.username}</div>

      <div className="chat-messages">
        {messages.map((m, i) => (
          <div
            key={i}
            className={m.senderId === currentUser.userId ? "message own" : "message"}
          >
            <div>{m.content}</div>
            <small className="message-time">
              {m.pending ? "Sending..." : formatMessageTime(m.timestamp)}
            </small>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      <form className="chat-input" onSubmit={send}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Type a message..."
        />
        <button type="submit">Send</button>
      </form>
    </div>
  );
}