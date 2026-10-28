package com.example.bakeryshop.billing;

import com.bakery.bakeryplatform.model.payment.Payment;
import com.bakery.bakeryplatform.model.payment.PaymentStatus;
import com.bakery.bakeryplatform.repository.order.OrderRepository;
import com.bakery.bakeryplatform.service.payment.PaymentService;
import com.example.bakeryprojects.model.User;
import com.example.bakeryprojects.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/invoices")
public class AdminInvoiceController {
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminInvoiceController(
            InvoiceService invoiceService,
            PaymentService paymentService,
            OrderRepository orderRepository,
            UserRepository userRepository) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<AdminInvoice> getAllInvoices() {
        List<Payment> payments = paymentService.getAll(null);
        Map<Long, Payment> paymentsById = payments.stream()
                .collect(Collectors.toMap(Payment::getId, payment -> payment));

        payments.forEach(payment -> orderRepository.findById(payment.getOrderId())
                .ifPresent(order -> invoiceService.generateInvoice(payment, order.getUserId())));

        List<Invoice> invoices = invoiceService.getAllInvoices();
        Set<Long> userIds = invoices.stream()
                .map(Invoice::getUserId)
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());
        Map<Long, User> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return invoices.stream()
                .map(invoice -> toAdminInvoice(invoice, paymentsById, usersById))
                .toList();
    }

    private static AdminInvoice toAdminInvoice(
            Invoice invoice,
            Map<Long, Payment> paymentsById,
            Map<Long, User> usersById) {
        User user = usersById.get(invoice.getUserId());
        Payment payment = paymentsById.get(invoice.getPaymentId());
        return new AdminInvoice(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getPaymentId(),
                invoice.getOrderId(),
                invoice.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getEmail(),
                invoice.getPaymentType(),
                invoice.getAmount(),
                payment == null ? null : payment.getStatus());
    }

    public record AdminInvoice(
            Long id,
            String invoiceNumber,
            LocalDate invoiceDate,
            Long paymentId,
            Long orderId,
            Long userId,
            String customerName,
            String customerEmail,
            String paymentType,
            BigDecimal amount,
            PaymentStatus paymentStatus) {
    }
}
