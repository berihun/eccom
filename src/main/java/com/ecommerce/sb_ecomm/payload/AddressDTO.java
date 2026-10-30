package com.ecommerce.sb_ecomm.payload;

import lombok.Data;
@Data
public class AddressDTO {

    private Long addressId;
    private String street;
    private String buildingName;
    private String city;
    private String state;
    private String country;
    private String pincode;

}
