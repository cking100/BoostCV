import React, { useState, useRef, useEffect } from "react";
import {
  ArrowLeft, Sparkles, Bot, FileText, Send, Loader2,
  Copy, Check, ChevronDown, MessageSquare,
  Briefcase, Map, Mic, Target, RefreshCw
} from "lucide-react";
import api from "./api";
import "./AIHub.css";

const TABS = [
  { id: "job-match",    icon: Target,       label: "Job Match"         },
  { id: "coach",        icon: Bot,          label: "Career Coach"      },
  { id: "cover-letter", icon: FileText,     label: "Cover Letter"      },
  { id: "interview",    icon: Mic,          label: "Interview Coach"   },
  { id: "career-paths", icon: Map,          label: "Career Paths"      },
];

export default function AIHub({ setCurrentView, resumes = [] }) {
  const [activeTab, setActiveTab]       = useState("job-match");
  const [selectedResumeId, setSelectedResumeId] = useState(resumes[0]?.id || "");

  const selectedResume = resumes.find(r => String(r.id) === String(selectedResumeId));

  return (
    <div className="aihub-container">
      {/* Sidebar */}
      <aside className="aihub-sidebar">
        <div className="aihub-sidebar-top">
          <button className="aihub-back-btn" onClick={() => setCurrentView("dashboard")}>
            <ArrowLeft size={18} />
            <span>Dashboard</span>
          </button>
          <div className="aihub-logo">
            <Sparkles size={20} className="aihub-logo-icon" />
            <span>AI Hub</span>
          </div>
        </div>

        {/* Resume selector */}
        <div className="aihub-resume-picker">
          <label className="picker-label">
            <FileText size={14} />
            Active Resume
          </label>
          {resumes.length === 0 ? (
            <p className="picker-empty">Upload a resume first</p>
          ) : (
            <div className="picker-select-wrap">
              <select
                className="picker-select"
                value={selectedResumeId}
                onChange={e => setSelectedResumeId(e.target.value)}
              >
                {resumes.map(r => (
                  <option key={r.id} value={r.id}>
                    {r.fileName} ({r.atsScore}/100)
                  </option>
                ))}
              </select>
              <ChevronDown size={16} className="picker-chevron" />
            </div>
          )}
        </div>

        {/* Tab nav */}
        <nav className="aihub-nav">
          {TABS.map(tab => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                className={`aihub-nav-btn ${activeTab === tab.id ? "active" : ""}`}
                onClick={() => setActiveTab(tab.id)}
              >
                <Icon size={18} />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>

        <div className="aihub-sidebar-footer">
          <p className="sidebar-footer-note">
            <Sparkles size={12} />
            Powered by Gemini AI
          </p>
        </div>
      </aside>

      {/* Main panel */}
      <main className="aihub-main">
        {!selectedResumeId && (
          <div className="aihub-no-resume">
            <Sparkles size={64} className="no-resume-icon" />
            <h2>No resume selected</h2>
            <p>Upload a resume from the Dashboard to get started.</p>
            <button className="hub-action-btn primary" onClick={() => setCurrentView("dashboard")}>
              Go to Dashboard
            </button>
          </div>
        )}

        {selectedResumeId && activeTab === "job-match" && (
          <JobMatchPanel resumeId={selectedResumeId} resumeName={selectedResume?.fileName} />
        )}
        {selectedResumeId && activeTab === "coach" && (
          <CoachPanel resumeId={selectedResumeId} resumeName={selectedResume?.fileName} />
        )}
        {selectedResumeId && activeTab === "cover-letter" && (
          <CoverLetterPanel resumeId={selectedResumeId} resumeName={selectedResume?.fileName} />
        )}
        {selectedResumeId && activeTab === "interview" && (
          <InterviewPanel resumeId={selectedResumeId} resumeName={selectedResume?.fileName} />
        )}
        {selectedResumeId && activeTab === "career-paths" && (
          <CareerPathPanel resumeId={selectedResumeId} resumeName={selectedResume?.fileName} />
        )}
      </main>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────
//  PANEL 1 — JOB MATCH ANALYZER
// ─────────────────────────────────────────────────────────────────
function JobMatchPanel({ resumeId, resumeName }) {
  const [jobTitle, setJobTitle]         = useState("");
  const [company, setCompany]           = useState("");
  const [jobDesc, setJobDesc]           = useState("");
  const [result, setResult]             = useState(null);
  const [loading, setLoading]           = useState(false);
  const [error, setError]               = useState("");

  const handleAnalyze = async () => {
    if (!jobDesc.trim()) { setError("Please paste a job description."); return; }
    setLoading(true); setError(""); setResult(null);
    try {
      const data = await api.analyzeForJob(resumeId, jobDesc, jobTitle, company);
      setResult(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const scoreColor = (s) => s >= 80 ? "high" : s >= 60 ? "medium" : "low";

  return (
    <div className="hub-panel">
      <div className="hub-panel-header">
        <div className="hub-panel-title">
          <Target size={24} className="panel-title-icon target" />
          <div>
            <h1>Job Match Analyzer</h1>
            <p>Paste a job description to get your match score, keyword gaps, and AI feedback</p>
          </div>
        </div>
        <div className="active-resume-badge">
          <FileText size={14} />
          {resumeName}
        </div>
      </div>

      {!result ? (
        <div className="hub-form">
          <div className="hub-form-row">
            <div className="hub-field">
              <label>Job Title</label>
              <input
                className="hub-input"
                placeholder="e.g. Senior Software Engineer"
                value={jobTitle}
                onChange={e => setJobTitle(e.target.value)}
              />
            </div>
            <div className="hub-field">
              <label>Company (optional)</label>
              <input
                className="hub-input"
                placeholder="e.g. Google"
                value={company}
                onChange={e => setCompany(e.target.value)}
              />
            </div>
          </div>
          <div className="hub-field">
            <label>Job Description *</label>
            <textarea
              className="hub-textarea tall"
              placeholder="Paste the full job description here..."
              value={jobDesc}
              onChange={e => setJobDesc(e.target.value)}
            />
          </div>
          {error && <p className="hub-error">{error}</p>}
          <button className="hub-action-btn primary" onClick={handleAnalyze} disabled={loading}>
            {loading ? <><Loader2 size={18} className="spin" /> Analyzing...</> : <><Target size={18} /> Analyze Match</>}
          </button>
        </div>
      ) : (
        <div className="hub-result">
          {/* Score */}
          <div className="match-score-card">
            <div className={`match-score-circle ${scoreColor(result.matchScore)}`}>
              <span className="match-score-number">{result.matchScore}</span>
              <span className="match-score-denom">/100</span>
            </div>
            <div className="match-score-info">
              <h2>Match Score</h2>
              <p className={`match-label ${scoreColor(result.matchScore)}`}>
                {result.matchScore >= 80 ? "Excellent Match 🎉" : result.matchScore >= 60 ? "Good Match 👍" : "Needs Improvement ⚡"}
              </p>
            </div>
          </div>

          {/* Keywords */}
          <div className="keyword-grid">
            <div className="keyword-section matched">
              <h3>✅ Matched Keywords</h3>
              <div className="kw-tags">
                {(result.matchedKeywords || []).map((k, i) => (
                  <span key={i} className="kw-tag matched">{k}</span>
                ))}
              </div>
            </div>
            <div className="keyword-section missing">
              <h3>❌ Missing Keywords</h3>
              <div className="kw-tags">
                {(result.missingKeywords || []).map((k, i) => (
                  <span key={i} className="kw-tag missing">{k}</span>
                ))}
              </div>
            </div>
          </div>

          {/* Feedback */}
          {result.overallFeedback && (
            <div className="hub-output-card">
              <h3>📋 Overall Feedback</h3>
              <p className="hub-output-text">{result.overallFeedback}</p>
            </div>
          )}

          {/* Suggestions */}
          {result.contentSuggestions?.length > 0 && (
            <div className="hub-output-card">
              <h3>💡 Improvement Suggestions</h3>
              <ul className="hub-list">
                {result.contentSuggestions.map((s, i) => <li key={i}>{s}</li>)}
              </ul>
            </div>
          )}

          <button className="hub-action-btn secondary" onClick={() => setResult(null)}>
            <RefreshCw size={16} /> Try Another Job
          </button>
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────
//  PANEL 2 — RAG CAREER COACH (CHAT)
// ─────────────────────────────────────────────────────────────────
function CoachPanel({ resumeId, resumeName }) {
  const [messages, setMessages] = useState([
    { role: "assistant", text: "Hi! I'm your AI career coach. I've read your resume and I'm ready to help. Ask me anything — salary negotiation, interview prep, how to position your experience, what roles to target..." }
  ]);
  const [input, setInput]       = useState("");
  const [loading, setLoading]   = useState(false);
  const bottomRef               = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const send = async () => {
    if (!input.trim() || loading) return;
    const userMsg = { role: "user", text: input };
    setMessages(prev => [...prev, userMsg]);
    setInput("");
    setLoading(true);
    try {
      const data = await api.ragCoach(resumeId, input);
      setMessages(prev => [...prev, { role: "assistant", text: data.advice }]);
    } catch (e) {
      setMessages(prev => [...prev, { role: "assistant", text: `Error: ${e.message}` }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKey = (e) => {
    if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); send(); }
  };

  return (
    <div className="hub-panel coach-panel">
      <div className="hub-panel-header">
        <div className="hub-panel-title">
          <Bot size={24} className="panel-title-icon coach" />
          <div>
            <h1>RAG Career Coach</h1>
            <p>AI advice grounded in your actual resume — not generic tips</p>
          </div>
        </div>
        <div className="active-resume-badge">
          <FileText size={14} />
          {resumeName}
        </div>
      </div>

      <div className="chat-window">
        {messages.map((msg, i) => (
          <div key={i} className={`chat-bubble ${msg.role}`}>
            {msg.role === "assistant" && (
              <div className="chat-avatar">
                <Bot size={16} />
              </div>
            )}
            <div className="chat-text">{msg.text}</div>
          </div>
        ))}
        {loading && (
          <div className="chat-bubble assistant">
            <div className="chat-avatar"><Bot size={16} /></div>
            <div className="chat-text typing">
              <span /><span /><span />
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="chat-input-area">
        <textarea
          className="chat-input"
          placeholder="Ask your coach anything... (Enter to send)"
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKey}
          rows={2}
        />
        <button className="chat-send-btn" onClick={send} disabled={loading || !input.trim()}>
          {loading ? <Loader2 size={20} className="spin" /> : <Send size={20} />}
        </button>
      </div>

      <div className="chat-starters">
        {["How can I improve my resume?", "What roles am I best suited for?", "Help me prepare for interviews", "How do I negotiate salary?"].map(s => (
          <button key={s} className="starter-chip" onClick={() => { setInput(s); }}>
            {s}
          </button>
        ))}
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────
//  PANEL 3 — COVER LETTER GENERATOR
// ─────────────────────────────────────────────────────────────────
function CoverLetterPanel({ resumeId, resumeName }) {
  const [jobTitle, setJobTitle]   = useState("");
  const [company, setCompany]     = useState("");
  const [jobDesc, setJobDesc]     = useState("");
  const [result, setResult]       = useState("");
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState("");
  const [copied, setCopied]       = useState(false);

  const handleGenerate = async () => {
    if (!jobTitle.trim()) { setError("Please enter a job title."); return; }
    setLoading(true); setError(""); setResult("");
    try {
      const data = await api.generateCoverLetter(resumeId, jobTitle, company, jobDesc);
      setResult(data.coverLetter);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(result);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="hub-panel">
      <div className="hub-panel-header">
        <div className="hub-panel-title">
          <FileText size={24} className="panel-title-icon cover" />
          <div>
            <h1>Cover Letter Generator</h1>
            <p>AI-written cover letter tailored to your resume and the specific role</p>
          </div>
        </div>
        <div className="active-resume-badge">
          <FileText size={14} />
          {resumeName}
        </div>
      </div>

      <div className="hub-two-col">
        <div className="hub-form compact">
          <div className="hub-field">
            <label>Job Title *</label>
            <input className="hub-input" placeholder="e.g. Frontend Engineer" value={jobTitle} onChange={e => setJobTitle(e.target.value)} />
          </div>
          <div className="hub-field">
            <label>Company</label>
            <input className="hub-input" placeholder="e.g. Stripe" value={company} onChange={e => setCompany(e.target.value)} />
          </div>
          <div className="hub-field">
            <label>Job Description</label>
            <textarea className="hub-textarea" placeholder="Paste the job description for a more tailored letter..." value={jobDesc} onChange={e => setJobDesc(e.target.value)} />
          </div>
          {error && <p className="hub-error">{error}</p>}
          <button className="hub-action-btn primary" onClick={handleGenerate} disabled={loading}>
            {loading ? <><Loader2 size={18} className="spin" /> Generating...</> : <><Sparkles size={18} /> Generate Letter</>}
          </button>
        </div>

        <div className="cover-letter-output">
          {!result && !loading && (
            <div className="output-placeholder">
              <FileText size={48} />
              <p>Your cover letter will appear here</p>
            </div>
          )}
          {loading && (
            <div className="output-placeholder">
              <Loader2 size={48} className="spin" />
              <p>Writing your cover letter...</p>
            </div>
          )}
          {result && (
            <>
              <div className="cover-letter-actions">
                <h3>Generated Cover Letter</h3>
                <button className="copy-btn" onClick={handleCopy}>
                  {copied ? <><Check size={16} /> Copied!</> : <><Copy size={16} /> Copy</>}
                </button>
              </div>
              <div className="cover-letter-text">{result}</div>
              <button className="hub-action-btn secondary mt-sm" onClick={() => setResult("")}>
                <RefreshCw size={16} /> Regenerate
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────
//  PANEL 4 — INTERVIEW COACH
// ─────────────────────────────────────────────────────────────────
function InterviewPanel({ resumeId, resumeName }) {
  const [jobDesc, setJobDesc]   = useState("");
  const [result, setResult]     = useState("");
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState("");
  const [qaItems, setQaItems]   = useState([]);

  const parseQA = (text) => {
    const pairs = [];
    const lines = text.split("\n").filter(l => l.trim());
    let current = null;
    for (const line of lines) {
      const qMatch = line.match(/^Q(\d+):\s*(.+)/i);
      const aMatch = line.match(/^A(\d+):\s*(.+)/i);
      if (qMatch) {
        if (current) pairs.push(current);
        current = { q: qMatch[2], a: "" };
      } else if (aMatch && current) {
        current.a = aMatch[2];
      } else if (current && current.a === "" && !line.match(/^[QA]\d+:/)) {
        current.a += line + " ";
      }
    }
    if (current) pairs.push(current);
    return pairs.length > 0 ? pairs : null;
  };

  const handleGenerate = async () => {
    setLoading(true); setError(""); setResult(""); setQaItems([]);
    try {
      const data = await api.getInterviewCoach(resumeId, jobDesc);
      const raw = data.questionsAndAnswers;
      setResult(raw);
      const parsed = parseQA(raw);
      if (parsed) setQaItems(parsed);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hub-panel">
      <div className="hub-panel-header">
        <div className="hub-panel-title">
          <Mic size={24} className="panel-title-icon interview" />
          <div>
            <h1>Interview Coach</h1>
            <p>10 likely interview questions with model answers based on your profile</p>
          </div>
        </div>
        <div className="active-resume-badge"><FileText size={14} />{resumeName}</div>
      </div>

      {!result && !loading && (
        <div className="hub-form">
          <div className="hub-field">
            <label>Job Description (optional but recommended)</label>
            <textarea className="hub-textarea" placeholder="Paste the job description to get role-specific questions..." value={jobDesc} onChange={e => setJobDesc(e.target.value)} />
          </div>
          {error && <p className="hub-error">{error}</p>}
          <button className="hub-action-btn primary" onClick={handleGenerate}>
            <Mic size={18} /> Generate Questions
          </button>
        </div>
      )}

      {loading && (
        <div className="hub-loading-state">
          <Loader2 size={48} className="spin" />
          <p>Preparing your interview questions...</p>
        </div>
      )}

      {result && !loading && (
        <div className="hub-result">
          {qaItems.length > 0 ? (
            <div className="qa-list">
              {qaItems.map((item, i) => (
                <InterviewCard key={i} index={i + 1} question={item.q} answer={item.a.trim()} />
              ))}
            </div>
          ) : (
            <div className="hub-output-card">
              <pre className="hub-output-pre">{result}</pre>
            </div>
          )}
          <button className="hub-action-btn secondary mt-sm" onClick={() => { setResult(""); setQaItems([]); }}>
            <RefreshCw size={16} /> Generate New Set
          </button>
        </div>
      )}
    </div>
  );
}

function InterviewCard({ index, question, answer }) {
  const [open, setOpen] = useState(false);
  return (
    <div className="qa-card">
      <button className="qa-header" onClick={() => setOpen(!open)}>
        <span className="qa-num">Q{index}</span>
        <span className="qa-question">{question}</span>
        <ChevronDown size={18} className={`qa-chevron ${open ? "open" : ""}`} />
      </button>
      {open && (
        <div className="qa-answer">
          <span className="qa-answer-label">Model Answer</span>
          <p>{answer}</p>
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────
//  PANEL 5 — CAREER PATH ADVISOR
// ─────────────────────────────────────────────────────────────────
function CareerPathPanel({ resumeId, resumeName }) {
  const [result, setResult]   = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState("");

  const handleGenerate = async () => {
    setLoading(true); setError(""); setResult("");
    try {
      const data = await api.getCareerPaths(resumeId);
      setResult(data.careerPaths);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="hub-panel">
      <div className="hub-panel-header">
        <div className="hub-panel-title">
          <Map size={24} className="panel-title-icon paths" />
          <div>
            <h1>Career Path Advisor</h1>
            <p>3 personalised career paths with skill gaps and 90-day action plans</p>
          </div>
        </div>
        <div className="active-resume-badge"><FileText size={14} />{resumeName}</div>
      </div>

      {!result && !loading && (
        <div className="career-launch">
          <div className="career-launch-graphic">
            <Map size={80} className="career-graphic-icon" />
            <div className="career-launch-orbs">
              <div className="orb orb1" />
              <div className="orb orb2" />
              <div className="orb orb3" />
            </div>
          </div>
          <h2>Discover Your Career Paths</h2>
          <p>Our AI will analyse your entire resume and suggest 3 distinct career trajectories with concrete next steps.</p>
          {error && <p className="hub-error">{error}</p>}
          <button className="hub-action-btn primary large" onClick={handleGenerate}>
            <Sparkles size={20} /> Generate My Career Paths
          </button>
        </div>
      )}

      {loading && (
        <div className="hub-loading-state">
          <Loader2 size={48} className="spin" />
          <p>Analysing your profile and mapping career trajectories...</p>
          <p className="loading-sub">This may take a few seconds</p>
        </div>
      )}

      {result && !loading && (
        <div className="hub-result">
          <div className="hub-output-card career-paths-output">
            <pre className="hub-output-pre formatted">{result}</pre>
          </div>
          <button className="hub-action-btn secondary mt-sm" onClick={() => setResult("")}>
            <RefreshCw size={16} /> Regenerate Paths
          </button>
        </div>
      )}
    </div>
  );
}
