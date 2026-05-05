package main;

import com.formdev.flatlaf.FlatLightLaf;

import frame.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setupLookAndFeel();
            new LoginFrame().setVisible(true);
        });
    }

    private static void setupLookAndFeel() {
        try {
            FlatLightLaf.setup();

            // Tinh chỉnh UI đẹp hơn
            UIManager.put("Button.arc", 14);
            UIManager.put("Component.arc", 14);
            UIManager.put("TextComponent.arc", 14);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("TabbedPane.arc", 14);
            UIManager.put("TabbedPane.tabArc", 14);
            UIManager.put("Table.rowHeight", 32);

            // Font
            Font f = new Font("Segoe UI", Font.PLAIN, 13);
            var keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object k = keys.nextElement();
                Object v = UIManager.get(k);
                if (v instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(k, new javax.swing.plaf.FontUIResource(f));
                }
            }
        } catch (Exception e) {
            // Nếu thiếu FlatLaf jar vẫn chạy theo LAF mặc định
            e.printStackTrace();
        }
    }
}