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

createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/feed" element={<FeedPage />} />
      <Route path="/posts/new" element={<PostNewPage />} />
      <Route path="/posts/:uuid" element={<PostDetailPage />} />
      <Route path="/alarms" element={<AlarmPage />} />
    </Routes>
  </BrowserRouter>,
);
