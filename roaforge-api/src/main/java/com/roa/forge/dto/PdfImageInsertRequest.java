package com.roa.forge.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "PDF 이미지 삽입 요청")
public class PdfImageInsertRequest {

    @Schema(description = "페이지 번호(1-base)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(1)
    private int page;

    @Schema(description = "X 좌표(pt, 좌하단 기준)", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private float x;

    @Schema(description = "Y 좌표(pt, 좌하단 기준)", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
    private float y;

    @Schema(description = "가로(pt). 생략 시 원본 비율 유지", example = "200")
    @Positive
    private Float width;

    @Schema(description = "세로(pt). 생략 시 원본 비율 유지", example = "80")
    @Positive
    private Float height;

    @Schema(description = "불투명도(0.0~1.0). 기본 1.0", example = "1.0")
    @DecimalMin("0.0") @DecimalMax("1.0")
    private Float opacity = 1.0f;  // null 방지

    @Schema(description = "결과 파일명(확장자 생략 가능)", example = "demo-img")
    private String outputName;
}
