package com.roa.forge.controller;

import com.roa.forge.dto.PdfResult;
import com.roa.forge.dto.ResponseWrapAdvice;
import com.roa.forge.policy.FilenamePolicy;
import com.roa.forge.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdf")
@Tag(name = "PDF", description = "PDF 편집/가공 API")
public class PdfController {
    private final PdfService pdfService;

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

    @PostMapping(
            value = "/text/download",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
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
}
