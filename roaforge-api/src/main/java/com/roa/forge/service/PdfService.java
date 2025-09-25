package com.roa.forge.service;

import org.springframework.web.multipart.MultipartFile;

public interface PdfService {
    byte[] addText(MultipartFile file, int page, float x, float y, String text,
                   float fontSize, String colorHex, float rotationDeg,
                   boolean whiteout, float whiteoutWidth, float whiteoutHeight) throws Exception;


    byte[] addWatermark(MultipartFile file, String text, float fontSize, String colorHex,
                        float opacity, float rotationDeg) throws Exception;


    byte[] addImage(MultipartFile pdf, org.springframework.web.multipart.MultipartFile image,
                    int page, float x, float y, Float width, Float height, float opacity) throws Exception;


    byte[] fillFormField(MultipartFile file, String fieldName, String value, float fontSize) throws Exception;


    byte[] flattenForm(MultipartFile file) throws Exception;


    byte[] addSignatureField(MultipartFile file, int page,
                             float x, float y, float width, float height) throws Exception;
}
