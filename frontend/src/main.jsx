import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./index.css";
import LoginPage from "./pages/LoginPage.jsx";
import SignupPage from "./pages/SignupPage";
import FeedPage from "./pages/FeedPage";
import PostNewPage from "./pages/PostNewPage";
import PostDetailPage from "./pages/PostDetailPage";
import AlarmPage from "./pages/AlarmPage";
import GoalsPage from "./pages/GoalsPage";
import SchedulePage from "./pages/SchedulePage";
import DashboardPage from "./pages/DashboardPage";
import LandingPage from "./pages/LandingPage";
import PrivateRoute from "./components/PrivateRoute";

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route
        path="/feed"
        element={
          <PrivateRoute>
            <FeedPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/posts/new"
        element={
          <PrivateRoute>
            <PostNewPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/posts/:uuid"
        element={
          <PrivateRoute>
            <PostDetailPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/alarms"
        element={
          <PrivateRoute>
            <AlarmPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/goals"
        element={
          <PrivateRoute>
            <GoalsPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/schedule"
        element={
          <PrivateRoute>
            <SchedulePage />
          </PrivateRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        }
      />
    </Routes>
  </BrowserRouter>,
);
