package com.eventsplatform.events.config;

import com.eventsplatform.events.dto.CreateInventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "http://localhost:8082")
public interface InventoryClient {

    @PostMapping("/api/inventories")
    void createInventory(@RequestBody CreateInventoryRequest request);
}
