import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";
import eggsLogo from "../assets/eggs.png";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleLogin = async () => {
    try {
      const res = await api.post("/api/auth/login", { email, password });
      localStorage.setItem("token", res.data.accessToken);
      navigate("/feed");
    } catch (e) {
      setError("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
  };

  return (
    <div className="min-h-screen bg-cream flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-md p-8 w-full max-w-sm">
        <div className="flex flex-col items-center gap-2 mb-6">
          <img
            src={eggsLogo}
            alt="삶은달걀 로고"
            className="w-16 h-16 object-contain"
          />
          <span className="text-2xl font-wisylist text-gray-800">삶은달걀</span>
        </div>
        <input
          className="w-full border rounded-lg px-4 py-2 mb-3 focus:outline-none focus:ring-2 focus:ring-peach"
          type="email"
          placeholder="이메일"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <input
          className="w-full border rounded-lg px-4 py-2 mb-3 focus:outline-none focus:ring-2 focus:ring-peach"
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <p className="text-red-400 text-sm mb-2">{error}</p>}
        <button
          className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition"
          onClick={handleLogin}
        >
          로그인
        </button>
        <p className="text-center text-sm text-gray-400 mt-4">
          계정이 없으신가요?{" "}
          <Link to="/signup" className="text-coral font-semibold">
            회원가입
          </Link>
        </p>
      </div>
    </div>
  );
}
