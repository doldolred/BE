package com.doldol.api.notice;

import com.doldol.api.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(nullable = false)
    private boolean pinned;
    @Column(nullable = false)
    private boolean published;
    public static Notice create(String title, String content, boolean pinned, boolean published) {
        Notice notice = new Notice();
        notice.update(title, content, pinned, published);
        return notice;
    }
    public void update(String title, String content, boolean pinned, boolean published) {
        this.title = title.trim();
        this.content = content.strip();
        this.pinned = pinned;
        this.published = published;
    }

}