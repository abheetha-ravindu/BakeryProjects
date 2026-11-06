package com.example.bakeryshop.billing;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public  InvoiceService(InvoiceRepository invoiceRepository){
        this.invoiceRepository = invoiceRepository;
    }

    public void generateInvoice(Invoice invoice){
        double total1 = invoice.calculateTotal();
        System.out.println("Generating invoice. Total Amount: "+total1);

        invoiceRepository.save(invoice);
    }

    public Invoice getInvoiceById(Long id){
        return invoiceRepository.findById(id).orElse(null);
    }
    public List<Invoice> getAllInvoices(){
        return invoiceRepository.findAll();
    }
    public void cancelInvoice(Long id){
        invoiceRepository.deleteById(id);
    }
}