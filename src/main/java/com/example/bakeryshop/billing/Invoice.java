package com.example.bakeryshop.billing;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "invoice_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Invoice{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true,nullable = false)
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public String getInvoiceNumber(){
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber){
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate(){
        return invoiceDate;
    }
    public void setInvoiceDate(LocalDate invoiceDate){
        this.invoiceDate = invoiceDate;
    }
    public abstract double calculateTotal();
}