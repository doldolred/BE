package com.doldol.api.product;

import java.util.Set;

public enum ProductStatus {
    SELLING,
    COMING_SOON,
    HIDDEN;

    public static final Set<ProductStatus> PUBLIC = Set.of(SELLING, COMING_SOON);
    public boolean isPublic(){return this != HIDDEN;}
}
