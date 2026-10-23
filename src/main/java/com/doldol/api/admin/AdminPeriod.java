package com.doldol.api.admin;

import com.doldol.api.error.BusinessException;
import com.doldol.api.error.ErrorCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record AdminPeriod(LocalDate from, LocalDate to){
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public static AdminPeriod of(LocalDate from, LocalDate to){
        LocalDate end = to != null ? to : LocalDate.now(KST);
        LocalDate start = from != null ? from : end.minusDays(29);
        if(start.isAfter(end) || ChronoUnit.DAYS.between(start, end) > 366){
            throw new BusinessException(ErrorCode.INVALID_PERIOD);
        }
        return new AdminPeriod(start, end);
    }
    public Instant start(){return from.atStartOfDay(KST).toInstant();
    }
    public Instant endExclusive(){return to.plusDays(1).atStartOfDay(KST).toInstant();
    }
}
