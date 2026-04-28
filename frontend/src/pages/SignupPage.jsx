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

  const validate = () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const passwordRegex =
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,20}$/;

    if (!emailRegex.test(form.email)) {
      setError("올바른 이메일 형식을 입력해주세요.");
      return false;
    }
    if (!passwordRegex.test(form.password)) {
      setError("비밀번호는 8~20자, 영문/숫자/특수문자를 포함해야 합니다.");
      return false;
    }
    return true;
  };

  const handleSignup = async () => {
    if (!validate()) return;
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
          <div key={field}>
            <input
              className="w-full border rounded-lg px-4 py-2 mb-1 focus:outline-none focus:ring-2 focus:ring-peach"
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
            {field === "email" && (
              <p className="text-xs text-gray-400 mb-2">
                example@email.com 형식으로 입력해주세요
              </p>
            )}
            {field === "password" && (
              <p className="text-xs text-gray-400 mb-2">
                8~20자, 영문/숫자/특수문자(@$!%*#?&) 포함
              </p>
            )}
          </div>
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
