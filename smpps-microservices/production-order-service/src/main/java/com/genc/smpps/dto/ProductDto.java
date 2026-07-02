package com.genc.smpps.dto;

public record ProductDto(Integer productId, String productCode, String productName, String bomVersion, Double standardCost, String productStatus) {
}
