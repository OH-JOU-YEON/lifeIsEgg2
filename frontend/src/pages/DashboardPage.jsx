import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import api from "../api/axios";

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const res = await api.get("/api/dashboard/stats");
        setStats(res.data.data);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="text-center text-gray-400 text-sm py-20">
          불러오는 중...
        </div>
      </Layout>
    );
  }

  if (!stats) {
    return (
      <Layout>
        <div className="text-center text-gray-400 text-sm py-20">
          데이터를 불러올 수 없어요
        </div>
      </Layout>
    );
  }

  const { weeklyGoal, monthlyGoal, categoryTime, activitySummary } = stats;

  return (
    <Layout>
      <div className="flex flex-col gap-6">
        {/* 목표 달성률 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-gray-500">목표 달성률</h2>
          <div className="grid grid-cols-2 gap-3">
            <GoalStatCard label="이번 주" stats={weeklyGoal} />
            <GoalStatCard label="이번 달" stats={monthlyGoal} />
          </div>
        </section>

        {/* 카테고리별 학습 시간 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-gray-500">
            최근 7일 카테고리별 학습 시간
          </h2>
          <div className="bg-white rounded-2xl shadow-sm p-4 flex flex-col gap-3">
            {categoryTime.length === 0 ? (
              <p className="text-sm text-gray-400 text-center py-4">
                완료된 일정이 없어요
              </p>
            ) : (
              categoryTime.map((item) => (
                <CategoryBar
                  key={item.category}
                  item={item}
                  max={Math.max(...categoryTime.map((c) => c.totalHours))}
                />
              ))
            )}
          </div>
        </section>

        {/* 이번 달 활동 */}
        <section className="flex flex-col gap-3">
          <h2 className="text-sm font-semibold text-gray-500">이번 달 활동</h2>
          <div className="grid grid-cols-2 gap-3">
            <ActivityCard
              icon="📝"
              label="작성한 일기"
              count={activitySummary.diaryCount}
              unit="개"
            />
            <ActivityCard
              icon="🐣"
              label="받은 응원"
              count={activitySummary.cheerCount}
              unit="개"
            />
          </div>
        </section>
      </div>
    </Layout>
  );
}

function GoalStatCard({ label, stats }) {
  const rate = Math.round(stats.achievementRate);
  return (
    <div className="bg-white rounded-2xl shadow-sm p-4 flex flex-col gap-2">
      <span className="text-xs text-gray-400">{label}</span>
      <span className="text-2xl font-semibold text-coral">{rate}%</span>
      <div className="h-1.5 bg-gray-100 rounded-full overflow-hidden">
        <div
          className="h-1.5 bg-coral rounded-full transition-all"
          style={{ width: `${Math.min(rate, 100)}%` }}
        />
      </div>
      <span className="text-xs text-gray-400">
        {stats.completedCount} / {stats.totalCount}개 완료
      </span>
    </div>
  );
}

function CategoryBar({ item, max }) {
  const percent = max === 0 ? 0 : (item.totalHours / max) * 100;
  const hours = Math.floor(item.totalHours);
  const minutes = Math.round((item.totalHours - hours) * 60);

  return (
    <div className="flex flex-col gap-1">
      <div className="flex justify-between text-xs text-gray-600">
        <span>{item.category}</span>
        <span>
          {hours > 0 ? `${hours}시간 ` : ""}
          {minutes > 0 ? `${minutes}분` : ""}
        </span>
      </div>
      <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
        <div
          className="h-2 bg-coral rounded-full transition-all"
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
}

function ActivityCard({ icon, label, count, unit }) {
  return (
    <div className="bg-white rounded-2xl shadow-sm p-4 flex flex-col gap-1">
      <span className="text-lg">{icon}</span>
      <span className="text-xs text-gray-400">{label}</span>
      <span className="text-2xl font-semibold text-coral">
        {count}
        <span className="text-sm font-normal text-gray-400 ml-1">{unit}</span>
      </span>
    </div>
  );
}
