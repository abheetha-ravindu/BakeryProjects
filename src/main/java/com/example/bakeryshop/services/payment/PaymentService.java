package com.example.bakeryshop.services.payment;

import com.example.bakeryshop.billing.InvoiceService;
import com.example.bakeryshop.dto.payment.PaymentRequest;
import com.example.bakeryshop.exception.BusinessRuleException;
import com.example.bakeryshop.exception.ResourceNotFoundException;
import com.example.bakeryshop.model.order.Order;
import com.example.bakeryshop.model.payment.CardPayment;
import com.example.bakeryshop.model.payment.CashOnDelivery;
import com.example.bakeryshop.model.payment.Payment;
import com.example.bakeryshop.model.payment.PaymentStatus;
import com.example.bakeryshop.model.User;
import com.example.bakeryshop.repisitory.order.OrderRepository;
import com.example.bakeryshop.repisitory.payment.PaymentRepository;
import com.example.bakeryshop.repisitory.UserRepository;
import com.example.bakeryshop.services.EmailService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// Provides Payment service logic.
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            InvoiceService invoiceService,
            UserRepository userRepository,
            EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.invoiceService = invoiceService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Create record  payment for an order
    @Transactional
    public Payment create(PaymentRequest request) {
        Order order = orderRepository.findById(request.orderId()).orElse(null);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id " + request.orderId());
        }
        if (paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.PAID)) {
            throw new BusinessRuleException("This order is already paid.");
        }

        Payment payment;
        if (request.paymentType().equals("CARD")) {
            payment = buildCardPayment(order, request);
        } else {
            payment = new CashOnDelivery(order.getId(), order.getTotalAmount());
        }

        payment.setFee(payment.calculateFee());              // polymo: each type has its own fee
        payment.setReceiptNote(payment.processPayment());    // polymo: each type has its own message
        Payment savedPayment = paymentRepository.save(payment);
        if (savedPayment.getStatus() == PaymentStatus.PAID) {
            var invoice = invoiceService.generateInvoice(savedPayment, order.getUserId());
            User user = userRepository.findById(order.getUserId()).orElse(null);
            emailService.sendInvoice(user == null ? null : user.getEmail(), invoice);
        }
        return savedPayment;
    }

    // Read
    public List<Payment> getAll(PaymentStatus status) {
        if (status == null) {
            return paymentRepository.findAll();
        }
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> getByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    // Payment history of one customer = payments of that customer's orders
    public List<Payment> getByUser(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<Long> orderIds = new ArrayList<>();
        for (Order order : orders) {
            orderIds.add(order.getId());
        }
        if (orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        return paymentRepository.findByOrderIdIn(orderIds);
    }

    public Payment getById(Long id) {
        return findPayment(id);
    }

    // Update: PAID -> REFUNDED
    public Payment updateStatus(Long id, PaymentStatus newStatus) {
        Payment payment = findPayment(id);
        if (!payment.getStatus().canMoveTo(newStatus)) {
            throw new BusinessRuleException(
                    "A payment cannot move from " + payment.getStatus() + " to " + newStatus + ".");
        }
        payment.setStatus(newStatus);
        return paymentRepository.save(payment);
    }

    // Delete: only refunded records can be removed
    public void delete(Long id) {
        Payment payment = findPayment(id);
        if (payment.getStatus() != PaymentStatus.REFUNDED) {
            throw new BusinessRuleException("Only refunded payments can be deleted.");
        }
        paymentRepository.delete(payment);
    }

    private Payment buildCardPayment(Order order, PaymentRequest request) {
        String holder = request.cardHolderName();
        String number = request.cardNumber();
        if (holder == null || holder.trim().isEmpty() || number == null) {
            throw new BusinessRuleException("Card holder name and a 16-digit card number are required for card payments.");
        }
        String last4 = number.substring(number.length() - 4);   // keep only last 4 digits
        return new CardPayment(order.getId(), order.getTotalAmount(), holder.trim(), last4);
    }

    private Payment findPayment(Long id) {
        Payment payment = paymentRepository.findById(id).orElse(null);
        if (payment == null) {
            throw new ResourceNotFoundException("Payment not found with id " + id);
        }
        return payment;
    }
}