package com.example.Resume.ResumeAI.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AIAnalysisService {
    
    public Map<String, Object> analyzeResumeForJob(String resumeText, String jobDescription, 
                                                    String jobTitle, String requirements) {
        Map<String, Object> analysis = new HashMap<>();
        
        Set<String> jobKeywords = extractKeywordsFromJob(jobDescription, requirements);
        Set<String> resumeKeywords = extractKeywordsFromResume(resumeText);
        
        Set<String> matchedKeywords = new HashSet<>(resumeKeywords);
        matchedKeywords.retainAll(jobKeywords);
        
        Set<String> missingKeywords = new HashSet<>(jobKeywords);
        missingKeywords.removeAll(resumeKeywords);
        
        analysis.put("matchedKeywords", matchedKeywords);
        analysis.put("missingKeywords", missingKeywords);
        
        int matchScore = calculateMatchScore(matchedKeywords, missingKeywords, jobKeywords.size());
        analysis.put("matchScore", matchScore);
        
        List<Map<String, String>> grammarIssues = detectGrammarIssues(resumeText);
        analysis.put("grammarIssues", grammarIssues);
        
        List<Map<String, String>> formattingIssues = detectFormattingIssues(resumeText);
        analysis.put("formattingIssues", formattingIssues);
        
        List<Map<String, String>> contentSuggestions = generateContentSuggestions(
            resumeText, jobDescription, missingKeywords, matchScore
        );
        analysis.put("contentSuggestions", contentSuggestions);
        
        Map<String, List<String>> strengthsWeaknesses = analyzeStrengthsWeaknesses(
            resumeText, matchedKeywords, missingKeywords, grammarIssues.size(), formattingIssues.size()
        );
        analysis.put("strengthsWeaknesses", strengthsWeaknesses);
        
        String overallFeedback = generateOverallFeedback(
            matchScore, matchedKeywords.size(), missingKeywords.size(), 
            grammarIssues.size(), formattingIssues.size(), jobTitle
        );
        analysis.put("overallFeedback", overallFeedback);
        
        String improvedVersion = generateImprovedVersion(
            resumeText, missingKeywords, grammarIssues, formattingIssues
        );
        analysis.put("improvedVersion", improvedVersion);
        
        return analysis;
    }
    
    private Set<String> extractKeywordsFromJob(String jobDesc, String requirements) {
        Set<String> keywords = new HashSet<>();
        String combinedText = (jobDesc + " " + requirements).toLowerCase();
        
        String[] techPatterns = {
            "java", "python", "javascript", "typescript", "react", "angular", "vue", "node",
            "spring", "springboot", "django", "flask", "express", "fastapi",
            "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "ci/cd",
            "rest", "api", "microservices", "graphql", "grpc",
            "git", "agile", "scrum", "jira", "confluence",
            "html", "css", "sass", "tailwind", "bootstrap",
            "junit", "testing", "tdd", "unit test", "integration test",
            "linux", "unix", "bash", "shell", "devops",
            "machine learning", "ai", "deep learning", "tensorflow", "pytorch",
            "data structures", "algorithms", "system design", "oop"
        };
        
        for (String tech : techPatterns) {
            if (combinedText.contains(tech)) {
                keywords.add(tech);
            }
        }
        
        Pattern expPattern = Pattern.compile("(\\d+)\\+?\\s*years?");
        Matcher matcher = expPattern.matcher(combinedText);
        if (matcher.find()) {
            keywords.add(matcher.group(0));
        }
        
        String[] degrees = {"bachelor", "master", "phd", "mba", "degree"};
        for (String degree : degrees) {
            if (combinedText.contains(degree)) {
                keywords.add(degree);
            }
        }
        
        return keywords;
    }
    
    private Set<String> extractKeywordsFromResume(String resumeText) {
        Set<String> keywords = new HashSet<>();
        String lowerText = resumeText.toLowerCase();
        
        String[] allKeywords = {
            "java", "python", "javascript", "typescript", "react", "angular", "vue", "node",
            "spring", "springboot", "django", "flask", "express", "fastapi",
            "sql", "mysql", "postgresql", "mongodb", "redis", "elasticsearch",
            "aws", "azure", "gcp", "docker", "kubernetes", "jenkins", "ci/cd",
            "rest", "api", "microservices", "graphql", "grpc",
            "git", "agile", "scrum", "jira", "confluence",
            "html", "css", "sass", "tailwind", "bootstrap",
            "junit", "testing", "tdd", "linux", "unix", "bash",
            "machine learning", "ai", "tensorflow", "pytorch", "bachelor", "master"
        };
        
        for (String keyword : allKeywords) {
            if (lowerText.contains(keyword)) {
                keywords.add(keyword);
            }
        }
        
        return keywords;
    }
    
    private int calculateMatchScore(Set<String> matched, Set<String> missing, int totalRequired) {
        if (totalRequired == 0) return 75;
        double matchRatio = (double) matched.size() / totalRequired;
        int baseScore = (int) (matchRatio * 100);
        int bonusPoints = Math.min(10, matched.size() - totalRequired);
        return Math.min(100, baseScore + bonusPoints);
    }
    
    private List<Map<String, String>> detectGrammarIssues(String text) {
        List<Map<String, String>> issues = new ArrayList<>();
        
        String[][] commonMistakes = {
            {"recieve", "receive"}, {"occured", "occurred"}, {"seperate", "separate"},
            {"definately", "definitely"}, {"experiance", "experience"}, {"managment", "management"},
            {"responsability", "responsibility"}, {"acheivement", "achievement"}
        };
        
        for (String[] mistake : commonMistakes) {
            if (text.toLowerCase().contains(mistake[0])) {
                Map<String, String> issue = new HashMap<>();
                issue.put("type", "spelling");
                issue.put("error", mistake[0]);
                issue.put("suggestion", mistake[1]);
                issue.put("severity", "high");
                issues.add(issue);
            }
        }
        
        Pattern repeatedWords = Pattern.compile("\\b(\\w+)\\s+\\1\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = repeatedWords.matcher(text);
        while (matcher.find()) {
            Map<String, String> issue = new HashMap<>();
            issue.put("type", "grammar");
            issue.put("error", "Repeated word: " + matcher.group(1));
            issue.put("suggestion", "Remove duplicate");
            issue.put("severity", "medium");
            issues.add(issue);
        }
        
        String[] passiveIndicators = {"was developed", "were created", "was managed", "were implemented"};
        for (String passive : passiveIndicators) {
            if (text.toLowerCase().contains(passive)) {
                Map<String, String> issue = new HashMap<>();
                issue.put("type", "style");
                issue.put("error", "Passive voice detected");
                issue.put("suggestion", "Use active voice for stronger impact");
                issue.put("severity", "low");
                issues.add(issue);
                break;
            }
        }
        
        return issues;
    }
    
    private List<Map<String, String>> detectFormattingIssues(String text) {
        List<Map<String, String>> issues = new ArrayList<>();
        
        boolean hasSlashDates = text.contains("/20");
        boolean hasDashDates = text.contains("-20");
        if (hasSlashDates && hasDashDates) {
            Map<String, String> issue = new HashMap<>();
            issue.put("type", "formatting");
            issue.put("error", "Inconsistent date formatting");
            issue.put("suggestion", "Use consistent date format throughout (MM/YYYY or MM-YYYY)");
            issue.put("severity", "medium");
            issues.add(issue);
        }
        
        boolean hasDashes = text.contains("- ");
        boolean hasBullets = text.contains("• ");
        boolean hasAsterisks = text.contains("* ");
        int bulletTypes = (hasDashes ? 1 : 0) + (hasBullets ? 1 : 0) + (hasAsterisks ? 1 : 0);
        
        if (bulletTypes > 1) {
            Map<String, String> issue = new HashMap<>();
            issue.put("type", "formatting");
            issue.put("error", "Inconsistent bullet point styles");
            issue.put("suggestion", "Use the same bullet style throughout the document");
            issue.put("severity", "low");
            issues.add(issue);
        }
        
        if (text.contains("\n\n\n")) {
            Map<String, String> issue = new HashMap<>();
            issue.put("type", "formatting");
            issue.put("error", "Excessive white space");
            issue.put("suggestion", "Remove extra line breaks for cleaner formatting");
            issue.put("severity", "low");
            issues.add(issue);
        }
        
        return issues;
    }
    
    private List<Map<String, String>> generateContentSuggestions(String resumeText, String jobDesc, 
                                                                  Set<String> missingKeywords, int matchScore) {
        List<Map<String, String>> suggestions = new ArrayList<>();
        
        if (!missingKeywords.isEmpty() && missingKeywords.size() <= 10) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("category", "Keywords");
            suggestion.put("priority", "high");
            suggestion.put("suggestion", "Add these relevant skills to match the job: " + String.join(", ", missingKeywords));
            suggestion.put("impact", "Could increase match score by " + (missingKeywords.size() * 5) + "%");
            suggestions.add(suggestion);
        }
        
        if (!resumeText.contains("%") && !resumeText.contains("$")) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("category", "Impact");
            suggestion.put("priority", "high");
            suggestion.put("suggestion", "Quantify your achievements with numbers, percentages, or dollar amounts");
            suggestion.put("impact", "Makes accomplishments more concrete and impressive");
            suggestions.add(suggestion);
        }
        
        String[] weakVerbs = {"responsible for", "worked on", "helped with", "assisted"};
        boolean hasWeakVerbs = false;
        for (String verb : weakVerbs) {
            if (resumeText.toLowerCase().contains(verb)) {
                hasWeakVerbs = true;
                break;
            }
        }
        
        if (hasWeakVerbs) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("category", "Language");
            suggestion.put("priority", "medium");
            suggestion.put("suggestion", "Replace weak phrases with strong action verbs: Led, Developed, Implemented, Achieved, Optimized");
            suggestion.put("impact", "Creates stronger impression of leadership and initiative");
            suggestions.add(suggestion);
        }
        
        if (matchScore < 70) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("category", "Relevance");
            suggestion.put("priority", "high");
            suggestion.put("suggestion", "Tailor your experience descriptions to emphasize skills mentioned in the job posting");
            suggestion.put("impact", "Dramatically improves ATS ranking and recruiter interest");
            suggestions.add(suggestion);
        }
        
        if (!resumeText.toLowerCase().contains("project")) {
            Map<String, String> suggestion = new HashMap<>();
            suggestion.put("category", "Content");
            suggestion.put("priority", "medium");
            suggestion.put("suggestion", "Add a Projects section to showcase hands-on experience");
            suggestion.put("impact", "Demonstrates practical application of skills");
            suggestions.add(suggestion);
        }
        
        return suggestions;
    }
    
    private Map<String, List<String>> analyzeStrengthsWeaknesses(String resumeText, Set<String> matchedKeywords,
                                                                  Set<String> missingKeywords, int grammarIssueCount,
                                                                  int formattingIssueCount) {
        Map<String, List<String>> analysis = new HashMap<>();
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        
        if (matchedKeywords.size() > 5) {
            strengths.add("Strong technical skill set with " + matchedKeywords.size() + " relevant keywords");
        }
        
        if (resumeText.contains("@") && resumeText.matches(".*\\d{3}[-.\\s]?\\d{3}[-.\\s]?\\d{4}.*")) {
            strengths.add("Complete contact information included");
        }
        
        if (resumeText.toLowerCase().contains("linkedin") || resumeText.toLowerCase().contains("github")) {
            strengths.add("Professional online presence demonstrated");
        }
        
        Pattern numberPattern = Pattern.compile("\\d+%|\\$[\\d,]+|\\d+\\+");
        Matcher matcher = numberPattern.matcher(resumeText);
        int quantifiedAchievements = 0;
        while (matcher.find()) quantifiedAchievements++;
        
        if (quantifiedAchievements > 3) {
            strengths.add("Good use of metrics and quantified achievements");
        }
        
        if (missingKeywords.size() > 5) {
            weaknesses.add("Missing " + missingKeywords.size() + " important keywords from job description");
        }
        
        if (grammarIssueCount > 0) {
            weaknesses.add(grammarIssueCount + " grammar or spelling issues detected");
        }
        
        if (formattingIssueCount > 0) {
            weaknesses.add("Formatting inconsistencies found");
        }
        
        if (resumeText.length() < 500) {
            weaknesses.add("Resume may be too brief - consider adding more detail");
        }
        
        if (!resumeText.toLowerCase().contains("achieved") && !resumeText.toLowerCase().contains("accomplished")) {
            weaknesses.add("Limited focus on achievements and results");
        }
        
        analysis.put("strengths", strengths);
        analysis.put("weaknesses", weaknesses);
        
        return analysis;
    }
    
    private String generateOverallFeedback(int matchScore, int matchedCount, int missingCount,
                                          int grammarIssues, int formattingIssues, String jobTitle) {
        StringBuilder feedback = new StringBuilder();
        
        if (matchScore >= 80) {
            feedback.append("Excellent! Your resume is highly aligned with the ").append(jobTitle).append(" position. ");
        } else if (matchScore >= 60) {
            feedback.append("Good foundation, but your resume needs optimization for the ").append(jobTitle).append(" role. ");
        } else {
            feedback.append("Significant improvements needed to match the ").append(jobTitle).append(" requirements. ");
        }
        
        feedback.append("Match Score: ").append(matchScore).append("/100\n\n");
        feedback.append("Keyword Analysis:\n");
        feedback.append("✓ ").append(matchedCount).append(" relevant keywords found\n");
        if (missingCount > 0) {
            feedback.append("✗ ").append(missingCount).append(" important keywords missing\n");
        }
        feedback.append("\n");
        
        if (grammarIssues > 0 || formattingIssues > 0) {
            feedback.append("Quality Issues:\n");
            if (grammarIssues > 0) {
                feedback.append("- ").append(grammarIssues).append(" grammar/spelling issues to fix\n");
            }
            if (formattingIssues > 0) {
                feedback.append("- ").append(formattingIssues).append(" formatting inconsistencies\n");
            }
            feedback.append("\n");
        }
        
        feedback.append("Recommended Actions:\n");
        feedback.append("1. Review and incorporate missing keywords naturally\n");
        feedback.append("2. Fix all grammar and spelling errors\n");
        feedback.append("3. Ensure consistent formatting throughout\n");
        feedback.append("4. Quantify achievements with specific metrics\n");
        feedback.append("5. Tailor experience descriptions to job requirements\n");
        
        return feedback.toString();
    }
    
    private String generateImprovedVersion(String originalText, Set<String> missingKeywords,
                                          List<Map<String, String>> grammarIssues,
                                          List<Map<String, String>> formattingIssues) {
        StringBuilder improved = new StringBuilder();
        
        improved.append("=== AI-GENERATED IMPROVEMENT SUGGESTIONS ===\n\n");
        improved.append("1. ADD MISSING KEYWORDS:\n");
        improved.append("Consider naturally incorporating these skills in your experience or skills section:\n");
        improved.append("- ").append(String.join("\n- ", missingKeywords)).append("\n\n");
        
        improved.append("2. FIX IDENTIFIED ISSUES:\n");
        for (Map<String, String> issue : grammarIssues) {
            improved.append("- ").append(issue.get("error")).append(" → ").append(issue.get("suggestion")).append("\n");
        }
        for (Map<String, String> issue : formattingIssues) {
            improved.append("- ").append(issue.get("error")).append(" → ").append(issue.get("suggestion")).append("\n");
        }
        improved.append("\n");
        
        improved.append("3. EXAMPLE IMPROVED BULLET POINT:\n");
        improved.append("❌ Worked on developing web applications\n");
        improved.append("✓ Developed and deployed 5+ responsive web applications using React and Node.js, improving user engagement by 40%\n\n");
        
        improved.append("4. QUANTIFY YOUR IMPACT:\n");
        improved.append("Add specific numbers, percentages, or metrics to your achievements.\n");
        improved.append("Examples: 'Increased efficiency by 30%', 'Managed team of 5 developers', 'Reduced costs by $50K'\n");
        
        return improved.toString();
    }
}