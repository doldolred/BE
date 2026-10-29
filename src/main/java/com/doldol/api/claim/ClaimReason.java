package com.doldol.api.claim;

public enum ClaimReason {
    CHANGE_OF_MIND,
    DEFECTIVE,
    WRONG_ITEM,
    DELAYED,
    OTHER;
    public boolean isSellerFault(){return this == DEFECTIVE || this == WRONG_ITEM;}
}
