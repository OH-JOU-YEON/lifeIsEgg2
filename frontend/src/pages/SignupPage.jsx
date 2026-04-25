import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";
import eggsLogo from "../assets/eggs.png";

export default function SignupPage() {
  const [form, setForm] = useState({
    email: "",
    password: "",
    nickname: "",
    age: "",
  });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSignup = async () => {
    try {
      await api.post("/api/auth/signup", { ...form, age: Number(form.age) });
      navigate("/login");
    } catch (e) {
      setError("회원가입에 실패했습니다. 입력값을 확인해주세요.");
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
          <span className="text-2xl font-wisylist text-gray-800">회원가입</span>
        </div>
        {["email", "password", "nickname", "age"].map((field) => (
          <input
            key={field}
            className="w-full border rounded-lg px-4 py-2 mb-3 focus:outline-none focus:ring-2 focus:ring-peach"
            type={
              field === "password"
                ? "password"
                : field === "age"
                  ? "number"
                  : "text"
            }
            name={field}
            placeholder={
              {
                email: "이메일",
                password: "비밀번호",
                nickname: "닉네임",
                age: "나이",
              }[field]
            }
            value={form[field]}
            onChange={handleChange}
          />
        ))}
        {error && <p className="text-red-400 text-sm mb-2">{error}</p>}
        <button
          className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition"
          onClick={handleSignup}
        >
          가입하기
        </button>
        <p className="text-center text-sm text-gray-400 mt-4">
          이미 계정이 있으신가요?{" "}
          <Link to="/login" className="text-coral font-semibold">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
}
