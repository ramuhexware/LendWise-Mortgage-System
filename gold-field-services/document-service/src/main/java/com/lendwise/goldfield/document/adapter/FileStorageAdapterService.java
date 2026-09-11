package com.lendwise.goldfield.document.adapter;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File Storage Adapter Service
 *
 * Implements outbound FileAdapter behavior in document-service to write, archive,
 * and retrieve uploaded borrower mortgage documents (Paystubs, W-2s, Tax Returns, Bank Statements).
 */
@Service
public class FileStorageAdapterService {

    private static final String STORAGE_BASE_DIR = "./target/document-archive/";

    public FileStorageAdapterService() {
        File dir = new File(STORAGE_BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Outbound FileAdapter Write Operation: Write payload bytes to file
     */
    public String writeDocumentToFile(String documentId, String fileName, byte[] content) throws IOException {
        Path targetPath = Paths.get(STORAGE_BASE_DIR, documentId + "_" + fileName);
        try (FileOutputStream fos = new FileOutputStream(targetPath.toFile())) {
            fos.write(content);
        }
        System.out.println("[FileAdapter Outbound] Written document to file: " + targetPath.toAbsolutePath());
        return targetPath.toAbsolutePath().toString();
    }

    /**
     * Inbound FileAdapter Read Operation: Read file bytes from file path
     */
    public byte[] readDocumentFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            System.out.println("[FileAdapter Inbound] Read document from file: " + path.toAbsolutePath());
            return Files.readAllBytes(path);
        }
        throw new IOException("Document file not found at: " + filePath);
    }
}
