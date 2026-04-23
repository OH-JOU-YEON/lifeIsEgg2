import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

export default function FeedPage() {
  const [posts, setPosts] = useState([]);
  const [excludeIds, setExcludeIds] = useState([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const fetchPosts = async () => {
    if (loading || !hasMore) return;
    setLoading(true);
    try {
      const res = await api.get("/api/posts/feed", {
        params: { excludeIds: excludeIds.join(",") },
      });
      const newPosts = res.data;
      if (newPosts.length === 0) {
        setHasMore(false);
      } else {
        setPosts((prev) => [...prev, ...newPosts]);
        setExcludeIds((prev) => [...prev, ...newPosts.map((p) => p.postId)]);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, []);

  return (
    <div className="min-h-screen bg-cream">
      {/* 헤더 */}
      <header className="bg-white shadow-sm px-4 py-3 flex items-center justify-between sticky top-0 z-10">
        <h1 className="text-xl font-wisylist text-coral">🥚 삶은달걀</h1>
        <button
          className="bg-coral text-white text-sm px-4 py-1.5 rounded-full hover:bg-salmon transition"
          onClick={() => navigate("/posts/new")}
        >
          일기 쓰기
        </button>
      </header>

      {/* 피드 목록 */}
      <main className="max-w-xl mx-auto px-4 py-6 space-y-4">
        {posts.map((post) => (
          <div
            key={post.postId}
            className="bg-white rounded-2xl shadow-sm p-5 cursor-pointer hover:shadow-md transition"
            onClick={() => navigate(`/posts/${post.uuid}`)}
          >
            <p className="text-sm text-gray-400 mb-2">
              {post.age}세 ·{" "}
              {new Date(post.createdAt).toLocaleDateString("ko-KR")}
            </p>
            <p className="text-gray-700 line-clamp-3">{post.content}</p>
            <div className="flex items-center gap-2 mt-3 text-sm text-coral">
              <span>🤍 응원 {post.cheerCount}</span>
            </div>
          </div>
        ))}

        {hasMore && (
          <button
            className="w-full py-3 text-coral text-sm font-semibold hover:opacity-70 transition"
            onClick={fetchPosts}
            disabled={loading}
          >
            {loading ? "불러오는 중..." : "더 보기"}
          </button>
        )}

        {!hasMore && (
          <p className="text-center text-gray-400 text-sm py-4">
            모든 일기를 불러왔어요 🥚
          </p>
        )}
      </main>
    </div>
  );
}
