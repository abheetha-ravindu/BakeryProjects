package com.example.bakeryshop.billing;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController

@RequestMapping("/api/invoices")

public class InvoiceController{
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService){
        this.invoiceService = invoiceService;
    }

    @PostMapping
    public String createInvoice(Invoice invoice){
        invoiceService.generateInvoice(invoice);
        return "Invoice generated successfully";
    }

    @GetMapping
    public List<Invoice> viewAllInvoices(){
        return invoiceService.getAllInvoices();
    }

    @DeleteMapping("/{id}")
    public String voidInvoice(@PathVariable Long id){
        invoiceService.cancelInvoice(id);
        return "Invoice status changed to VOID/Cancelled successfully";
    }
}


