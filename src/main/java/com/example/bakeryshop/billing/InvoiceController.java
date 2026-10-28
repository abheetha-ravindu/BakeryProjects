package com.example.bakeryshop.billing;

import com.bakery.bakeryplatform.exception.ResourceNotFoundException;
import com.bakery.bakeryplatform.service.payment.PaymentService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public InvoiceController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Invoice> viewMyInvoices(HttpSession session) {
        Long userId = requireCustomerId(session);
        paymentService.getByUser(userId).stream()
                .forEach(payment -> invoiceService.generateInvoice(payment, userId));
        return invoiceService.getInvoicesByUserId(userId);
    }

    @GetMapping("/{id}")
    public Invoice viewInvoice(@PathVariable Long id, HttpSession session) {
        Long userId = requireCustomerId(session);
        Invoice invoice = invoiceService.getInvoiceByIdAndUserId(id, userId);
        if (invoice == null) {
            throw new ResourceNotFoundException("Invoice not found with id " + id);
        }
        return invoice;
    }

    private static Long requireCustomerId(HttpSession session) {
        Long userId = (Long) session.getAttribute("loggedInUserId");
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Customer login required.");
        }
        return userId;
    }
}
