package com.example.bakeryshop.billing;

public class OrderInvoice extends Invoice{

    private double orderAmount;
    private double tax;

    public double getOrderAmount(){
        return orderAmount;
    }
    public void setOrderAmount(double orderAmount){
        this.orderAmount = orderAmount;
    }
    public double getTax(){
        return tax;
    }
    public void setTax(double tax){
        this.tax = tax;
    }

    @Override
    public double calculateTotal(){
        return orderAmount+tax;
    }
}