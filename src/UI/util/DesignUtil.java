package UI.util;

import javax.swing.*;
import java.awt.*;


public class DesignUtil {
    public static void showErrorMsg(String msg, String title, JFrame frame) {
        JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showInformationMsg(String msg, String title, JFrame frame) {
        JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarningMsg(String msg, String title, JFrame frame) {
        JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.WARNING_MESSAGE);
    }



    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("menlo", Font.BOLD, 17));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        return button;
    }

    public static class Theme {
        public static Color ThemeColor = new Color(0.5f, 0.0f, 0.0f);
    }
}
