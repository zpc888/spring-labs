package com.example.demo.controller;

import com.example.demo.model.CardTypeAndNumber;
import com.example.demo.model.TransferRequest;
import com.example.demo.resolver.Channel;
import com.example.demo.resolver.CurrentCard;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TransferController {

    @PostMapping(path="/transfer1")
    public ResponseEntity<Map<String, String>> transfer1(@RequestBody TransferRequest request, @Channel String channel) {
        String fromAccount = request.getFromAccount();
        String toAccount = request.getToAccount();

        if (fromAccount == null || fromAccount.isEmpty()) {
            throw new IllegalArgumentException("From account cannot be null or empty");
        }
        if (toAccount == null || toAccount.isEmpty()) {
            throw new IllegalArgumentException("To account cannot be null or empty");
        }

        return ResponseEntity.ok(Map.of(
                "channel", channel,
                "fromAccount", fromAccount,
                "toAccount", toAccount,
                "status", "Transfer initiated"
        ));
    }

    @PostMapping(path="/transfer2")
    public ResponseEntity<Map<String, String>> transfer2(@CurrentCard CardTypeAndNumber cardTypeAndNumber,
                                                        @RequestBody TransferRequest request) {
        String fromAccount = request.getFromAccount();
        String toAccount = request.getToAccount();

        if (fromAccount == null || fromAccount.isEmpty()) {
            throw new IllegalArgumentException("From account cannot be null or empty");
        }
        if (toAccount == null || toAccount.isEmpty()) {
            throw new IllegalArgumentException("To account cannot be null or empty");
        }

        return ResponseEntity.ok(Map.of(
                "cardType", cardTypeAndNumber.cardType().name(),
                "cardNumber", cardTypeAndNumber.cardNumber(),
                "fromAccount", fromAccount,
                "toAccount", toAccount,
                "status", "Transfer initiated"
        ));
    }

    @PostMapping(path="/transfer3")
    public ResponseEntity<Map<String, String>> transfer3(@CurrentCard CardTypeAndNumber cardTypeAndNumber,
                                                         @Channel String channel,
                                                         @RequestBody TransferRequest request) {
        String fromAccount = request.getFromAccount();
        String toAccount = request.getToAccount();

        if (fromAccount == null || fromAccount.isEmpty()) {
            throw new IllegalArgumentException("From account cannot be null or empty");
        }
        if (toAccount == null || toAccount.isEmpty()) {
            throw new IllegalArgumentException("To account cannot be null or empty");
        }

        return ResponseEntity.ok(Map.of(
                "cardType", cardTypeAndNumber.cardType().name(),
                "cardNumber", cardTypeAndNumber.cardNumber(),
                "channel", channel,
                "fromAccount", fromAccount,
                "toAccount", toAccount,
                "status", "Transfer initiated"
        ));
    }

    @GetMapping(path="/transfer4")
    public ResponseEntity<Map<String, String>> transfer3(@CurrentCard CardTypeAndNumber cardTypeAndNumber,
                                                         @Channel String channel) {
        return ResponseEntity.ok(Map.of(
                "cardType", cardTypeAndNumber.cardType().name(),
                "cardNumber", cardTypeAndNumber.cardNumber(),
                "channel", channel,
                "status", "Transfer initiated"
        ));
    }
}