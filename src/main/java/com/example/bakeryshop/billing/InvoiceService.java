package com.example.bakeryshop.billing;

import com.bakery.bakeryplatform.model.payment.Payment;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice generateInvoice(Payment payment, Long userId) {
        return invoiceRepository.findByPaymentId(payment.getId()).orElseGet(() -> {
            PaymentInvoice invoice = new PaymentInvoice();
            invoice.setPaymentId(payment.getId());
            invoice.setOrderId(payment.getOrderId());
            invoice.setUserId(userId);
            invoice.setPaymentType(payment.getPaymentType());
            invoice.setAmount(payment.getTotalCharged());
            invoice.setInvoiceDate(LocalDate.now());
            invoice.setInvoiceNumber("INV-PAY-" + invoice.getInvoiceDate().getYear()
                    + "-" + String.format("%06d", payment.getId()));
            return invoiceRepository.save(invoice);
        });
    }

    public Invoice getInvoiceByIdAndUserId(Long id, Long userId) {
        return invoiceRepository.findById(id)
                .filter(invoice -> userId.equals(invoice.getUserId()))
                .orElse(null);
    }

    public List<Invoice> getInvoicesByUserId(Long userId) {
        return invoiceRepository.findByUserIdOrderByInvoiceDateDesc(userId);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByInvoiceDateDesc();
    }

}