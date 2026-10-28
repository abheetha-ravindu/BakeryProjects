package com.example.bakeryshop.billing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUserIdOrderByInvoiceDateDesc(Long userId);

    List<Invoice> findAllByOrderByInvoiceDateDesc();

    Optional<Invoice> findByPaymentId(Long paymentId);
}
