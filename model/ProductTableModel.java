package model;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import entity.Product;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProductTableModel extends AbstractTableModel {

    private final String[] cols = {
        "ID", "Ma", "Ten", "Nhom", "Gia", "Hoat dong", "Hinh anh"
    };

    private List<Product> data = new ArrayList<Product>();

    private final NumberFormat money =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    // Cache anh: URL -> ImageIcon da scale (thread-safe)
    private final Map<String, ImageIcon> imageCache =
            new ConcurrentHashMap<String, ImageIcon>();

    // Danh sach URL dang duoc tai, tranh tai trung lap
    private final Set<String> loadingUrls =
            Collections.synchronizedSet(new HashSet<String>());

    // ─────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────

    public void setData(List<Product> list) {
        this.data = list;
        fireTableDataChanged();
        for (int i = 0; i < list.size(); i++) {
            prefetchImage(list.get(i));
        }
    }

    public Product getAt(int row) {
        return data.get(row);
    }

    // ─────────────────────────────────────────────────────────────────────
    // AbstractTableModel
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int col) {
        return cols[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 6) return Icon.class;
        return Object.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Product p = data.get(row);

        if (col == 0) return p.id;
        if (col == 1) return p.code;
        if (col == 2) return p.name;
        if (col == 3) return p.category;
        if (col == 4) return money.format(p.price);
        if (col == 5) return p.active ? "ON" : "OFF";
        if (col == 6) {
            if (p.image == null || p.image.trim().isEmpty()) return null;
            // Chi lay tu cache, KHONG tai o day
            return imageCache.get(p.image);
        }
        return "";
    }

    // ─────────────────────────────────────────────────────────────────────
    // Tai anh bat dong bo — moi URL chay 1 thread rieng
    // ─────────────────────────────────────────────────────────────────────

    private void prefetchImage(final Product p) {
        if (p.image == null || p.image.trim().isEmpty()) return;
        if (imageCache.containsKey(p.image))             return;
        if (loadingUrls.contains(p.image))               return;

        loadingUrls.add(p.image);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(p.image);
                    HttpURLConnection conn =
                            (HttpURLConnection) url.openConnection();

                    // Tang timeout de tranh bi cut sớm
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(15000);

                    // Header day du gia lap browser — can thiet voi postimg
                    conn.setRequestProperty("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36");
                    conn.setRequestProperty("Accept",
                            "image/webp,image/apng,image/*,*/*;q=0.8");
                    conn.setRequestProperty("Accept-Language",
                            "en-US,en;q=0.9");
                    conn.setRequestProperty("Referer",
                            "https://postimg.cc/");

                    // Tu dong follow redirect (301, 302)
                    conn.setInstanceFollowRedirects(true);
                    HttpURLConnection.setFollowRedirects(true);

                    // Kiem tra response code truoc khi doc anh
                    int responseCode = conn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.out.println("[IMG] Loi " + responseCode
                                + " khi tai: " + p.image);
                        imageCache.put(p.image, new ImageIcon());
                        return;
                    }

                    // Doc anh tu stream
                    BufferedImage raw =
                            javax.imageio.ImageIO.read(conn.getInputStream());

                    if (raw != null) {
                        // Scale giu ti le, vua 60x60
                        int tw = 60, th = 60;
                        double scale = Math.min(
                                tw / (double) raw.getWidth(),
                                th / (double) raw.getHeight());
                        int sw = (int)(raw.getWidth()  * scale);
                        int sh = (int)(raw.getHeight() * scale);
                        Image scaled = raw.getScaledInstance(
                                sw, sh, Image.SCALE_SMOOTH);
                        imageCache.put(p.image, new ImageIcon(scaled));
                        System.out.println("[IMG] OK: " + p.code);
                    } else {
                        System.out.println("[IMG] Khong doc duoc anh: "
                                + p.image);
                        imageCache.put(p.image, new ImageIcon());
                    }

                } catch (Exception ex) {
                    // In loi ra Console de debug
                    System.out.println("[IMG] Exception khi tai "
                            + p.code + ": " + ex.getMessage());
                    imageCache.put(p.image, new ImageIcon());

                } finally {
                    loadingUrls.remove(p.image);
                    // Repaint bang tren EDT
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            fireTableDataChanged();
                        }
                    });
                }
            }
        });

        thread.setDaemon(true);
        thread.setName("img-" + p.code);
        thread.start();
    }
}