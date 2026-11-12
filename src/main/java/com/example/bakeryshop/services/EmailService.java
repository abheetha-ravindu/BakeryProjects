package com.example.bakeryshop.services;

import com.example.bakeryshop.billing.Invoice;
import com.example.bakeryshop.model.CustomCakeBooking;
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
// Provides Email service logic.
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String senderEmail;

    public EmailService(JavaMailSender mailSender,
                       @Value("${spring.mail.username:}") String senderEmail) {
        this.mailSender = mailSender;
        this.senderEmail = senderEmail;
    }
    public void sendWelcomeEmail(String toEmail, String username) {
        String subject = "Welcome to Bakery Shop!";
        String body = """
                Dear %s,

                Welcome to Bakery Shop! We are delighted to have you as a customer.

                Enjoy our delicious baked goods and have a great experience!

                Best regards,
                Bakery Shop Team
                """.formatted(username);
        sendAfterCommit(toEmail, subject, body);
    }
    public void emailOTP(String toEmail,String Username){
        String subject = "Do not share this OTP";
        String body = """
                Dear %s,

                Your OTP is 123456. Please do not share this with anyone.

                Best regards,
                Bakery Shop Team
                """.formatted(Username);
        sendAfterCommit(toEmail, subject, body);
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

    public void sendCustomCakeBookingConfirmation(CustomCakeBooking booking) {
        String subject = "Custom cake booking confirmation #" + booking.getBookingId();
        String body = """
                Dear %s,

                Thank you for your custom cake order. We have received your booking and will contact you to confirm the final details.

                Booking: #%s
                Occasion: %s
                Cake: %s %s
                Decoration: %s
                Filling: %s
                Size: %s
                Required date: %s
                Preferred time: %s
                Collection or delivery: %s
                Delivery address: %s
                Cake message: %s
                Estimated price: Rs. %s
                Status: %s

                Best regards,
                Bakery Shop Team
                """.formatted(
                valueOr(booking.getCustomerName(), "Customer"),
                booking.getBookingId(),
                valueOr(booking.getOccasion(), "Not specified"),
                valueOr(booking.getShape(), "Custom"),
                valueOr(booking.getFlavor(), "cake"),
                valueOr(booking.getDecoration(), "Not specified"),
                valueOr(booking.getFilling(), "Not specified"),
                valueOr(booking.getCakeSize(), "Not specified"),
                valueOr(booking.getRequiredDate(), "Not specified"),
                valueOr(booking.getPreferredTime(), "Not specified"),
                valueOr(booking.getPickupOrDelivery(), "Not specified"),
                valueOr(booking.getDeliveryAddress(), "Not applicable"),
                valueOr(booking.getCakeMessage(), "None"),
                booking.getEstimatedPrice(),
                booking.getStatus());
        sendAfterCommit(booking.getCustomerEmail(), subject, body);
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

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
