import { useEffect, useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import eggsLogo from "../assets/eggs.png";

export default function VerifyPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState("loading"); // loading | success | error
  const navigate = useNavigate();

  useEffect(() => {
    const token = searchParams.get("token");
    if (!token) {
      setStatus("error");
      return;
    }
    api
      .get(`/api/auth/verify?token=${token}`)
      .then(() => setStatus("success"))
      .catch(() => setStatus("error"));
  }, []);

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
        {status === "loading" && <p className="text-gray-400">인증 중...</p>}
        {status === "success" && (
          <>
            <p className="text-gray-600 mb-6">이메일 인증이 완료됐어요!</p>
            <button
              className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition"
              onClick={() => navigate("/login")}
            >
              로그인하러 가기
            </button>
          </>
        )}
        {status === "error" && (
          <p className="text-red-400">유효하지 않거나 만료된 링크예요.</p>
        )}
      </div>
    </div>
  );
}
