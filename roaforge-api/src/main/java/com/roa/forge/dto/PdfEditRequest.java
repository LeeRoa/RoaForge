package com.roa.forge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Data
@Schema(description = "PDF 통합 편집 요청")
public class PdfEditRequest {
    @Schema(description = "결과 파일명(확장자 생략 가능)", example = "edited-doc")
    private String outputName;

    @Valid
    @NotNull
    @Size(min = 1, message = "하나 이상의 작업이 필요합니다.")
    private List<Operation> operations;

    @Schema(description = "편집 작업 유형")
    public enum OpType { TEXT, IMAGE, RECT, LINE }

    @Getter
    @Schema(description = "공통 작업")
    public static abstract class Operation {
        @NotNull
        private OpType type;

        @Min(1)
        @Schema(description = "페이지 번호(1-base)", example = "1")
        private int page;

        @Schema(description = "X 좌표(pt, 좌하단 기준)", example = "100")
        private float x;

        @Schema(description = "Y 좌표(pt, 좌하단 기준)", example = "120")
        private float y;

        public abstract OpType getType();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "텍스트 추가 작업")
    public static class TextOp extends Operation {
        @NotBlank
        private String text;

        @Positive
        @Schema(defaultValue = "14")
        private float fontSize = 14f;

        @NotBlank
        @Schema(description = "HEX 색상", example = "#000000", defaultValue = "#000000")
        private String colorHex = "#000000";

        @Schema(description = "회전(deg)", defaultValue = "0")
        private float rotationDeg = 0f;

        @Schema(description = "화이트아웃 사용", defaultValue = "false")
        private boolean whiteout = false;

        @Schema(description = "화이트아웃 너비", defaultValue = "0")
        private float whiteoutWidth = 0f;

        @Schema(description = "화이트아웃 높이", defaultValue = "0")
        private float whiteoutHeight = 0f;

        @Override public OpType getType() { return OpType.TEXT; }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "이미지 추가 작업")
    public static class ImageOp extends Operation {
        @Positive(message = "가로는 양수여야 합니다.")
        private Float width;

        @Positive(message = "세로는 양수여야 합니다.")
        private Float height;

        @DecimalMin("0.0") @DecimalMax("1.0")
        @Schema(description = "불투명도(0~1)", defaultValue = "1.0")
        private Float opacity = 1.0f;

        // 업로드된 이미지 파트명과 매칭할 키(프론트가 multipart part 이름과 매핑)
        @NotBlank
        @Schema(description = "이미지 파트 키", example = "image1")
        private String imageKey;

        @Override public OpType getType() { return OpType.IMAGE; }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "사각형(도형) 채우기/선택")
    public static class  RectOp extends Operation {
        @Min(1) public int page;
        public float x; public float y; public float w; public float h;
        public String fillColor;    // #RRGGBB (옵션)
        public String strokeColor;  // (옵션)
        public Float strokeWidth;   // (옵션)
        public float opacity = 1.0f;

        @Override public OpType getType() { return OpType.RECT; }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "선 그리기")
    public static class  LineOp extends Operation {
        @Min(1) public int page;
        public float x1; public float y1; public float x2; public float y2;
        public String strokeColor;  // (옵션)
        public Float strokeWidth;   // (옵션)
        public float opacity = 1.0f;

        @Override
        public OpType getType() {
            return OpType.LINE;
        }
    }
}