package com.genc.smpps.client;

import com.genc.smpps.dto.FactoryMachineDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "machine-service")
public interface MachineClient {
    @GetMapping("/api/machines/{machineId}")
    FactoryMachineDto getMachine(@PathVariable Integer machineId);
}

