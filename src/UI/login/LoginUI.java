package UI.login;


import UI.util.DesignUtil;
import client.controller.UserController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class LoginUI implements KeyListener {
    private final JFrame frame;
    private JTextField idField;
    private JPasswordField passwordField;
    private final UserController userController;
    private boolean login = false;

    public static String id;

    static String iconURL = "/UI/assets/chat_icon.png";

    public LoginUI(UserController userController) {
        frame = new JFrame("ChatSystem");
        frame.setBackground(DesignUtil.Theme.ThemeColor);

        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();

        frame.setBounds((d.width - d.width / 3) / 2, (d.height - d.height / 3) / 2, 510, 380);
        frame.setIconImage(new ImageIcon(LoginUI.class.getResource(iconURL)).getImage());
        frame.setResizable(false);

        JPanel northPanel = creatNorth();
        JPanel westPanel = creatWest();
        JPanel centerPanel = creatCenter();
        JPanel southPanel = creatSouth();
        JPanel eastPanel = creatEast();

        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(westPanel, BorderLayout.WEST);
        frame.add(southPanel, BorderLayout.SOUTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(eastPanel, BorderLayout.EAST);
        frame.setVisible(true);

        this.userController = userController;
        this.frame.addWindowListener(new CloseWindow());
    }

    public JPanel creatNorth() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(null);
        jp.setPreferredSize(new Dimension(0, 190));

        ImageIcon in = new ImageIcon(LoginUI.class.getResource(iconURL));
        JLabel cc = new JLabel(in);
        cc.setBounds(0, 0, 500, 190);
        cc.setOpaque(false);
        jp.add(cc);

        return jp;
    }

    public JPanel creatWest() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(null);
        jp.setPreferredSize(new Dimension(140, 0));

        JLabel IDLabel = new JLabel("ID:");
        IDLabel.setBounds(120, 10, 100, 30);
        IDLabel.setFont(new Font("menlo", Font.BOLD, 15));
        jp.add(IDLabel);

        JLabel passwordLabel = new JLabel("password:");
        passwordLabel.setBounds(61, 42, 210, 30);
        passwordLabel.setFont(new Font("menlo", Font.BOLD, 15));
        jp.add(passwordLabel);

        return jp;
    }

    public JPanel creatCenter() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(null);
        jp.setPreferredSize(new Dimension(0, 220));

        idField = new JTextField(10);
        idField.setBounds(10, 10, 200, 30);
        idField.setFont(new Font("menlo", Font.BOLD, 17));
        idField.addFocusListener(new JTextFieldHandler(idField, "ID"));
        idField.setOpaque(false);
        idField.addKeyListener(this);
        jp.add(idField);

        passwordField = new JPasswordField(18);
        passwordField.setBounds(10, 42, 200, 30);
        passwordField.setFont(new Font("menlo", Font.BOLD, 17));
        passwordField.addFocusListener(new JPasswordFieldHandler(passwordField, "password"));
        passwordField.setOpaque(false);
        passwordField.addKeyListener(this);
        jp.add(passwordField);

        return jp;
    }

    public JPanel creatSouth() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(null);
        jp.setPreferredSize(new Dimension(0, 40));

        JButton loginButton = DesignUtil.createButton("Login");
        loginButton.setBounds(180, 0, 140, 30);
        loginButton.addActionListener(new LoginHandler(this));
        loginButton.setMargin(new Insets(0,0,0,0));
        jp.add(loginButton);

        return jp;
    }

    public JPanel creatEast() {
        JPanel jp = new JPanel();
        jp.setBackground(new Color(255, 255, 255));
        jp.setLayout(null);
        jp.setPreferredSize(new Dimension(130, 0));

        return jp;
    }

    public boolean checkInput() {
        return !"".equals(idField.getText()) && !"".equals(String.valueOf(passwordField.getPassword()));
    }

    class LoginHandler implements ActionListener {
        LoginUI uiFrame;

        public LoginHandler(LoginUI uiFrame) {
            super();
            this.uiFrame = uiFrame;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (uiFrame.checkInput()) {
                try {
                    if (uiFrame.userController.login(idField.getText(), String.valueOf(passwordField.getPassword()))) {
                        uiFrame.login = true;
                        id = idField.getText();
                        uiFrame.dispose();
                    }
                } catch (Exception e1) {
                    DesignUtil.showErrorMsg(e1.getMessage(), "error", frame);
                    e1.printStackTrace();
                }
            }
        }
    }

    static class JTextFieldHandler implements FocusListener {
        private final String s;
        private final JTextField text;

        public JTextFieldHandler(JTextField text, String s) {
            this.text = text;
            this.s = s;
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (text.getText().equals(s)) {
                text.setText("");
                text.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if ("".equals(text.getText())) {
                text.setForeground(Color.gray);
                text.setText(s);
            }
        }
    }

    static class JPasswordFieldHandler implements FocusListener {
        private final String s;
        private final JPasswordField text;

        public JPasswordFieldHandler(JPasswordField text, String s) {
            this.text = text;
            this.s = s;
        }

        @Override
        public void focusGained(FocusEvent e) {
            if (String.valueOf(text.getPassword()).equals(s)) {
                text.setText("");
                text.setEchoChar('*');
                text.setForeground(Color.BLACK);
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if ("".equals(String.valueOf(text.getPassword()))) {
                text.setEchoChar((char) (0));
                text.setForeground(Color.gray);
                text.setText(s);
            }
        }
    }

    class CloseWindow extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            dispose();
        }
    }

    public void dispose() {
        if (!login) {
            this.userController.exit();
        }
        this.frame.dispose();
    }

    int keyCode = 0;

    @Override
    public void keyPressed(KeyEvent event) {
        keyCode = event.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if ("ENTER".equals(KeyEvent.getKeyText(event.getKeyCode())) && "ENTER".equals(KeyEvent.getKeyText(keyCode)) || event.getKeyCode() == 10 && keyCode == 10) {
            if (this.checkInput()) {
                try {
                    if (this.userController.login(idField.getText(), String.valueOf(passwordField.getPassword()))) {
                        login = true;
                        dispose();
                    }
                } catch (Exception e) {
                    DesignUtil.showErrorMsg(e.getMessage(), "Error", frame);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }
}
