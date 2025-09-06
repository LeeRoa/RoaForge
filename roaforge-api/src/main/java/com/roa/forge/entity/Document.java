package com.roa.forge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "document")
@ToString(exclude = "fields")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "path", length = 500)
    private String path;

    @Min(0)
    @Column(name = "page_count")
    private Integer pageCount;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Field> fields = new ArrayList<>();

    @Builder
    private Document(String name, String path, Integer pageCount, List<Field> fields) {
        this.name = name;
        this.path = path;
        this.pageCount = pageCount;
        if (fields != null) {
            this.fields = fields;
            // 넘어온 필드가 있으면 역참조 정리
            this.fields.forEach(f -> f.setDocumentInternal(this));
        }
    }

    public void addField(Field field) {
        if (field == null) return;
        Document prev = field.getDocument();
        if (prev != null && prev != this) prev.removeField(field);
        if (!this.fields.contains(field)) this.fields.add(field);
        field.setDocumentInternal(this);
    }

    public void removeField(Field field) {
        if (field == null) return;
        if (this.fields.remove(field)) field.setDocumentInternal(null);
    }
}