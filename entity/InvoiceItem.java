package entity;

import java.math.BigDecimal;

public class InvoiceItem {
    public int id;
    public int invoiceId;
    public int productId;
    public String productCode;
    public String productName;
    public int qty;
    public BigDecimal unitPrice;
    public BigDecimal lineTotal;
}