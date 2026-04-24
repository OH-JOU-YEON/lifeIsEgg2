import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import Layout from "../components/Layout";

export default function PostDetailPage() {
  const { uuid } = useParams();
  const [post, setPost] = useState(null);
  const [cheers, setCheers] = useState([]);
  const [cheerContent, setCheerContent] = useState("");
  const [replyTo, setReplyTo] = useState(null); // { id, content }
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
      const res = await api.get(`/api/posts/${uuid}/cheers`);
      setCheers(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCheer = async () => {
    if (!cheerContent.trim()) return;
    try {
      await api.post(`/api/posts/${uuid}/cheers`, {
        content: cheerContent,
        parentId: replyTo ? replyTo.id : null,
      });
      setCheerContent("");
      setReplyTo(null);
      fetchCheers();
    } catch (e) {
      const message = e.response?.data?.message || "응원 작성에 실패했습니다.";
      alert(message);
    }
  };

  const handleDelete = async (cheerId) => {
    try {
      await api.delete(`/api/cheers/${cheerId}`);
      fetchCheers();
    } catch (e) {
      const message = e.response?.data?.message || "삭제에 실패했습니다.";
      alert(message);
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
        {/* 일기 본문 */}
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <p className="text-sm text-gray-400 mb-2">
            {new Date(post.createdAt).toLocaleDateString("ko-KR")}
          </p>
          <p className="text-gray-700 whitespace-pre-wrap">{post.content}</p>
        </div>

        {/* 응원 작성 */}
        <div className="bg-white rounded-2xl shadow-sm p-5">
          <h3 className="text-sm font-semibold text-gray-600 mb-3">
            🤍{" "}
            {replyTo
              ? `"${replyTo.content.slice(0, 20)}..."에 답글`
              : "응원 남기기"}
          </h3>
          {replyTo && (
            <button
              className="text-xs text-gray-400 mb-2 hover:text-coral"
              onClick={() => setReplyTo(null)}
            >
              답글 취소
            </button>
          )}
          <textarea
            className="w-full h-24 border rounded-xl px-4 py-3 text-gray-700 resize-none focus:outline-none focus:ring-2 focus:ring-peach"
            placeholder={
              post.isOwner
                ? "본인 일기에는 응원할 수 없어요"
                : "따뜻한 응원을 남겨보세요."
            }
            value={cheerContent}
            onChange={(e) => setCheerContent(e.target.value)}
            disabled={post.isOwner}
          />
          <button
            className="w-full bg-coral text-white rounded-lg py-2 font-semibold hover:bg-salmon transition mt-2 disabled:opacity-50"
            onClick={handleCheer}
            disabled={post.isOwner}
          >
            응원하기
          </button>
        </div>

        {/* 응원 목록 */}
        <div className="space-y-3">
          {cheers.map((cheer) => (
            <div key={cheer.id} className="bg-white rounded-2xl shadow-sm p-4">
              <p className="text-sm text-gray-700">{cheer.content}</p>
              <div className="flex gap-3 mt-2">
                {!post.isOwner && (
                  <button
                    className="text-xs text-gray-400 hover:text-coral"
                    onClick={() =>
                      setReplyTo({ id: cheer.id, content: cheer.content })
                    }
                  >
                    답글
                  </button>
                )}
                {post.isOwner && (
                  <button
                    className="text-xs text-gray-400 hover:text-red-400"
                    onClick={() => handleDelete(cheer.id)}
                  >
                    삭제
                  </button>
                )}
              </div>
              {/* 답글 목록 */}
              {cheer.children && cheer.children.length > 0 && (
                <div className="ml-4 mt-2 space-y-2">
                  {cheer.children.map((child) => (
                    <div key={child.id} className="bg-cream rounded-xl p-3">
                      <p className="text-sm text-gray-600">{child.content}</p>
                      {post.isOwner && (
                        <button
                          className="text-xs text-gray-400 hover:text-red-400 mt-1"
                          onClick={() => handleDelete(child.id)}
                        >
                          삭제
                        </button>
                      )}
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
