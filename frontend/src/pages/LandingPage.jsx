import { useNavigate } from "react-router-dom";
import eggsLogo from "../assets/eggs.png";

export default function LandingPage() {
  const navigate = useNavigate();

  const token = localStorage.getItem("token");
  if (token) navigate("/feed");

  return (
    <div className="min-h-screen bg-cream flex flex-col items-center justify-center gap-8 px-4">
      <div className="flex flex-col items-center gap-3">
        <img
          src={eggsLogo}
          alt="삶은달걀 로고"
          className="w-24 h-24 object-contain"
        />
        <h1 className="text-4xl font-wisylist text-gray-800">삶은달걀</h1>
        <p className="text-gray-400 text-sm text-center">
          오늘 하루를 조용히 기록하고
          <br />
          서로 응원하는 공간
        </p>
      </div>
      <div className="flex flex-col gap-3 w-full max-w-xs">
        <button
          className="w-full bg-coral text-white rounded-full py-3 font-semibold hover:bg-salmon transition"
          onClick={() => navigate("/login")}
        >
          로그인
        </button>
        <button
          className="w-full bg-white text-coral border border-coral rounded-full py-3 font-semibold hover:bg-peach transition"
          onClick={() => navigate("/signup")}
        >
          회원가입
        </button>
      </div>
    </div>
  );
}
