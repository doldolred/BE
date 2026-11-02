package com.doldol.api.point;

public record PointSummaryResponse(int balance, int expiringSoon, int earnRate, int minUse, int minPayment, int validDays) { }
