import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import Layout from "../components/Layout";

export default function AlarmPage() {
  const [alarms, setAlarms] = useState([]);
  const navigate = useNavigate();

  const fetchAlarms = async () => {
    try {
      const res = await api.get("/api/alarms");
      setAlarms(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleRead = async (alarm) => {
    try {
      await api.patch(`/api/alarms/${alarm.id}/read`);
      window.dispatchEvent(new Event("alarmRead"));
      if (alarm.postUuid) {
        navigate(`/posts/${alarm.postUuid}`);
      } else {
        navigate("/feed");
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchAlarms();
  }, []);

  return (
    <Layout>
      <div className="space-y-3">
        <h2 className="text-lg font-semibold text-gray-700">🔔 알림</h2>
        {alarms.length === 0 && (
          <p className="text-center text-gray-400 text-sm py-10">
            알림이 없어요
          </p>
        )}
        {alarms.map((alarm) => (
          <div
            key={alarm.id}
            className={`bg-white rounded-2xl shadow-sm p-4 cursor-pointer hover:shadow-md transition ${
              !alarm.read ? "border-l-4 border-coral" : ""
            }`}
            onClick={() => handleRead(alarm)}
          >
            <p className="text-sm text-gray-700">{alarm.content}</p>
            <p className="text-xs text-gray-400 mt-1">
              {new Date(alarm.createdAt).toLocaleDateString("ko-KR")}
            </p>
          </div>
        ))}
      </div>
    </Layout>
  );
}
