import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { Bell, Home, Target, Calendar, BarChart2 } from "lucide-react";
import api from "../api/axios";
import eggsLogo from "../assets/eggs.png";

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
    { path: "/feed", label: "피드", icon: Home },
    { path: "/goals", label: "목표", icon: Target },
    { path: "/schedule", label: "일정", icon: Calendar },
    { path: "/dashboard", label: "대시보드", icon: BarChart2 },
  ];

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* 헤더 */}
      <header className="bg-white shadow-sm px-5 py-3 flex items-center justify-between sticky top-0 z-10 border-b border-orange-100">
        <div
          className="flex items-center gap-2 cursor-pointer"
          onClick={() => navigate("/feed")}
        >
          <img
            src={eggsLogo}
            alt="삶은달걀 로고"
            className="w-10 h-10 object-contain"
          />
          <span className="text-xl font-wisylist text-gray-800">삶은달걀</span>
        </div>
        <div className="flex items-center gap-2">
          <button
            className="relative w-9 h-9 flex items-center justify-center text-gray-400 hover:text-coral transition rounded-full hover:bg-peach"
            onClick={() => navigate("/alarms")}
          >
            <Bell size={20} />
            {unreadCount > 0 && (
              <span className="absolute top-0.5 right-0.5 bg-coral text-white text-xs rounded-full w-4 h-4 flex items-center justify-center">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </button>
          <button
            className="bg-coral text-white text-sm px-4 py-2 rounded-full hover:bg-salmon transition font-medium"
            onClick={() => navigate("/posts/new")}
          >
            일기 쓰기
          </button>
        </div>
      </header>

      {/* 본문 */}
      <main className="flex-1 bg-cream w-full">
        <div className="max-w-xl mx-auto px-4 py-6">{children}</div>
      </main>

      {/* 하단 네비 */}
      <nav className="bg-white border-t sticky bottom-0 z-10">
        <div className="flex justify-around max-w-xl mx-auto">
          {navItems.map(({ path, label, icon: Icon }) => {
            const active = location.pathname === path;
            return (
              <button
                key={path}
                className={`flex flex-col items-center py-2 px-4 text-xs transition ${
                  active ? "text-coral font-semibold" : "text-gray-400"
                }`}
                onClick={() => navigate(path)}
              >
                <Icon size={20} className="mb-0.5" />
                {label}
              </button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
