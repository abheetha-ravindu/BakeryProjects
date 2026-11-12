package com.example.bakeryshop.billing;

import com.example.bakeryshop.exception.ResourceNotFoundException;
import com.example.bakeryshop.model.order.Order;
import com.example.bakeryshop.repisitory.order.OrderRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/refund")
// Handles Refund controller logic.
public class RefundController {
    private final RefundService refundService;
    private final OrderRepository orderRepository;

    public RefundController(RefundService refundService, OrderRepository orderRepository) {
        this.refundService = refundService;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<RefundRequest> applyForRefund(
            @Valid @RequestBody RefundRequest request, HttpSession session) {
        // A logged-in customer must exist before a refund can be submitted.
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Customer login required.");
        }

        // Check that the order exists and belongs to the same customer requesting the refund.
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + request.getOrderId()));
        if (!userId.equals(order.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only request a refund for your own orders.");
        }

        RefundRequest savedRequest = refundService.submitRefundRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRequest);
    }

    @PutMapping("/{id}")
    public RefundRequest processRefund(@PathVariable Long id, @RequestParam String status) {
        return refundService.updateRefundStatus(id, status);
    }

    @GetMapping
    public List<RefundRequest> viewAllRefundRequest() {
        return refundService.getAllRefundRequest();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelRefundRequest(@PathVariable Long id) {
        refundService.withdrawRefundRequest(id);
        return ResponseEntity.noContent().build();
    }
}
