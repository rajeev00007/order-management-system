
package com.oms.ai;

import com.oms.order.*;
import com.oms.user.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OrderRepository orders;
    private final UserRepository users;

    public AiController(OrderRepository orders, UserRepository users) {
        this.orders = orders;
        this.users = users;
    }

    @PostMapping("/ask")
    public Map<String, String> ask(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String question = body.getOrDefault("question", "").trim().toLowerCase();

        if (question.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Question required"
            );
        }

        AppUser user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        List<CustomerOrder> list =
                user.getRole() == Role.ADMIN
                ? orders.findAllByOrderByCreatedAtDesc()
                : orders.findByCreatedByIdOrderByCreatedAtDesc(user.getId());

        long total = list.size();

        long pending = list.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .count();

        long delivered = list.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .count();

        BigDecimal sales = list.stream()
                .map(CustomerOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String answer;

        if (question.contains("pending")) {
            answer = "You have " + pending + " pending orders.";

        } else if (question.contains("delivered")) {
            answer = "You have " + delivered + " delivered orders.";

        } else if (question.contains("sales") ||
                   question.contains("revenue") ||
                   question.contains("order value")) {
            answer = "The total order value is ₹" + sales + ".";

        } else if (question.contains("how many") ||
                   question.contains("total orders") ||
                   question.contains("number of orders")) {
            answer = "You have " + total + " total orders.";

        } else {
            List<CustomerOrder> matching = list.stream()
                    .filter(o -> question.contains(
                        o.getCustomerName().toLowerCase()
                    ))
                    .toList();

            if (!matching.isEmpty()) {
                answer = matching.stream()
                        .map(o -> "Order #" + o.getId()
                                + " - " + o.getCustomerName()
                                + ", ₹" + o.getAmount()
                                + ", Status: " + o.getStatus())
                        .collect(Collectors.joining("\n"));

            } else {
                answer = list.stream()
                        .limit(10)
                        .map(o -> "Order #" + o.getId()
                                + " - " + o.getCustomerName()
                                + ", ₹" + o.getAmount()
                                + ", Status: " + o.getStatus())
                        .collect(Collectors.joining("\n"));

                if (answer.isBlank()) {
                    answer = "There are no orders available.";
                }
            }
        }

        return Map.of("answer", answer);
    }
}
