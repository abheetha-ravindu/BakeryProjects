package com.example.bakeryshop.services;

import com.example.bakeryshop.billing.Invoice;
import com.example.bakeryshop.model.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String senderEmail;

    public EmailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username:}") String senderEmail) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
    }

    public void sendOrderConfirmation(String toEmail, Order order) {
        String subject = "Order confirmation #" + order.getId();
        String body = """
                Thank you for your order!

                Order: #%s
                Items: %s
                Total: Rs. %s
                Status: %s
                """.formatted(order.getId(), order.getItemsSummary(), order.getTotalAmount(), order.getStatus());
        sendAfterCommit(toEmail, subject, body);
    }

    public void sendInvoice(String toEmail, Invoice invoice) {
        String subject = "Invoice " + invoice.getInvoiceNumber();
        String body = """
                Thank you for your payment.

                Invoice: %s
                Date: %s
                Order: #%s
                Payment type: %s
                Amount: Rs. %s
                """.formatted(invoice.getInvoiceNumber(), invoice.getInvoiceDate(), invoice.getOrderId(),
                invoice.getPaymentType(), invoice.getAmount());
        sendAfterCommit(toEmail, subject, body);
    }

    private void sendAfterCommit(String toEmail, String subject, String body) {
        Runnable send = () -> send(toEmail, subject, body);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }

    private void send(String toEmail, String subject, String body) {
        if (senderEmail.isBlank()) {
            logger.warn("Email notification to {} was skipped because MAIL_USERNAME is not configured.", toEmail);
            return;
        }
        if (toEmail == null || toEmail.isBlank()) {
            logger.warn("Email notification was skipped because the customer has no email address.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.error("Could not send email notification to {}.", toEmail, exception);
        }
    }
}
