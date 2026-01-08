package com.example.Resume.ResumeAI.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ResumeParserService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParserService.class);

    public String extractTextFromPDF(String filePath) throws IOException {
        File file = new File(filePath);
        
        if (!file.exists()) {
            throw new IOException("PDF file not found: " + filePath);
        }
        
        logger.info("Extracting text from PDF: {}", filePath);
        
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            logger.info("Successfully extracted {} characters from PDF", text.length());
            return text;
        } catch (Exception e) {
            logger.error("Failed to extract text from PDF: {}", filePath, e);
            throw new IOException("Failed to parse PDF file: " + e.getMessage(), e);
        }
    }

    public String extractTextFromDOCX(String filePath) throws IOException {
        File file = new File(filePath);
        
        if (!file.exists()) {
            throw new IOException("DOCX file not found: " + filePath);
        }
        
        logger.info("Extracting text from DOCX: {}", filePath);
        
        StringBuilder text = new StringBuilder();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            
            logger.info("Successfully extracted {} characters from DOCX", text.length());
            return text.toString();
        } catch (Exception e) {
            logger.error("Failed to extract text from DOCX: {}", filePath, e);
            throw new IOException("Failed to parse DOCX file: " + e.getMessage(), e);
        }
    }

    public String parseResume(String filePath, String contentType) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        
        logger.info("Parsing resume - File: {}, ContentType: {}", filePath, contentType);
        
        // Normalize content type
        String normalizedType = contentType.toLowerCase().trim();
        
        // Check for PDF
        if (normalizedType.contains("pdf") || normalizedType.equals("pdf")) {
            return extractTextFromPDF(filePath);
        } 
        // Check for DOCX
        else if (normalizedType.contains("wordprocessingml") || 
                 normalizedType.contains("docx") || 
                 normalizedType.equals("docx")) {
            return extractTextFromDOCX(filePath);
        }
        // Check for older DOC format
        else if (normalizedType.contains("msword") || 
                 normalizedType.equals("doc")) {
            return extractTextFromDOCX(filePath);
        }
        else {
            throw new IllegalArgumentException("Unsupported file type: " + contentType + 
                ". Supported types: PDF, DOCX");
        }
    }
    
    /**
     * Alternative method that accepts file extension directly
     */
    public String parseResumeByExtension(String filePath, String fileExtension) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        if (fileExtension == null || fileExtension.trim().isEmpty()) {
            throw new IllegalArgumentException("File extension cannot be null or empty");
        }
        
        logger.info("Parsing resume by extension - File: {}, Extension: {}", filePath, fileExtension);
        
        String ext = fileExtension.toLowerCase().trim();
        
        if (ext.equals("pdf")) {
            return extractTextFromPDF(filePath);
        } else if (ext.equals("docx") || ext.equals("doc")) {
            return extractTextFromDOCX(filePath);
        } else {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        }
    }
}