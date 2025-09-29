package com.roa.forge.service.impl;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.roa.forge.dto.ErrorCode;
import com.roa.forge.exception.AppException;
import com.roa.forge.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    @Value("${app.pdf.font-path}")
    private String fontPath;
    private volatile PdfFont cachedFont;
    private final MessageSource messageSource;
    private final ResourceLoader resourceLoader;

    /** 폰트 프로그램(바이트)만 캐시 — PdfFont 인스턴스는 절대 캐시 금지 */
    private volatile byte[] cachedFontProgram;

    private PdfFont loadFont() throws IOException {
        if (cachedFont != null) return cachedFont;

        Resource resource = resourceLoader.getResource(fontPath);
        try (InputStream is = resource.getInputStream()) {
            byte[] fontBytes = is.readAllBytes();
            PdfFont font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED, true);
            cachedFont = font;
            return font;
        }
    }

    private DeviceRgb parseHexColor(String hexLiteral) {
        if (hexLiteral == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    messageSource.getMessage("error.null_color", null, "Invalid", LocaleContextHolder.getLocale()));
        }

        String trimmed = hexLiteral.trim();
        if (trimmed.startsWith("#")) trimmed = trimmed.substring(1);
        if (trimmed.regionMatches(true, 0, "0x", 0, 2)) trimmed = trimmed.substring(2);

        // 허용 길이: 3(RGB), 4(RGBA), 6(RRGGBB), 8(RRGGBBAA)
        int len = trimmed.length();
        boolean isShorthand = (len == 3 || len == 4);
        boolean isFullForm  = (len == 6 || len == 8);

        if (!(isShorthand || isFullForm) || !trimmed.matches("(?i)^[0-9a-f]+$")) {
            throw new IllegalArgumentException("Invalid hex color: " + hexLiteral);
        }

        // #RGB / #RGBA → #RRGGBB / #RRGGBBAA로 확장
        String normalized = isShorthand ? expandShorthand(trimmed) : trimmed;

        int red   = Integer.parseInt(normalized.substring(0, 2), 16);
        int green = Integer.parseInt(normalized.substring(2, 4), 16);
        int blue  = Integer.parseInt(normalized.substring(4, 6), 16);

        return new DeviceRgb(red, green, blue);
    }

    private String expandShorthand(String shorthand) {
        StringBuilder expanded = new StringBuilder(shorthand.length() * 2);
        for (int i = 0; i < shorthand.length(); i++) {
            char ch = shorthand.charAt(i);
            expanded.append(ch).append(ch); // 'A' -> "AA"
        }
        return expanded.toString();
    }


    private void validatePage(PdfDocument pdfDoc, int page) {
        if (page < 1 || page > pdfDoc.getNumberOfPages()) {
            throw new IllegalArgumentException("Invalid page number: " + page);
        }
    }

    @Override
    public byte[] addText(MultipartFile file, int page, float x, float y, String text,
                          float fontSize, String colorHex, float rotationDeg,
                          boolean whiteout, float whiteoutWidth, float whiteoutHeight) throws Exception {

        try (InputStream in = file.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfDocument pdfDoc = new PdfDocument(new PdfReader(in), new PdfWriter(baos));
            try (Document doc = new Document(pdfDoc)) {
                // 유효성
                if (page < 1 || page > pdfDoc.getNumberOfPages()) {
                    throw new IllegalArgumentException("Invalid page: " + page);
                }

                PdfFont font = newPdfFontForDoc();             // 문서 전용 폰트(요청마다 새로)
                DeviceRgb color = parseHexColor(colorHex);
                float rad = (float) Math.toRadians(rotationDeg);

                PdfPage pdfPage = pdfDoc.getPage(page);

                // 화이트아웃(선택)
                if (whiteout && whiteoutWidth > 0 && whiteoutHeight > 0) {
                    PdfCanvas pc = new PdfCanvas(pdfPage);
                    pc.saveState();
                    pc.setFillColor(com.itextpdf.kernel.colors.ColorConstants.WHITE);
                    pc.rectangle(x, y, whiteoutWidth, whiteoutHeight);
                    pc.fill();
                    pc.restoreState();
                }

                // 텍스트
                Paragraph p = new Paragraph(text)
                        .setFont(font)
                        .setFontSize(fontSize)
                        .setFontColor(color);

                doc.showTextAligned(p, x, y, page,
                        TextAlignment.LEFT, VerticalAlignment.BOTTOM, rad);

            }

            return baos.toByteArray();
        }
    }

    private byte[] getFontProgram() throws IOException {
        byte[] fp = cachedFontProgram;
        if (fp != null) return fp;
        synchronized (this) {
            if (cachedFontProgram == null) {
                Resource res = resourceLoader.getResource(fontPath);
                try (var is = res.getInputStream()) {
                    cachedFontProgram = is.readAllBytes();
                }
            }
            return cachedFontProgram;
        }
    }

    private PdfFont newPdfFontForDoc() throws IOException {
        return PdfFontFactory.createFont(getFontProgram(), PdfEncodings.IDENTITY_H);
    }

    @Override
    public byte[] addWatermark(MultipartFile file, String text, float fontSize, String colorHex, float opacity, float rotationDeg) throws Exception {
        PdfFont font = loadFont();
        DeviceRgb color = parseHexColor(colorHex);

        try (InputStream in = file.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfDocument pdfDoc = new PdfDocument(new PdfReader(in), new PdfWriter(baos))) {

            int total = pdfDoc.getNumberOfPages();
            for (int i = 1; i <= total; i++) {
                PdfPage page = pdfDoc.getPage(i);
                Rectangle pageSize = page.getPageSize();
                PdfCanvas pdfCanvas = new PdfCanvas(page);

                PdfExtGState gs = new PdfExtGState().setFillOpacity(opacity).setStrokeOpacity(opacity);
                pdfCanvas.saveState();
                pdfCanvas.setExtGState(gs);

                try (Canvas canvas = new Canvas(pdfCanvas, page.getPageSize())) {
                    Paragraph p = new Paragraph(text)
                            .setFont(font)
                            .setFontSize(fontSize)
                            .setFontColor(color);
                    double rad = Math.toRadians(rotationDeg);
                    float cx = pageSize.getWidth() / 2f;
                    float cy = pageSize.getHeight() / 2f;
                    canvas.showTextAligned(p, cx, cy, i, TextAlignment.CENTER, VerticalAlignment.MIDDLE, (float) rad);
                }

                pdfCanvas.restoreState();
            }

            pdfDoc.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] addImage(MultipartFile pdf, MultipartFile image,
                           int page, float x, float y,
                           Float width, Float height, float opacity) throws Exception {
        // opacity 클램핑 (선택)
        if (opacity < 0f) opacity = 0f;
        if (opacity > 1f) opacity = 1f;

        try (InputStream inPdf = pdf.getInputStream();
             InputStream inImg = image.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfDocument pdfDoc = new PdfDocument(new PdfReader(inPdf), new PdfWriter(baos));
            try (Document doc = new Document(pdfDoc)) {
                // 페이지 검증
                validatePage(pdfDoc, page);

                // 이미지 데이터
                byte[] imgBytes = inImg.readAllBytes();
                ImageData imgData = ImageDataFactory.create(imgBytes);
                Image img = getImage(width, height, imgData);

                // 위치/불투명도 설정
                img.setFixedPosition(page, x, y); // 좌하단 기준 (pt)
                img.setOpacity(opacity);          // 0.0 ~ 1.0

                doc.add(img);
            }

            return baos.toByteArray();
        }
    }

    private Image getImage(Float width, Float height, ImageData imgData) {
        Image img = new Image(imgData);

        // 크기 결정 (비율 유지)
        float naturalW = imgData.getWidth();
        float naturalH = imgData.getHeight();

        if (width != null && height != null) {
            // 둘 다 지정 → 정확히 맞춤 (비율 무시)
            img.setWidth(width);
            img.setHeight(height);
        } else if (width != null) {
            // width 만 지정 → 비율 유지
            float scale = width / naturalW;
            img.setWidth(width);
            img.setHeight(naturalH * scale);
        } else if (height != null) {
            // height 만 지정 → 비율 유지
            float scale = height / naturalH;
            img.setWidth(naturalW * scale);
            img.setHeight(height);
        } else {
            // 아무것도 없으면 원본 크기 그대로
            img.setAutoScale(false);
        }
        return img;
    }

    @Override
    public byte[] fillFormField(MultipartFile file, String fieldName, String value, float fontSize) throws Exception {
        PdfFont font = loadFont();
        try (InputStream in = file.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfDocument pdfDoc = new PdfDocument(new PdfReader(in), new PdfWriter(baos))) {

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            PdfFormField field = form.getField(fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Form field not found: " + fieldName);
            }
            field.setFont(font);
            field.setFontSize(fontSize);
            field.setValue(value);

            pdfDoc.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] flattenForm(MultipartFile file) throws Exception {
        try (InputStream in = file.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfDocument pdfDoc = new PdfDocument(new PdfReader(in), new PdfWriter(baos))) {

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);
            if (form != null) {
                form.flattenFields();
            }

            pdfDoc.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] addSignatureField(MultipartFile file, int page, float x, float y, float width, float height) throws Exception {
        try (InputStream in = file.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfDocument pdfDoc = new PdfDocument(new PdfReader(in), new PdfWriter(baos))) {

            validatePage(pdfDoc, page);
            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);

            Rectangle rect = new Rectangle(x, y, width, height);
            PdfSignatureFormField sig = PdfFormField.createSignature(pdfDoc, rect);
            form.addField(sig, pdfDoc.getPage(page));

            pdfDoc.close();
            return baos.toByteArray();
        }
    }
}
