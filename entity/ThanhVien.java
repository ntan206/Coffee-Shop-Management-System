package entity;

public class ThanhVien {
    public int id;
    public String maThanhVien;
    public String tenThanhVien;
    public String loaiThanhVien; // "BAC" | "VANG" | "BACH_KIM"
    public boolean active;

    public String displayType() {
        if (loaiThanhVien == null) return "";
        return switch (loaiThanhVien) {
            case "BAC" -> "Bạc";
            case "VANG" -> "Vàng";
            case "BACH_KIM" -> "Bạch Kim";
            default -> loaiThanhVien;
        };
    }
}