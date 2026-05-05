package entity;

import java.math.BigDecimal;

public class CartItem {
    public Product product;
    public String memberCode;   // mã thành viên (nếu có)
    public String promoCode;    // mã khuyến mãi (nếu có)

    // tuỳ chọn
    public int icePercent;      // 0-100
    public int sugarPercent;    // 0-100
    public int quantity;        // số ly/lon
    public String note;

    public BigDecimal unitPrice;   // giá tại thời điểm order
    public BigDecimal lineTotal;   // unitPrice * quantity (sau giảm giá nếu có)
}