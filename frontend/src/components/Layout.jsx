import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import api from "../api/axios";

export default function Layout({ children }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [unreadCount, setUnreadCount] = useState(0);

  const fetchUnreadCount = async () => {
    try {
      const res = await api.get("/api/alarms/count");
      setUnreadCount(res.data.data.count);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchUnreadCount();
    const interval = setInterval(fetchUnreadCount, 30000);
    window.addEventListener("alarmRead", fetchUnreadCount);
    return () => {
      clearInterval(interval);
      window.removeEventListener("alarmRead", fetchUnreadCount);
    };
  }, []);

  const navItems = [
    { path: "/feed", label: "피드", icon: "🏠" },
    { path: "/goals", label: "목표", icon: "🎯" },
    { path: "/schedule", label: "일정", icon: "📅" },
    { path: "/dashboard", label: "대시보드", icon: "📊" },
  ];

  return (
    <div className="min-h-screen bg-cream flex flex-col">
      {/* 헤더 */}
      <header className="bg-white shadow-sm px-4 py-3 flex items-center justify-between sticky top-0 z-10">
        <h1
          className="text-xl font-wisylist text-coral cursor-pointer"
          onClick={() => navigate("/feed")}
        >
          🥚 삶은달걀
        </h1>
        <div className="flex items-center gap-3">
          <button
            className="relative text-gray-400 hover:text-coral transition"
            onClick={() => navigate("/alarms")}
          >
            🔔
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 bg-coral text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </button>
          <button
            className="bg-coral text-white text-sm px-4 py-1.5 rounded-full hover:bg-salmon transition"
            onClick={() => navigate("/posts/new")}
          >
            일기 쓰기
          </button>
        </div>
      </header>

      {/* 본문 */}
      <main className="flex-1 max-w-xl w-full mx-auto px-4 py-6">
        {children}
      </main>

      {/* 하단 네비 */}
      <nav className="bg-white border-t sticky bottom-0 z-10">
        <div className="flex justify-around max-w-xl mx-auto">
          {navItems.map((item) => (
            <button
              key={item.path}
              className={`flex flex-col items-center py-2 px-4 text-xs transition ${
                location.pathname === item.path
                  ? "text-coral font-semibold"
                  : "text-gray-400"
              }`}
              onClick={() => navigate(item.path)}
            >
              <span className="text-lg">{item.icon}</span>
              {item.label}
            </button>
          ))}
        </div>
      </nav>
    </div>
  );
}
