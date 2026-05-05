package entity;
import java.math.BigDecimal;

public class Product {
    public int id;
    public String code;
    public String name;
    public String category;
    public BigDecimal price;
    public boolean active;
 // Thêm dòng này để lưu link ảnh từ PostImages
    public String image;
    @Override
    public String toString() {
        return code + " - " + name + " (" + price + ")";
    }
}