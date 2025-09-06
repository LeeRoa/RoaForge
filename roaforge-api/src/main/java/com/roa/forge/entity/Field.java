package com.roa.forge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@ToString(exclude = "document")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(
        name = "field",
        indexes = {
                @Index(name = "idx_field_document_page", columnList = "document_id,page")
        }
)
@Getter
public class Field extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @NotNull @Min(1)
    @Column(name = "page", nullable = false)
    private Integer page;

    @NotNull @PositiveOrZero
    @Column(name = "x", nullable = false)
    private Double x;

    @NotNull @PositiveOrZero
    @Column(name = "y", nullable = false)
    private Double y;

    @NotNull @Positive
    @Column(name = "w", nullable = false)
    private Double w;

    @NotNull @Positive
    @Column(name = "h", nullable = false)
    private Double h;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private FieldType type;

    @Column(name = "data", length = 1000)
    private String data;

    @Builder
    public Field(Integer page, Double x, Double y, Double w, Double h, FieldType type, String data) {
        this.page = page;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.type = type;
        this.data = data;
    }

    void setDocumentInternal(Document document) {
        this.document = document;
    }
}