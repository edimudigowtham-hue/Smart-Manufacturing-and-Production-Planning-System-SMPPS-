package com.genc.smpps.client;

import com.genc.smpps.dto.FactoryMachineDto;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "machine-service", url = "${machine.service.url:http://localhost:8103}")
public interface MachineClient {
    @GetMapping("/api/machines/{machineId}")
    FactoryMachineDto getMachine(@PathVariable("machineId") Integer machineId);

    @PostMapping("/api/machines/{machineId}/availability")
    FactoryMachineDto updateAvailability(@PathVariable("machineId") Integer machineId, @RequestBody Map<String, String> body);
}


