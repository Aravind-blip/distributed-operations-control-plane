package com.opscontrolplane.emsbridge;

import com.opscontrolplane.emsbridge.dto.EmsMessage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ems-bridge")
public class EmsBridgeController {

    private final EmsBridgeService emsBridgeService;

    public EmsBridgeController(EmsBridgeService emsBridgeService) {
        this.emsBridgeService = emsBridgeService;
    }

    @PostMapping("/simulate")
    public Map<String, Object> simulate(@Valid @RequestBody EmsMessage message) {
        return emsBridgeService.normalizeAndPublish(message);
    }
}
