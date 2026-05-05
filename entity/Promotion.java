package entity;

import java.time.LocalDateTime;

// Khuyến mãi
public class Promotion {
	public int id;
    public String code;
    public String name;
    public int discountPercent; // Giảm bao nhiêu %
    public LocalDateTime startDate;
    public LocalDateTime endDate;
	public double getDiscountPercent() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
