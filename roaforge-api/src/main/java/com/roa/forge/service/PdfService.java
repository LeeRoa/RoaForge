package com.roa.forge.service;

import com.roa.forge.dto.PdfEditRequest;
import com.roa.forge.dto.PdfImageInsertRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface PdfService {
    byte[] addText(MultipartFile file, int page, float x, float y, String text,
                   float fontSize, String colorHex, float rotationDeg,
                   boolean whiteout, float whiteoutWidth, float whiteoutHeight) throws Exception;


    byte[] addWatermark(MultipartFile file, String text, float fontSize, String colorHex,
                        float opacity, float rotationDeg) throws Exception;


    byte[] addImage(MultipartFile pdf, MultipartFile image,
                    PdfImageInsertRequest req) throws Exception;


    byte[] fillFormField(MultipartFile file, String fieldName, String value, float fontSize) throws Exception;


    byte[] flattenForm(MultipartFile file) throws Exception;


    byte[] addSignatureField(MultipartFile file, int page,
                             float x, float y, float width, float height) throws Exception;

    byte[] edit(MultipartFile pdf, PdfEditRequest req, Map<String, MultipartFile> imageParts) throws Exception;
}
