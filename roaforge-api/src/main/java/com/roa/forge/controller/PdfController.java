package com.roa.forge.controller;

import com.roa.forge.dto.PdfEditRequest;
import com.roa.forge.dto.PdfImageInsertRequest;
import com.roa.forge.dto.PdfResult;
import com.roa.forge.dto.ResponseWrapAdvice;
import com.roa.forge.policy.FilenamePolicy;
import com.roa.forge.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdf")
@Tag(name = "PDF", description = "PDF 편집/가공 API")
public class PdfController {
    private final PdfService pdfService;
    private static final String NO_CACHE_HEADER = "no-store, no-cache, must-revalidate, max-age=0";

    private PdfResult setPdfResult(byte[] bytes, String filename) {
        String b64 = Base64.getEncoder().encodeToString(bytes);
        return new PdfResult(filename, b64, bytes.length);
    }

    @PostMapping(
            value = "/text",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "텍스트 추가", description = "지정 페이지/좌표에 텍스트(한글 포함)를 추가합니다. 필요 시 화이트아웃 박스로 기존 텍스트를 가릴 수 있습니다.")
    public PdfResult addText(
            @Parameter(description = "원본 PDF 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "페이지 번호(1-base)", required = true)
            @RequestParam int page,
            @Parameter(description = "X 좌표", required = true)
            @RequestParam float x,
            @Parameter(description = "Y 좌표", required = true)
            @RequestParam float y,
            @Parameter(description = "추가할 텍스트", required = true)
            @RequestParam String text,
            @Parameter(description = "폰트 크기 (기본 14)") @RequestParam(defaultValue = "14") float fontSize,
            @Parameter(description = "글자 색상 HEX (기본 #000000)") @RequestParam(defaultValue = "#000000") String colorHex,
            @Parameter(description = "회전 각도(deg, 기본 0)") @RequestParam(defaultValue = "0") float rotationDeg,
            @Parameter(description = "화이트아웃 사용 여부 (기본 false)") @RequestParam(defaultValue = "false") boolean whiteout,
            @Parameter(description = "화이트아웃 너비 (기본 0)") @RequestParam(defaultValue = "0") float whiteoutWidth,
            @Parameter(description = "화이트아웃 높이 (기본 0)") @RequestParam(defaultValue = "0") float whiteoutHeight,
            @RequestParam(required = false) String outputName
    ) throws Exception {
        String filename = FilenamePolicy.build("text", file.getOriginalFilename(), outputName);
        return setPdfResult(pdfService.addText(file, page, x, y, text, fontSize, colorHex, rotationDeg, whiteout, whiteoutWidth, whiteoutHeight), filename);
    }

    /*
        PDF 편집 - Text
     */
    @PostMapping(
            value = "/text/download",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = { MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE })
    @ResponseWrapAdvice.NoWrap
    public ResponseEntity<byte[]> addTextDownload(
            @Parameter(description = "원본 PDF 파일", required = true)
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "페이지 번호(1-base)", required = true)
            @RequestParam int page,
            @Parameter(description = "X 좌표", required = true)
            @RequestParam float x,
            @Parameter(description = "Y 좌표", required = true)
            @RequestParam float y,
            @Parameter(description = "추가할 텍스트", required = true)
            @RequestParam String text,
            @Parameter(description = "폰트 크기 (기본 14)") @RequestParam(defaultValue = "14") float fontSize,
            @Parameter(description = "글자 색상 HEX (기본 #000000)") @RequestParam(defaultValue = "#000000") String colorHex,
            @Parameter(description = "회전 각도(deg, 기본 0)") @RequestParam(defaultValue = "0") float rotationDeg,
            @Parameter(description = "화이트아웃 사용 여부 (기본 false)") @RequestParam(defaultValue = "false") boolean whiteout,
            @Parameter(description = "화이트아웃 너비 (기본 0)") @RequestParam(defaultValue = "0") float whiteoutWidth,
            @Parameter(description = "화이트아웃 높이 (기본 0)") @RequestParam(defaultValue = "0") float whiteoutHeight,
            @RequestParam(required = false) String outputName
    ) throws Exception {
        byte[] out = pdfService.addText(file, page, x, y, text, fontSize, colorHex, rotationDeg, whiteout, whiteoutWidth, whiteoutHeight);
        String filename = FilenamePolicy.build("text", file.getOriginalFilename(), outputName);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition","attachment; filename=\"" + filename + "\"")
                .body(out);
    }

    /*
        PDF 편집 - Image
     */
    @PostMapping(
            value = "/image/download",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = { MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE }
    )
    @ResponseWrapAdvice.NoWrap
    @Operation(summary = "이미지 추가 (다운로드)")
    public ResponseEntity<byte[]> addImageDownload(
            @RequestPart("pdf") MultipartFile pdf,
            @RequestPart("image") MultipartFile image,
            @Valid @ModelAttribute PdfImageInsertRequest req
    ) throws Exception {
        byte[] pdfBytes = pdfService.addImage(pdf, image, req);
        String filename = FilenamePolicy.build("image", pdf.getOriginalFilename(), req.getOutputName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(filename))
                .header(HttpHeaders.CACHE_CONTROL, NO_CACHE_HEADER)
                .header("Pragma", "no-cache")
                .body(pdfBytes);
    }

    private String buildContentDisposition(String filename) {
        String asciiFallback = filename.replaceAll("[^\\x20-\\x7E]", "_");
        String encoded = UriUtils.encode(filename, StandardCharsets.UTF_8);
        return "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded;
    }

    @PostMapping(
            value = "/edit",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = { MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE }
    )
    @ResponseWrapAdvice.NoWrap
    @Operation(summary = "배치 편집(다운로드)", description = "여러 작업을 한 번에 적용하고 PDF 바이너리로 반환")
    public ResponseEntity<byte[]> edit(
            @RequestPart("pdf") MultipartFile pdf,
            @Valid @ModelAttribute PdfEditRequest req,
            @RequestPart(required = false) Map<String, MultipartFile> imageParts
    ) throws Exception {
        byte[] pdfBytes = pdfService.edit(pdf, req, imageParts == null ? Map.of() : imageParts);
        String filename = FilenamePolicy.build("edit", pdf.getOriginalFilename(), req.getOutputName());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, buildContentDisposition(filename))
                .header(HttpHeaders.CACHE_CONTROL, NO_CACHE_HEADER)
                .header("Pragma", "no-cache")
                .body(pdfBytes);
    }
}
