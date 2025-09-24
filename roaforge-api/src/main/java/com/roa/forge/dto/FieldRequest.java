package com.roa.forge.dto;

import com.roa.forge.entity.Field;
import com.roa.forge.entity.FieldType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record FieldRequest(
        @NotNull Integer page,
        @NotNull @PositiveOrZero Double x,
        @NotNull @PositiveOrZero Double y,
        @NotNull @Positive Double w,
        @NotNull @Positive Double h,
        @NotNull FieldType type,
        String data
) {
    public Field toEntity() {
        return Field.builder()
                .page(page)
                .x(x)
                .y(y)
                .w(w)
                .h(h)
                .type(type)
                .data(data)
                .build();
    }
}