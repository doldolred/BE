package com.doldol.api.inquiry;

import com.doldol.api.common.BaseEntity;
import com.doldol.api.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryCategory category;
    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(length = 30)
    private String orderNumber;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;
    @Column(columnDefinition = "TEXT")
    private String answer;
    private Instant answeredAt;
    public static Inquiry create(Member member, InquiryCategory category, String title, String content, String orderNumber){
        Inquiry inquiry = new Inquiry();
        inquiry.member = member;
        inquiry.category = category;
        inquiry.title = title.trim();
        inquiry.content = content.strip();
        inquiry.orderNumber = orderNumber;
        inquiry.status = InquiryStatus.WAITING;
        return inquiry;
    }
    public boolean answer(String answer){
        boolean first = status == InquiryStatus.WAITING;
        this.answer = answer.strip();
        this.status = InquiryStatus.ANSWERED;
        this.answeredAt = Instant.now();
        return first;
    }
    public boolean isAnswered(){
        return status == InquiryStatus.ANSWERED;
    }

}
