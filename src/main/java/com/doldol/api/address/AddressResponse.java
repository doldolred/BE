package com.doldol.api.address;

public record AddressResponse(Long id, String label, String receiverName, String receiverPhone, String zipcode, String address1, String address2, boolean defaultAddress) {
    public static AddressResponse from(MemberAddress a) {return new AddressResponse(a.getId(), a.getLabel(), a.getReceiverName(), a.getReceiverPhone(), a.getZipcode(), a.getAddress1(), a.getAddress2(), a.isDefaultAddress());}
}