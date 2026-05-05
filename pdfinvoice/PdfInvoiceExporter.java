package pdfinvoice;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import entity.CartItem;
import entity.Invoice;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Xuất hóa đơn PDF theo chuẩn hóa đơn quán cà phê thực tế.
 * Layout: khổ giấy nhiệt 80mm (226 pt rộng), chiều cao tự động.
 *
 * Thông tin hiển thị:
 *  - Logo text + tên quán + địa chỉ + SĐT / WiFi
 *  - Mã hóa đơn, nhân viên, thời gian
 *  - Bảng món: tên, số lượng, đơn giá, thành tiền
 *  - Tổng gốc, giảm giá (thành viên + promo), thành tiền
 *  - Thông tin thành viên (nếu có)
 *  - Phương thức thanh toán
 *  - Lời cảm ơn + chính sách
 */
public class PdfInvoiceExporter {

    // ── Thông tin quán (chỉnh tại đây) ─────────────────────────────────────
    private static final String SHOP_NAME    = "SG CITY COFFEE SHOP";
    private static final String SHOP_TAGLINE = "Cà Phê & Free Fire & Liên Quân & Minecraft";
    private static final String SHOP_ADDR    = "Phường An Nhơn Nam, Tỉnh Gia Lai";
    private static final String SHOP_PHONE   = "ĐT: 079 113 256";
    private static final String SHOP_WIFI    = "WiFi: SG City | MK: khongaihoi";
    private static final String SHOP_THANKS  = "Cảm ơn quý khách đã ghé thăm!";
    private static final String SHOP_POLICY  = "Hóa đơn xuất trong vòng 24h kể từ\n"
                                             + "thời điểm thanh toán. Không đổi trả\n"
                                             + "sau khi đã rời quán.";
    private static final String SHOP_FB      = "fb.com/sgcity.2026";
    private static final String SHOP_IG      = "instagram.com/sgcity.vn";

    // ── Khổ giấy nhiệt 80mm ─────────────────────────────────────────────────
    private static final float PAGE_W    = 226f;   // 80mm ≈ 226pt
    private static final float MARGIN    = 12f;
    private static final float CONTENT_W = PAGE_W - MARGIN * 2;

    // ── Font paths ───────────────────────────────────────────────────────────
    private static final String[] FONT_REGULAR = {
        "C:/Windows/Fonts/arial.ttf",
        "C:/Windows/Fonts/arialuni.ttf",
        "C:/Windows/Fonts/tahoma.ttf",
        "C:/Windows/Fonts/segoeui.ttf",
        "C:/Windows/Fonts/calibri.ttf",
        "/Library/Fonts/Arial.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
    };
    private static final String[] FONT_BOLD = {
        "C:/Windows/Fonts/arialbd.ttf",
        "C:/Windows/Fonts/tahomabd.ttf",
        "C:/Windows/Fonts/segoeuib.ttf",
        "C:/Windows/Fonts/calibrib.ttf",
        "/Library/Fonts/Arial Bold.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf",
    };

    // ════════════════════════════════════════════════════════════════════════
    // PUBLIC API
    // ════════════════════════════════════════════════════════════════════════

    public static File exportReceiptPdf(String filePath, Invoice inv, List<CartItem> items)
            throws Exception {

        File out = new File(filePath);
        if (out.getParentFile() != null) out.getParentFile().mkdirs();

        // ── Tính chiều cao trang động ────────────────────────────────────────
        float pageH = estimateHeight(items, inv);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(new PDRectangle(PAGE_W, pageH));
            doc.addPage(page);

            PDType0Font regular = loadFont(doc, FONT_REGULAR);
            PDType0Font bold    = loadFont(doc, FONT_BOLD);
            if (bold    == null) bold    = regular;
            if (regular == null)
                throw new RuntimeException(
                    "Không tìm thấy font TTF hỗ trợ tiếng Việt.\n" +
                    "Vui lòng cài Arial hoặc DejaVu Sans.");

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                Pen pen = new Pen(cs, regular, bold, pageH);

                drawHeader(pen, inv);
                drawDivider(pen, '-');
                drawInvoiceMeta(pen, inv);
                drawDivider(pen, '-');
                drawItemsHeader(pen);
                drawDivider(pen, ' ');
                drawItems(pen, items);
                drawDivider(pen, '-');
                drawTotals(pen, inv, items);
                drawDivider(pen, '=');
                drawMemberPromo(pen, inv);
                drawPaymentMethod(pen, inv);
                drawDivider(pen, '-');
                drawFooter(pen);
            }

