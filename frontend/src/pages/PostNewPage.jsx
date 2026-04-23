import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import Layout from "../components/Layout";

export default function PostNewPage() {
  const [content, setContent] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async () => {
    if (!content.trim()) {
      setError("내용을 입력해주세요.");
      return;
    }
    try {
      await api.post("/api/posts", {
        content,
        title: "",
        visibility: "PUBLIC",
      });
      navigate("/feed");
    } catch (e) {
      const message = e.response?.data?.message || "일기 작성에 실패했습니다.";
      setError(message);
    }
  };

  return (
    <Layout>
      <div className="bg-white rounded-2xl shadow-sm p-5">
        <h2 className="text-lg font-semibold text-gray-700 mb-4">
          오늘의 일기 🥚
        </h2>
        <textarea
          className="w-full h-60 border rounded-xl px-4 py-3 text-gray-700 resize-none focus:outline-none focus:ring-2 focus:ring-peach"
          placeholder="오늘 준비하면서 있었던 일을 적어보세요."
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        {error && <p className="text-red-400 text-sm mt-2">{error}</p>}
        <button
          className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition mt-4"
          onClick={handleSubmit}
        >
          작성 완료
        </button>
      </div>
    </Layout>
  );
}
