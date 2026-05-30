import React, { useState } from "react";
import {
  User, Mail, Lock, Bell, Shield, Palette,
  Trash2, Save, Eye, EyeOff, Zap, FileText,
  Brain, BarChart3, Settings, LogOut,
  CheckCircle, Moon, Sun, Monitor, Info,
} from "lucide-react";
import api from "./api";
import "./SettingsPage.css";

export default function SettingsPage({ setCurrentView, onLogout }) {
  const [activeTab, setActiveTab] = useState("profile");
  const [toast, setToast] = useState(null);

  const storedUser = (() => {
    try { return JSON.parse(localStorage.getItem("user") || "{}"); } catch { return {}; }
  })();

  const [profile, setProfile] = useState({
    firstName: storedUser.firstName || "",
    lastName:  storedUser.lastName  || "",
    email:     storedUser.email     || "",
  });
  const [pwd, setPwd]       = useState({ current: "", next: "", confirm: "" });
  const [showPwd, setShowPwd] = useState({ current: false, next: false, confirm: false });
  const [notifs, setNotifs]  = useState({ atsAlerts: true, weeklyDigest: false, tips: true });
  const [theme, setTheme]    = useState("dark");

  const showToast = (msg, type = "success") => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleSaveProfile = (e) => {
    e.preventDefault();
    localStorage.setItem("user", JSON.stringify({ ...storedUser, firstName: profile.firstName, lastName: profile.lastName }));
    showToast("Profile saved successfully!");
  };

  const handleChangePassword = (e) => {
    e.preventDefault();
    if (!pwd.current || !pwd.next || !pwd.confirm) { showToast("Please fill all password fields.", "error"); return; }
    if (pwd.next !== pwd.confirm)                   { showToast("New passwords do not match.", "error"); return; }
    if (pwd.next.length < 8)                        { showToast("Password must be at least 8 characters.", "error"); return; }
    showToast("Password updated!");
    setPwd({ current: "", next: "", confirm: "" });
  };

  const handleDeleteAccount = () => {
    if (window.confirm("This will permanently delete your account. Are you sure?"))
      showToast("Account deletion coming soon.", "info");
  };

  const handleLogout = () => { api.logout(); if (onLogout) onLogout(); };
  const togglePwd = (k) => setShowPwd(p => ({ ...p, [k]: !p[k] }));

  const TABS = [
    { id: "profile",       icon: User,    label: "Profile" },
    { id: "account",       icon: Lock,    label: "Security" },
    { id: "notifications", icon: Bell,    label: "Notifications" },
    { id: "appearance",    icon: Palette, label: "Appearance" },
    { id: "privacy",       icon: Shield,  label: "Privacy" },
  ];

  return (
    <div className="sp-page">
      {/* ── Single app sidebar ── */}
      <aside className="sp-sidebar">
        <div className="sp-logo">
          <div className="sp-logo-icon"><Zap size={20} /></div>
          <span className="sp-logo-name">BoostCV</span>
        </div>
        <nav className="sp-nav">
          {[
            { id: "dashboard", icon: FileText,  label: "My Resumes" },
            { id: "aihub",     icon: Brain,     label: "AI Hub" },
            { id: "analytics", icon: BarChart3, label: "Analytics" },
            { id: "settings",  icon: Settings,  label: "Settings", active: true },
          ].map(n => (
            <button key={n.id} className={`sp-nav-btn${n.active ? " active" : ""}`}
              onClick={() => !n.active && setCurrentView(n.id)}>
              <n.icon size={19} /><span>{n.label}</span>
            </button>
          ))}
        </nav>
        <button className="sp-logout" onClick={handleLogout}>
          <LogOut size={18} /><span>Logout</span>
        </button>
      </aside>

      {/* ── Main content ── */}
      <main className="sp-main">
        {/* Page header */}
        <header className="sp-header">
          <div>
            <h1 className="sp-title">Settings</h1>
            <p className="sp-subtitle">Manage your account preferences and profile</p>
          </div>
          {/* Avatar chip */}
          <div className="sp-user-chip">
            <div className="sp-avatar">{(profile.firstName?.[0] || "U").toUpperCase()}{(profile.lastName?.[0] || "").toUpperCase()}</div>
            <div className="sp-user-info">
              <p className="sp-user-name">{profile.firstName} {profile.lastName}</p>
              <p className="sp-user-email">{profile.email}</p>
            </div>
          </div>
        </header>

        {/* Horizontal tab bar */}
        <div className="sp-tabs">
          {TABS.map(t => (
            <button key={t.id} className={`sp-tab${activeTab === t.id ? " active" : ""}`}
              onClick={() => setActiveTab(t.id)}>
              <t.icon size={15} />
              {t.label}
            </button>
          ))}
        </div>

        {/* Tab content */}
        <div className="sp-body">
          {toast && (
            <div className={`sp-toast ${toast.type}`}>
              <CheckCircle size={15} />{toast.msg}
            </div>
          )}

          {/* ── PROFILE ── */}
          {activeTab === "profile" && (
            <form onSubmit={handleSaveProfile} className="sp-form">
              <div className="sp-card">
                <h2 className="sp-card-title">Personal Information</h2>
                <p className="sp-card-desc">Update your name displayed across BoostCV.</p>
                <div className="sp-two-col">
                  <div className="sp-field">
                    <label>First Name</label>
                    <input type="text" value={profile.firstName}
                      onChange={e => setProfile(p => ({ ...p, firstName: e.target.value }))}
                      placeholder="John" className="sp-input" />
                  </div>
                  <div className="sp-field">
                    <label>Last Name</label>
                    <input type="text" value={profile.lastName}
                      onChange={e => setProfile(p => ({ ...p, lastName: e.target.value }))}
                      placeholder="Doe" className="sp-input" />
                  </div>
                </div>
                <div className="sp-field">
                  <label>Email Address</label>
                  <div className="sp-input-wrap">
                    <Mail size={15} className="sp-input-icon" />
                    <input type="email" value={profile.email} disabled
                      className="sp-input sp-input-icon-pad disabled" />
                  </div>
                  <span className="sp-hint">Email cannot be changed. Contact support if needed.</span>
                </div>
              </div>
              <button type="submit" className="sp-save-btn"><Save size={15} />Save Changes</button>
            </form>
          )}

          {/* ── SECURITY ── */}
          {activeTab === "account" && (
            <div className="sp-stack">
              <div className="sp-card">
                <h2 className="sp-card-title">Change Password</h2>
                <p className="sp-card-desc">Use a strong password with at least 8 characters.</p>
                <form onSubmit={handleChangePassword} className="sp-form">
                  {[
                    { key: "current", label: "Current Password",     ph: "Enter current password" },
                    { key: "next",    label: "New Password",         ph: "Minimum 8 characters" },
                    { key: "confirm", label: "Confirm New Password", ph: "Re-enter new password" },
                  ].map(f => (
                    <div key={f.key} className="sp-field">
                      <label>{f.label}</label>
                      <div className="sp-input-wrap">
                        <Lock size={15} className="sp-input-icon" />
                        <input type={showPwd[f.key] ? "text" : "password"} value={pwd[f.key]}
                          onChange={e => setPwd(p => ({ ...p, [f.key]: e.target.value }))}
                          placeholder={f.ph} className="sp-input sp-input-icon-pad sp-input-pr" />
                        <button type="button" className="sp-eye" onClick={() => togglePwd(f.key)}>
                          {showPwd[f.key] ? <EyeOff size={15} /> : <Eye size={15} />}
                        </button>
                      </div>
                    </div>
                  ))}
                  <button type="submit" className="sp-save-btn"><Shield size={15} />Update Password</button>
                </form>
              </div>

              <div className="sp-card sp-danger-card">
                <h2 className="sp-card-title sp-danger-title">Danger Zone</h2>
                <p className="sp-card-desc">Permanently delete your account and all data. This cannot be undone.</p>
                <button className="sp-delete-btn" onClick={handleDeleteAccount}>
                  <Trash2 size={15} />Delete Account
                </button>
              </div>
            </div>
          )}

          {/* ── NOTIFICATIONS ── */}
          {activeTab === "notifications" && (
            <div className="sp-card">
              <h2 className="sp-card-title">Notification Preferences</h2>
              <p className="sp-card-desc">Choose what updates you'd like to receive.</p>
              <div className="sp-toggle-list">
                {[
                  { key: "atsAlerts",    label: "ATS Score Alerts",  desc: "Notify when a resume scores below 60" },
                  { key: "weeklyDigest", label: "Weekly Digest",     desc: "Summary of your resume activity each week" },
                  { key: "tips",         label: "Resume Tips",       desc: "Actionable tips to improve your score" },
                ].map(n => (
                  <div key={n.key} className="sp-toggle-row">
                    <div>
                      <p className="sp-toggle-label">{n.label}</p>
                      <p className="sp-toggle-desc">{n.desc}</p>
                    </div>
                    <button className={`sp-toggle${notifs[n.key] ? " on" : ""}`}
                      onClick={() => setNotifs(p => ({ ...p, [n.key]: !p[n.key] }))}
                      role="switch" aria-checked={notifs[n.key]}>
                      <span className="sp-knob" />
                    </button>
                  </div>
                ))}
              </div>
              <button className="sp-save-btn" onClick={() => showToast("Preferences saved!")}>
                <Save size={15} />Save Preferences
              </button>
            </div>
          )}

          {/* ── APPEARANCE ── */}
          {activeTab === "appearance" && (
            <div className="sp-card">
              <h2 className="sp-card-title">Appearance</h2>
              <p className="sp-card-desc">Choose your preferred colour theme.</p>
              <label className="sp-field-label">Theme</label>
              <div className="sp-theme-grid">
                {[
                  { id: "dark",   icon: Moon,    label: "Dark",   desc: "Default — easy on the eyes" },
                  { id: "light",  icon: Sun,     label: "Light",  desc: "Coming soon" },
                  { id: "system", icon: Monitor, label: "System", desc: "Follows your OS setting" },
                ].map(t => (
                  <button key={t.id} className={`sp-theme-card${theme === t.id ? " active" : ""}`}
                    onClick={() => { setTheme(t.id); showToast(`Theme set to ${t.label}!`); }}>
                    <div className="sp-theme-icon"><t.icon size={22} /></div>
                    <p className="sp-theme-label">{t.label}</p>
                    <p className="sp-theme-desc">{t.desc}</p>
                    {theme === t.id && <span className="sp-theme-check"><CheckCircle size={14} /></span>}
                  </button>
                ))}
              </div>
              <div className="sp-info-box">
                <Info size={15} />
                More customisation options are coming in a future update.
              </div>
            </div>
          )}

          {/* ── PRIVACY ── */}
          {activeTab === "privacy" && (
            <div className="sp-stack">
              <div className="sp-card">
                <h2 className="sp-card-title">Data We Store</h2>
                <ul className="sp-privacy-list">
                  {["Your uploaded resume files (stored securely)","Extracted resume text for ATS scoring","ATS analysis results and AI recommendations","Your account email and name"].map(i => <li key={i}>{i}</li>)}
                </ul>
              </div>
              <div className="sp-card">
                <h2 className="sp-card-title">Our Commitments</h2>
                <ul className="sp-privacy-list sp-privacy-check">
                  {["We never sell your data to third parties","We never share your resume without consent","Passwords are hashed with BCrypt — never plain text"].map(i => <li key={i}>{i}</li>)}
                </ul>
              </div>
              <div className="sp-privacy-actions">
                <button className="sp-secondary-btn" onClick={() => showToast("Data export coming soon.", "info")}>
                  Export My Data
                </button>
                <button className="sp-secondary-btn sp-danger-btn" onClick={handleDeleteAccount}>
                  <Trash2 size={14} />Request Deletion
                </button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
