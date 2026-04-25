import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import api from "../api/axios";

const CATEGORY_COLORS = {
  자격증: "bg-amber-100 text-amber-700",
  코딩: "bg-blue-100 text-blue-700",
  취준: "bg-coral/10 text-coral",
  독서: "bg-green-100 text-green-700",
  기타: "bg-gray-100 text-gray-500",
};

function categoryStyle(cat) {
  return CATEGORY_COLORS[cat] ?? "bg-gray-100 text-gray-500";
}

const EMPTY_FORM = {
  title: "",
  targetValue: "",
  unit: "",
  category: "",
  startDate: "",
  endDate: "",
};

export default function GoalsPage() {
  const [goals, setGoals] = useState([]);
  const [showCompleted, setShowCompleted] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(false);
  const [formError, setFormError] = useState("");

  const fetchGoals = async () => {
    try {
      const res = await api.get(`/api/goals?completed=${showCompleted}`);
      setGoals(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchGoals();
  }, [showCompleted]);

  const openCreate = () => {
    setEditTarget(null);
    setForm(EMPTY_FORM);
    setFormError("");
    setShowModal(true);
  };

  const openEdit = (goal) => {
    setEditTarget(goal);
    setForm({
      title: goal.title,
      targetValue: goal.targetValue,
      unit: goal.unit,
      category: goal.category ?? "",
      startDate: goal.startDate,
      endDate: goal.endDate,
    });
    setShowModal(true);
  };

  const handleSubmit = async () => {
    if (
      !form.title ||
      !form.targetValue ||
      !form.unit ||
      !form.startDate ||
      !form.endDate
    ) {
      setFormError("모든 필수 항목을 입력해주세요 (제목, 수치, 단위, 날짜)");
      return;
    }
    setFormError("");
    // ... 나머지 동일
  };

  const handleProgress = async (goalId, increment) => {
    try {
      const res = await api.patch(
        `/api/goals/${goalId}/progress?increment=${increment}`,
      );
      setGoals((prev) =>
        prev.map((g) => (g.id === goalId ? res.data.data : g)),
      );
    } catch (e) {
      console.error(e);
    }
  };

  const handleDelete = async (goalId) => {
    if (!window.confirm("목표를 삭제할까요?")) return;
    try {
      await api.delete(`/api/goals/${goalId}`);
      fetchGoals();
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Layout>
      <div className="flex flex-col gap-4">
        {/* 상단 컨트롤 */}
        <div className="flex items-center justify-between">
          <div className="flex gap-2">
            <button
              className={`text-sm px-3 py-1 rounded-full border transition ${
                !showCompleted
                  ? "bg-coral text-white border-coral"
                  : "text-gray-400 border-gray-200"
              }`}
              onClick={() => setShowCompleted(false)}
            >
              진행 중
            </button>
            <button
              className={`text-sm px-3 py-1 rounded-full border transition ${
                showCompleted
                  ? "bg-coral text-white border-coral"
                  : "bg-white text-gray-400 border-gray-200"
              }`}
              onClick={() => setShowCompleted(true)}
            >
              완료
            </button>
          </div>
          <button
            className="bg-coral text-white text-sm px-4 py-1.5 rounded-full hover:bg-salmon transition"
            onClick={openCreate}
          >
            + 목표 추가
          </button>
        </div>

        {/* 목표 카드 목록 */}
        {goals.length === 0 ? (
          <div className="text-center text-gray-400 text-sm py-16">
            {showCompleted ? "완료된 목표가 없어요" : "목표를 추가해보세요 🥚"}
          </div>
        ) : (
          goals.map((goal) => (
            <div
              key={goal.id}
              className="bg-white rounded-2xl shadow-sm p-4 flex flex-col gap-3"
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex flex-col gap-1">
                  <span className="font-semibold text-gray-800">
                    {goal.title}
                  </span>
                  {goal.category && (
                    <span
                      className={`text-xs px-2 py-0.5 rounded-full self-start ${categoryStyle(goal.category)}`}
                    >
                      {goal.category}
                    </span>
                  )}
                </div>
                <div className="flex gap-2 text-xs text-gray-400 shrink-0">
                  <button
                    onClick={() => openEdit(goal)}
                    className="hover:text-coral transition"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDelete(goal.id)}
                    className="hover:text-red-400 transition"
                  >
                    삭제
                  </button>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs text-gray-500 mb-1">
                  <span>
                    {goal.currentValue} / {goal.targetValue} {goal.unit}
                  </span>
                  <span>{Math.round(goal.progressRate)}%</span>
                </div>
                <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                    className="h-2 bg-coral rounded-full transition-all"
                    style={{ width: `${Math.min(goal.progressRate, 100)}%` }}
                  />
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-xs text-gray-400">
                  {goal.dDay > 0
                    ? `D-${goal.dDay}`
                    : goal.dDay === 0
                      ? "D-Day"
                      : `D+${Math.abs(goal.dDay)}`}
                  {" · "}
                  {goal.endDate} 까지
                </span>
                {!goal.completed ? (
                  <div className="flex items-center gap-2">
                    <button
                      className="w-7 h-7 rounded-full border border-gray-200 text-gray-500 hover:border-coral hover:text-coral transition text-sm disabled:opacity-30"
                      onClick={() => handleProgress(goal.id, -1)}
                      disabled={goal.currentValue === 0}
                    >
                      −
                    </button>
                    <button
                      className="w-7 h-7 rounded-full bg-coral text-white hover:bg-salmon transition text-sm"
                      onClick={() => handleProgress(goal.id, 1)}
                    >
                      +
                    </button>
                  </div>
                ) : (
                  <span className="text-xs text-green-500 font-semibold">
                    ✓ 완료
                  </span>
                )}
              </div>
            </div>
          ))
        )}

        {formError && (
          <p className="text-xs text-red-400 text-center">{formError}</p>
        )}

        {/* 모달 */}
        {showModal && (
          <div className="fixed inset-0 bg-black/40 z-50 flex items-end justify-center">
            <div className="bg-white w-full max-w-xl rounded-t-2xl p-5 flex flex-col gap-4">
              <h2 className="font-semibold text-gray-800">
                {editTarget ? "목표 수정" : "목표 추가"}
              </h2>
              <input
                className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral"
                placeholder="목표 제목"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
              />
              <div className="flex gap-2">
                <input
                  className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral flex-1"
                  placeholder="목표 수치"
                  type="number"
                  min="1"
                  value={form.targetValue}
                  onChange={(e) =>
                    setForm({ ...form, targetValue: e.target.value })
                  }
                />
                <input
                  className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral w-24"
                  placeholder="단위"
                  value={form.unit}
                  disabled={!!editTarget}
                  onChange={(e) => setForm({ ...form, unit: e.target.value })}
                />
              </div>
              <select
                className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral text-gray-700"
                value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
              >
                <option value="">카테고리 선택 (선택)</option>
                {Object.keys(CATEGORY_COLORS).map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
              <div className="flex gap-2">
                <div className="flex-1">
                  <label className="text-xs text-gray-400 mb-1 block">
                    시작일
                  </label>
                  <input
                    type="date"
                    className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral w-full"
                    value={form.startDate}
                    onChange={(e) =>
                      setForm({ ...form, startDate: e.target.value })
                    }
                  />
                </div>
                <div className="flex-1">
                  <label className="text-xs text-gray-400 mb-1 block">
                    종료일
                  </label>
                  <input
                    type="date"
                    className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral w-full"
                    value={form.endDate}
                    onChange={(e) =>
                      setForm({ ...form, endDate: e.target.value })
                    }
                  />
                </div>
              </div>
              <div className="flex gap-2 pt-1">
                <button
                  className="flex-1 border border-gray-200 text-gray-500 py-2.5 rounded-full text-sm hover:bg-gray-50 transition"
                  onClick={() => setShowModal(false)}
                >
                  취소
                </button>
                <button
                  className="flex-1 bg-coral text-white py-2.5 rounded-full text-sm hover:bg-salmon transition disabled:opacity-50"
                  onClick={handleSubmit}
                  disabled={loading}
                >
                  {loading ? "저장 중..." : editTarget ? "수정" : "추가"}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}
