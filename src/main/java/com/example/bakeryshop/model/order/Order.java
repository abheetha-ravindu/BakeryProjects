package com.example.bakeryshop.model.order;


import com.example.bakeryshop.model.cart.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    private Long userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private String note;

    @Column(length = 1000)
    private String itemsSummary;

    private BigDecimal totalAmount;

    public Order() {
    }

    public Order(Long userId, String note, String itemsSummary, BigDecimal totalAmount) {
        this.userId = userId;
        this.note = note;
        this.itemsSummary = itemsSummary;
        this.totalAmount = totalAmount;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }
    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String describe() {
        return "Order #" + getId() + " (" + status + ") total Rs. " + totalAmount;
    }
}