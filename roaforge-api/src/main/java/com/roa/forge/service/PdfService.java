package com.roa.forge.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.roa.forge.dto.ErrorCode;
import com.roa.forge.entity.Document;
import com.roa.forge.exception.AppException;
import com.roa.forge.repository.DocumentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfService {
    private final Path uploadDir = Paths.get("uploads");
    private final DocumentRepository documentRepository;

    @PostConstruct
    void init() {
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new AppException(ErrorCode.STORAGE_IO_ERROR);
            }
        }
    }
}
