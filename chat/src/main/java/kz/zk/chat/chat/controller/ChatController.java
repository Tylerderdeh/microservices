package kz.zk.chat.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@Validated
@RestController(value = "/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

//    @PostMapping("/dialogs")
////    @Operation(summary = "Create or get dialog", description = "Creates a new dialog with a store or returns existing one (idempotent)")
//    public ResponseEntity<ChatDialogResponse> createDistributorDialog(
//            @RequestBody CreateDialogByDistributorRequest request) {
//
//        UUID distributorId = UUID.fromString(SecurityOperations.extractTenantId());
//        log.info("Distributor {} creating/getting dialog with store {}", distributorId, request.getStoreId());
//        ChatDialogResponse response = dialogService.getOrCreateDialogResponseByDistributor(distributorId, request.getStoreId());
//        return ResponseEntity.ok(response);
//    }
}