import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import Layout from "../components/Layout";

export default function PostDetailPage() {
  const { uuid } = useParams();
  const [post, setPost] = useState(null);
  const [cheers, setCheers] = useState([]);
  const [cheerContent, setCheerContent] = useState("");
  const navigate = useNavigate();

  const fetchPost = async () => {
    try {
      const res = await api.get(`/api/posts/${uuid}`);
      setPost(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchCheers = async () => {
    try {
      const res = await api.get(`/api/cheers/${uuid}`);
      setCheers(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCheer = async () => {
    if (!cheerContent.trim()) return;
    try {
      await api.post(`/api/cheers/${uuid}`, { content: cheerContent });
      setCheerContent("");
      fetchCheers();
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchPost();
    fetchCheers();
  }, [uuid]);

  if (!post)
    return (
      <Layout>
        <p className="text-center text-gray-400 mt-10">불러오는 중...</p>
      </Layout>
    );

  return (
    <Layout>
      <div className="space-y-4">
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <p className="text-sm text-gray-400 mb-2">
            {new Date(post.createdAt).toLocaleDateString("ko-KR")}
          </p>
          <p className="text-gray-700 whitespace-pre-wrap">{post.content}</p>
        </div>

        <div className="bg-white rounded-2xl shadow-sm p-5">
          <h3 className="text-sm font-semibold text-gray-600 mb-3">
            🤍 응원 남기기
          </h3>
          <textarea
            className="w-full h-24 border rounded-xl px-4 py-3 text-gray-700 resize-none focus:outline-none focus:ring-2 focus:ring-peach"
            placeholder="따뜻한 응원을 남겨보세요."
            value={cheerContent}
            onChange={(e) => setCheerContent(e.target.value)}
          />
          <button
            className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition mt-2"
            onClick={handleCheer}
          >
            응원하기
          </button>
        </div>

        <div className="space-y-3">
          {cheers.map((cheer) => (
            <div
              key={cheer.cheerId}
              className="bg-white rounded-2xl shadow-sm p-4"
            >
              <p className="text-sm text-gray-700">{cheer.content}</p>
              {cheer.children && cheer.children.length > 0 && (
                <div className="ml-4 mt-2 space-y-2">
                  {cheer.children.map((child) => (
                    <div
                      key={child.cheerId}
                      className="bg-cream rounded-xl p-3"
                    >
                      <p className="text-sm text-gray-600">{child.content}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </Layout>
  );
}
