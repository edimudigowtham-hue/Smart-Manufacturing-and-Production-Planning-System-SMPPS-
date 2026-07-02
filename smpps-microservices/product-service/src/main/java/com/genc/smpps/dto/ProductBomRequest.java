package com.genc.smpps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProductBomRequest {

    @NotBlank(message = "BOM version is required")
    @Size(max = 20, message = "BOM version must not exceed 20 characters")
    private String bomVersion;

    public String getBomVersion() {
        return bomVersion;
    }

    public void setBomVersion(String bomVersion) {
        this.bomVersion = bomVersion;
    }
}


