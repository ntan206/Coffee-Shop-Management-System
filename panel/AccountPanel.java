package panel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import dao.UserDAO;

public class AccountPanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    
    private final JTextField txtUsername = new JTextField();
    private final JPasswordField txtPassword = new JPasswordField();
    private final JTextField txtFullName = new JTextField();
    private final JButton btnCreate = new JButton("Tạo tài khoản");

    public AccountPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form nhập liệu
        JPanel form = new JPanel(new GridLayout(0, 1, 5, 10));
        form.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên mới"));
        
        form.add(new JLabel("Tên đăng nhập:"));
        form.add(txtUsername);
        form.add(new JLabel("Mật khẩu:"));
        form.add(txtPassword);
        form.add(new JLabel("Họ tên đầy đủ:"));
        form.add(txtFullName);
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnWrapper.add(btnCreate);

        add(form, BorderLayout.NORTH);
        add(btnWrapper, BorderLayout.CENTER);

        // Xử lý sự kiện
        btnCreate.addActionListener(e -> onCreateAccount());
    }

    private void onCreateAccount() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();
        String name = txtFullName.getText().trim();

        if (user.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            if (userDAO.isUsernameExists(user)) {
                JOptionPane.showMessageDialog(this, "Tên đăng nhập đã tồn tại!");
                return;
            }

            userDAO.insertEmployee(user, pass, name);
            JOptionPane.showMessageDialog(this, "Tạo tài khoản thành công! Nhân viên có thể đăng nhập ngay.");
            
            // Xóa sạch form sau khi tạo
            txtUsername.setText("");
            txtPassword.setText("");
            txtFullName.setText("");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kết nối database: " + ex.getMessage());
        }
    }
}