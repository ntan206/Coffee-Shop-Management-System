package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    public int id;
    public String code;
    public LocalDateTime createdAt;
    public int createdBy;
    public String createdByName;
    public String customerName;
    public String note;
    public BigDecimal totalAmount;
    public String status; // PAID / CANCELLED
}
