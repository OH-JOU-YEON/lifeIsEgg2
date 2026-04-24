import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import api from "../api/axios";

const CATEGORY_COLORS = {
  코딩: "bg-blue-100 text-blue-700",
  자격증: "bg-amber-100 text-amber-700",
  취준: "bg-coral/10 text-coral",
  독서: "bg-green-100 text-green-700",
  운동: "bg-purple-100 text-purple-700",
  기타: "bg-gray-100 text-gray-500",
};

function categoryStyle(cat) {
  return CATEGORY_COLORS[cat] ?? "bg-gray-100 text-gray-500";
}

function formatDateTime(dt) {
  if (!dt) return "";
  const d = new Date(dt);
  return d.toLocaleString("ko-KR", {
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function toInputDateTime(dt) {
  if (!dt) return "";
  const d = new Date(dt);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

const EMPTY_FORM = {
  title: "",
  description: "",
  startTime: "",
  endTime: "",
  category: "",
};

export default function SchedulePage() {
  const [schedules, setSchedules] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [loading, setLoading] = useState(false);

  const fetchSchedules = async () => {
    try {
      const res = await api.get("/api/schedules");
      setSchedules(res.data.data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchSchedules();
  }, []);

  const openCreate = () => {
    setEditTarget(null);
    setForm(EMPTY_FORM);
    setShowModal(true);
  };

  const openEdit = (schedule) => {
    setEditTarget(schedule);
    setForm({
      title: schedule.title,
      description: schedule.description ?? "",
      startTime: toInputDateTime(schedule.startTime),
      endTime: toInputDateTime(schedule.endTime),
      category: schedule.category,
    });
    setShowModal(true);
  };

  const handleSubmit = async () => {
    if (!form.title || !form.startTime || !form.endTime || !form.category)
      return;
    setLoading(true);
    try {
      const payload = {
        ...form,
        startTime: new Date(form.startTime).toISOString().slice(0, 19),
        endTime: new Date(form.endTime).toISOString().slice(0, 19),
      };
      if (editTarget) {
        await api.put(`/api/schedules/${editTarget.id}`, payload);
      } else {
        await api.post("/api/schedules", payload);
      }
      setShowModal(false);
      fetchSchedules();
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (scheduleId) => {
    try {
      await api.patch(`/api/schedules/${scheduleId}/complete`);
      setSchedules((prev) =>
        prev.map((s) =>
          s.id === scheduleId ? { ...s, completed: !s.completed } : s,
        ),
      );
    } catch (e) {
      console.error(e);
    }
  };

  const handleDelete = async (scheduleId) => {
    if (!window.confirm("일정을 삭제할까요?")) return;
    try {
      await api.delete(`/api/schedules/${scheduleId}`);
      fetchSchedules();
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Layout>
      <div className="flex flex-col gap-4">
        {/* 상단 */}
        <div className="flex justify-end">
          <button
            className="bg-coral text-white text-sm px-4 py-1.5 rounded-full hover:bg-salmon transition"
            onClick={openCreate}
          >
            + 일정 추가
          </button>
        </div>

        {/* 일정 목록 */}
        {schedules.length === 0 ? (
          <div className="text-center text-gray-400 text-sm py-16">
            일정을 추가해보세요 📅
          </div>
        ) : (
          schedules.map((schedule) => (
            <div
              key={schedule.id}
              className={`bg-white rounded-2xl shadow-sm p-4 flex flex-col gap-2 transition ${
                schedule.completed ? "opacity-60" : ""
              }`}
            >
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleToggle(schedule.id)}
                    className={`w-5 h-5 rounded-full border-2 flex items-center justify-center shrink-0 transition ${
                      schedule.completed
                        ? "bg-coral border-coral text-white"
                        : "border-gray-300"
                    }`}
                  >
                    {schedule.completed && (
                      <span className="text-xs leading-none">✓</span>
                    )}
                  </button>
                  <span
                    className={`font-semibold text-gray-800 ${schedule.completed ? "line-through text-gray-400" : ""}`}
                  >
                    {schedule.title}
                  </span>
                </div>
                <div className="flex gap-2 text-xs text-gray-400 shrink-0">
                  <button
                    onClick={() => openEdit(schedule)}
                    className="hover:text-coral transition"
                  >
                    수정
                  </button>
                  <button
                    onClick={() => handleDelete(schedule.id)}
                    className="hover:text-red-400 transition"
                  >
                    삭제
                  </button>
                </div>
              </div>

              <div className="flex items-center gap-2 pl-7">
                {schedule.category && (
                  <span
                    className={`text-xs px-2 py-0.5 rounded-full ${categoryStyle(schedule.category)}`}
                  >
                    {schedule.category}
                  </span>
                )}
                <span className="text-xs text-gray-400">
                  {formatDateTime(schedule.startTime)} –{" "}
                  {formatDateTime(schedule.endTime)}
                </span>
              </div>

              {schedule.description && (
                <p className="text-xs text-gray-500 pl-7 leading-relaxed">
                  {schedule.description}
                </p>
              )}
            </div>
          ))
        )}

        {/* 모달 */}
        {showModal && (
          <div className="fixed inset-0 bg-black/40 z-50 flex items-end justify-center">
            <div className="bg-white w-full max-w-xl rounded-t-2xl p-5 flex flex-col gap-4">
              <h2 className="font-semibold text-gray-800">
                {editTarget ? "일정 수정" : "일정 추가"}
              </h2>
              <input
                className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral"
                placeholder="일정 제목"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
              />
              <select
                className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral text-gray-700"
                value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
              >
                <option value="">카테고리 선택</option>
                {Object.keys(CATEGORY_COLORS).map((c) => (
                  <option key={c} value={c}>
                    {c}
                  </option>
                ))}
              </select>
              <div className="flex gap-2">
                <div className="flex-1">
                  <label className="text-xs text-gray-400 mb-1 block">
                    시작
                  </label>
                  <input
                    type="datetime-local"
                    className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral w-full"
                    value={form.startTime}
                    onChange={(e) =>
                      setForm({ ...form, startTime: e.target.value })
                    }
                  />
                </div>
                <div className="flex-1">
                  <label className="text-xs text-gray-400 mb-1 block">
                    종료
                  </label>
                  <input
                    type="datetime-local"
                    className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral w-full"
                    value={form.endTime}
                    onChange={(e) =>
                      setForm({ ...form, endTime: e.target.value })
                    }
                  />
                </div>
              </div>
              <textarea
                className="border border-gray-200 rounded-xl px-4 py-2.5 text-sm outline-none focus:border-coral resize-none"
                placeholder="메모 (선택)"
                rows={2}
                value={form.description}
                onChange={(e) =>
                  setForm({ ...form, description: e.target.value })
                }
              />
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
