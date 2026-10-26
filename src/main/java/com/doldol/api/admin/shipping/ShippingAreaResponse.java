package com.doldol.api.admin.shipping;

import com.doldol.api.shipping.ShippingArea;

public record ShippingAreaResponse(Long id, String name, String zipFrom, String zipTo, int extraFee) {

    public static ShippingAreaResponse from(ShippingArea a) {
        return new ShippingAreaResponse(a.getId(), a.getName(), a.getZipFrom(), a.getZipTo(), a.getExtraFee());
    }

}
