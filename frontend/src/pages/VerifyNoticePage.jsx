import { useNavigate } from "react-router-dom";
import eggsLogo from "../assets/eggs.png";

export default function VerifyNoticePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-cream flex items-center justify-center">
      <div className="bg-white rounded-2xl shadow-md p-8 w-full max-w-sm text-center">
        <div className="flex flex-col items-center gap-2 mb-6">
          <img
            src={eggsLogo}
            alt="삶은달걀 로고"
            className="w-16 h-16 object-contain"
          />
          <span className="text-2xl font-wisylist text-gray-800">
            이메일 인증
          </span>
        </div>
        <p className="text-gray-600 mb-2">
          가입하신 이메일로 인증 메일을 보냈어요.
        </p>
        <p className="text-gray-400 text-sm mb-6">
          메일함을 확인하고 링크를 클릭해주세요.
        </p>
        <button
          className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition"
          onClick={() => navigate("/login")}
        >
          로그인하러 가기
        </button>
      </div>
    </div>
  );
}