            doc.save(out);
        }

        return out;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SECTIONS
    // ════════════════════════════════════════════════════════════════════════

    /** ── Logo + tên quán + địa chỉ ── */
    private static void drawHeader(Pen pen, Invoice inv) throws Exception {
        pen.centerBold(SHOP_NAME, 15);
        pen.nl(3);
        pen.centerRegular(SHOP_TAGLINE, 9);
        pen.nl(5);
        pen.centerRegular(SHOP_ADDR, 8);
        pen.nl(2);
        pen.centerRegular(SHOP_PHONE, 8);
        pen.nl(2);
        pen.centerRegular(SHOP_WIFI, 7.5f);
        pen.nl(8);
    }

    /** ── Mã HĐ / nhân viên / thời gian ── */
    private static void drawInvoiceMeta(Pen pen, Invoice inv) throws Exception {
        pen.nl(4);
        pen.leftBold("HOA DON BAN HANG", 9);
        pen.nl(5);

        String time = inv.createdAt != null
                ? inv.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss"))
                : "";

        pen.twoCol("Ma HD    :", safe(inv.code),            8);
        pen.twoCol("Thoi gian:", time,                      8);
        pen.twoCol("Nhan vien:", safe(inv.createdByName),   8);
        if (notBlank(inv.customerName))
            pen.twoCol("Khach hang:", safe(inv.customerName), 8);
        pen.nl(4);
    }

    /** ── Header bảng món ── */
    private static void drawItemsHeader(Pen pen) throws Exception {
        pen.nl(3);
        // Tên | SL | Đơn giá | T.Tiền
        pen.itemRow("Ten mon", "SL", "Don gia", "T.Tien", true);
        pen.nl(2);
    }

    /** ── Từng dòng món ── */
    private static void drawItems(Pen pen, List<CartItem> items) throws Exception {
        for (CartItem it : items) {
            String name = safe(it.product != null ? it.product.name : "");
            String qty  = String.valueOf(it.quantity);
            String unit = it.unitPrice  != null ? fmtNum(it.unitPrice.toPlainString())  : "-";
            String line = it.lineTotal  != null ? fmtNum(it.lineTotal.toPlainString())  : "-";

            pen.itemRow(name, qty, unit, line, false);

            // Tùy chọn: đá / đường / ghi chú
            StringBuilder opts = new StringBuilder();
            if (it.icePercent != 50)   opts.append("Da ").append(it.icePercent).append("%  ");
            if (it.sugarPercent != 50) opts.append("Duong ").append(it.sugarPercent).append("%");
            if (opts.length() > 0) {
                pen.nl(2);
                pen.indentRegular("  > " + opts.toString().trim(), 7.5f);
            }
            if (notBlank(it.note)) {
                pen.nl(2);
                pen.indentRegular("  Ghi chu: " + it.note, 7.5f);
            }
            pen.nl(3);
        }
    }

    /** ── Tổng tiền ── */
    private static void drawTotals(Pen pen, Invoice inv, List<CartItem> items) throws Exception {
        pen.nl(4);

        // Tổng gốc
        java.math.BigDecimal rawTotal = java.math.BigDecimal.ZERO;
        for (CartItem it : items)
            if (it.unitPrice != null)
                rawTotal = rawTotal.add(it.unitPrice.multiply(java.math.BigDecimal.valueOf(it.quantity)));

        java.math.BigDecimal finalTotal = inv.totalAmount != null
                ? inv.totalAmount : rawTotal;
        java.math.BigDecimal discount = rawTotal.subtract(finalTotal);
        if (discount.compareTo(java.math.BigDecimal.ZERO) < 0)
            discount = java.math.BigDecimal.ZERO;

        pen.twoCol("Tong cong  :", fmtNum(rawTotal.toPlainString()) + " VND", 8.5f);

        if (discount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            pen.twoCol("Giam gia   :", "-" + fmtNum(discount.toPlainString()) + " VND", 8.5f);
        }

        pen.nl(3);
        pen.twoColBold("THANH TIEN :", fmtNum(finalTotal.toPlainString()) + " VND", 11f);
        pen.nl(4);
    }

    /** ── Thông tin thành viên + promo (nếu có) ── */
    private static void drawMemberPromo(Pen pen, Invoice inv) throws Exception {
        // Phân tích note để lấy PROMO và PAY
        String note      = safe(inv.note);
        String promoCode = extractField(note, "PROMO=");
        String payMethod = extractField(note, "PAY=");

        boolean hasMember = notBlank(inv.customerName);
        boolean hasPromo  = notBlank(promoCode);

        if (!hasMember && !hasPromo) return;

        pen.nl(3);
        if (hasMember) {
            pen.twoCol("Thanh vien :", safe(inv.customerName), 8);
        }
        if (hasPromo) {
            pen.twoCol("Ma KM      :", promoCode, 8);
        }
        pen.nl(3);
    }

    /** ── Phương thức thanh toán ── */
    private static void drawPaymentMethod(Pen pen, Invoice inv) throws Exception {
        String note      = safe(inv.note);
        String payMethod = extractField(note, "PAY=");
        String display   = "QR".equalsIgnoreCase(payMethod) ? "Chuyen khoan / QR Pay"
                         : "CASH".equalsIgnoreCase(payMethod) ? "Tien mat"
                         : notBlank(payMethod) ? payMethod : "---";
        pen.nl(2);
        pen.twoCol("Thanh toan :", display, 8);
        pen.nl(6);
    }

    /** ── Footer: cảm ơn + mạng xã hội + chính sách ── */
    private static void drawFooter(Pen pen) throws Exception {
        pen.nl(4);
        pen.centerBold(SHOP_THANKS, 10);
        pen.nl(6);
        pen.centerRegular(SHOP_FB, 7.5f);
        pen.nl(2);
        pen.centerRegular(SHOP_IG, 7.5f);
        pen.nl(10);
        pen.centerRegular("* * *", 8);
        pen.nl(6);
        // Chính sách (nhiều dòng)
        for (String line : SHOP_POLICY.split("\n")) {
            pen.centerRegular(line.trim(), 7f);
            pen.nl(2);
        }
        pen.nl(10);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Pen — wrapper vẽ text lên PDPageContentStream
    // ════════════════════════════════════════════════════════════════════════

    private static class Pen {
        final PDPageContentStream cs;
        final PDType0Font regular, bold;
        float y;        // con trỏ Y hiện tại (từ top xuống → convert khi draw)
        final float pageH;

        Pen(PDPageContentStream cs, PDType0Font regular, PDType0Font bold, float pageH) {
            this.cs      = cs;
            this.regular = regular;
            this.bold    = bold;
            this.pageH   = pageH;
            this.y       = 8f;  // bắt đầu từ sát đỉnh
        }

        /** Y quy đổi: PDF gốc từ bottom → top */
        float ry() { return pageH - y; }

        void nl(float pt) { y += pt; }

        // ── Vẽ text canh trái ──────────────────────────────────────────────
        void leftRegular(String text, float size) throws Exception {
            if (text == null || text.isEmpty()) return;
            cs.beginText();
            cs.setFont(regular, size);
            cs.newLineAtOffset(MARGIN, ry());
            cs.showText(text);
            cs.endText();
            y += size + 1;
        }

        void leftBold(String text, float size) throws Exception {
            if (text == null || text.isEmpty()) return;
            cs.beginText();
            cs.setFont(bold, size);
            cs.newLineAtOffset(MARGIN, ry());
            cs.showText(text);
            cs.endText();
            y += size + 1;
        }

        void indentRegular(String text, float size) throws Exception {
            if (text == null || text.isEmpty()) return;
            cs.beginText();
            cs.setFont(regular, size);
            cs.newLineAtOffset(MARGIN + 4, ry());
            cs.showText(text);
            cs.endText();
            y += size + 1;
        }

        // ── Vẽ text căn giữa ──────────────────────────────────────────────
        void centerRegular(String text, float size) throws Exception {
            drawCentered(text, size, regular);
        }

        void centerBold(String text, float size) throws Exception {
            drawCentered(text, size, bold);
        }

        private void drawCentered(String text, float size, PDType0Font font) throws Exception {
            if (text == null || text.isEmpty()) return;
            float tw = font.getStringWidth(text) / 1000f * size;
            float tx = (PAGE_W - tw) / 2f;
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(tx, ry());
            cs.showText(text);
            cs.endText();
            y += size + 1;
        }

        // ── 2 cột: label bên trái, value bên phải ─────────────────────────
        void twoCol(String label, String value, float size) throws Exception {
            cs.beginText();
            cs.setFont(regular, size);
            cs.newLineAtOffset(MARGIN, ry());
            cs.showText(label);
            cs.endText();

            float vw = regular.getStringWidth(value) / 1000f * size;
            cs.beginText();
            cs.setFont(regular, size);
            cs.newLineAtOffset(PAGE_W - MARGIN - vw, ry());
            cs.showText(value);
            cs.endText();

            y += size + 3;
        }

        void twoColBold(String label, String value, float size) throws Exception {
            cs.beginText();
            cs.setFont(bold, size);
            cs.newLineAtOffset(MARGIN, ry());
            cs.showText(label);
            cs.endText();

            float vw = bold.getStringWidth(value) / 1000f * size;
            cs.beginText();
            cs.setFont(bold, size);
            cs.newLineAtOffset(PAGE_W - MARGIN - vw, ry());
            cs.showText(value);
            cs.endText();

            y += size + 3;
        }

        // ── Hàng bảng món: Tên | SL | ĐG | TT ───────────────────────────
        // Layout cột: tên chiếm ~50%, SL 10%, ĐG 20%, TT 20%
        void itemRow(String name, String qty, String unitPrice, String lineTotal, boolean isHeader)
                throws Exception {
            float size   = isHeader ? 8f : 8f;
            PDType0Font f = isHeader ? bold : regular;

            float colName = MARGIN;
            float colQty  = MARGIN + CONTENT_W * 0.52f;
            float colUnit = MARGIN + CONTENT_W * 0.63f;
            // thành tiền: canh phải
            float ltW  = f.getStringWidth(lineTotal) / 1000f * size;
            float colLT = PAGE_W - MARGIN - ltW;

            // Tên món: cắt nếu quá dài
            String displayName = truncatePx(name, CONTENT_W * 0.50f, f, size);

            cs.beginText(); cs.setFont(f, size);
            cs.newLineAtOffset(colName, ry()); cs.showText(displayName); cs.endText();

            cs.beginText(); cs.setFont(f, size);
            cs.newLineAtOffset(colQty,  ry()); cs.showText(qty);          cs.endText();

            cs.beginText(); cs.setFont(f, size);
            cs.newLineAtOffset(colUnit, ry()); cs.showText(unitPrice);    cs.endText();

            cs.beginText(); cs.setFont(f, size);
            cs.newLineAtOffset(colLT,   ry()); cs.showText(lineTotal);    cs.endText();

            y += size + 3;
        }

        // ── Đường kẻ ─────────────────────────────────────────────────────
        void drawDivider(PDPageContentStream cs, char ch) throws Exception {
            int repeat = ch == '=' ? 32 : ch == '-' ? 32 : 0;
            if (repeat == 0) { nl(4); return; }
            String line = String.valueOf(ch).repeat(repeat);
            cs.beginText();
            cs.setFont(regular, 8);
            cs.newLineAtOffset(MARGIN, ry());
            cs.showText(line);
            cs.endText();
            y += 10;
        }
    }

    // ── Wrapper gọi pen.drawDivider ───────────────────────────────────────
    private static void drawDivider(Pen pen, char ch) throws Exception {
        pen.drawDivider(pen.cs, ch);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Utility
    // ════════════════════════════════════════════════════════════════════════

    /** Ước lượng chiều cao trang để tránh overflow */
    private static float estimateHeight(List<CartItem> items, Invoice inv) {
        float h = 320f;                      // header + meta + footer cố định
        h += items.size() * 32f;            // mỗi món ~32pt
        for (CartItem it : items) {
            if (it.icePercent != 50 || it.sugarPercent != 50) h += 14f;
            if (notBlank(it.note)) h += 14f;
        }
        if (notBlank(inv.customerName)) h += 14f;
        String note = safe(inv.note);
        if (notBlank(extractField(note, "PROMO="))) h += 14f;
        return h;
    }

    private static PDType0Font loadFont(PDDocument doc, String[] candidates) {
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists() && f.canRead()) {
                try { return PDType0Font.load(doc, f); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /** Trích giá trị từ chuỗi note dạng "KEY=VALUE KEY2=VALUE2" */
    private static String extractField(String note, String key) {
        if (note == null || key == null) return "";
        int idx = note.indexOf(key);
        if (idx < 0) return "";
        String rest = note.substring(idx + key.length());
        int space = rest.indexOf(' ');
        return space < 0 ? rest.trim() : rest.substring(0, space).trim();
    }

    /** Format số: thêm dấu chấm phân cách nghìn */
    private static String fmtNum(String plain) {
        if (plain == null || plain.isEmpty()) return "";
        try {
            long v = new java.math.BigDecimal(plain).longValueExact();
            return String.format("%,d", v).replace(',', '.');
        } catch (Exception e) {
            return plain;
        }
    }

    /** Cắt chuỗi sao cho không vượt quá maxWidth pixels với font/size đã cho */
    private static String truncatePx(String s, float maxW, PDType0Font font, float size)
            throws Exception {
        if (s == null) return "";
        while (!s.isEmpty()) {
            float w = font.getStringWidth(s) / 1000f * size;
            if (w <= maxW) return s;
            s = s.substring(0, s.length() - 1);
        }
        return "";
    }

    private static String safe(String s)        { return s == null ? "" : s; }
    private static boolean notBlank(String s)   { return s != null && !s.trim().isEmpty(); }
}