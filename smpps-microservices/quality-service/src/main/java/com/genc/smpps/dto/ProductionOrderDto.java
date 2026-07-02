package com.genc.smpps.dto;

import java.time.LocalDate;

public record ProductionOrderDto(Integer orderId, Integer productId, Integer plannedQuantity, Integer producedQuantity,
                                 LocalDate startDate, LocalDate endDate, String orderStatus) {
}
