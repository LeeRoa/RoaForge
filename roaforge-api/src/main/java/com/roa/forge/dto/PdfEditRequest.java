package com.roa.forge.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class PdfEditRequest {
    @NotNull
    private List<Operation> operations;

    private String outputName;
    private Float defaultFontSize;
    private String defaultTextColor;

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY, // JSON에 있는 "type" 필드 사용
            property = "type",
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TextOp.class,  name = "text"),
            @JsonSubTypes.Type(value = ImageOp.class, name = "image"),
            @JsonSubTypes.Type(value = RectOp.class,  name = "rect"),
            @JsonSubTypes.Type(value = LineOp.class,  name = "line")
    })
    @Data
    public static abstract class Operation {
        @NotBlank
        protected String type;   // JSON의 타입 디스크리미네이터
        protected Integer z;     // 레이어/정렬용(옵션)
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @JsonTypeName("text")
    public static class TextOp extends Operation {
        @Min(1) private int page;
        private float x, y;
        @NotBlank private String text;
        private Float fontSize;
        private String colorHex;
        private float rotationDeg = 0f;
        private boolean whiteout = false;
        private Float whiteoutWidth, whiteoutHeight;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @JsonTypeName("image")
    public static class ImageOp extends Operation {
        @Min(1) private int page;
        private float x, y;
        @NotBlank private String asset; // 업로드한 파일명과 매칭
        private Float width, height;
        private float opacity = 1.0f;
        private float rotationDeg = 0f;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @JsonTypeName("rect")
    public static class RectOp extends Operation {
        @Min(1) private int page;
        private float x, y, w, h;
        private String fillColor, strokeColor;
        private Float strokeWidth;
        private float opacity = 1.0f;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @JsonTypeName("line")
    public static class LineOp extends Operation {
        @Min(1) private int page;
        private float x1, y1, x2, y2;
        private String strokeColor;
        private Float strokeWidth;
        private float opacity = 1.0f;
    }
}