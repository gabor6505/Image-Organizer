package net.gabor6505.imageorganizer.components;

import net.gabor6505.imageorganizer.MouseWheelController;
import net.gabor6505.imageorganizer.PreferenceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.security.Key;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyBindConfigWindow extends JFrame implements KeyEventDispatcher, WindowListener {

    private final AtomicBoolean keyBindConfigVisible;

    private JPanel centerPanel;

    public KeyBindConfigWindow(JFrame parentFrame, AtomicBoolean keyBindConfigVisible) {
        this.keyBindConfigVisible = keyBindConfigVisible;
        setTitle("Keybind configuration");
        setMinimumSize(new Dimension(500, 300));
        setSize(500, 700);
        setLocationRelativeTo(parentFrame);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
        addWindowListener(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setupComponents();

        setVisible(true);
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        setContentPane(mainPanel);

        centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(32, 48, 16, 48));

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        new MouseWheelController(scrollPane, 30);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setupKeyBindComponents();

        JPanel southPanel = new JPanel();
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("+");
        addButton.setFont(addButton.getFont().deriveFont(20f));
        addButton.addActionListener(e -> insertRow(-1, ""));
        southPanel.add(addButton);
    }

    private void setupKeyBindComponents() {
        Map<Integer, String> keyBindMap = PreferenceManager.getKeyBindMap();

        for (Integer keyCode : keyBindMap.keySet()) {
            insertRow(keyCode, keyBindMap.get(keyCode));
        }
    }

    private void insertRow(int keyCode, String folderName) {
        JTextField textField = new JTextField(folderName);
        KeyBindButton btn = new KeyBindButton(keyCode, textField);


        btn.setFont(btn.getFont().deriveFont(15f));
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 1;
        c1.weightx = 1;
        c1.fill = GridBagConstraints.BOTH;
        c1.ipady = 4;
        c1.insets = new Insets(0, 0, 8, 6);
        centerPanel.add(btn, c1);

        Timer timer = new Timer(1000, null);
        timer.setRepeats(false);
        timer.addActionListener(e1 -> PreferenceManager.updateKeyBindFolder(btn.getKeyCode(), textField.getText()));

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                if (c == '\\' || c == '/' || c == File.separatorChar) e.consume();
                else {
                    if (!timer.isRunning()) timer.start();
                    else timer.restart();
                }
            }
        });
        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 2;
        c2.weightx = 4;
        c2.fill = GridBagConstraints.BOTH;
        c2.ipady = 4;
        c2.insets = new Insets(0, 6, 8, 6);
        centerPanel.add(textField, c2);


        JButton removeBtn = new JButton("X");
        removeBtn.addActionListener(e -> {
            centerPanel.remove(btn);
            centerPanel.remove(textField);
            centerPanel.remove(removeBtn);
            revalidate();
            // TODO fix gui glitch when removing rows
            if (btn.getKeyCode() != -1) PreferenceManager.removeKeyBind(btn.getKeyCode());
        });
        removeBtn.setFont(btn.getFont().deriveFont(15f));
        removeBtn.setForeground(Color.RED);

        GridBagConstraints c3 = new GridBagConstraints();
        c3.gridx = 3;
        c3.fill = GridBagConstraints.VERTICAL;
        c3.insets = new Insets(0, 6, 8, 0);
        centerPanel.add(removeBtn, c3);

        revalidate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE && isFocused()) {
            dispose();
            keyBindConfigVisible.set(false);
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
            return true;
        }
        return false;
    }

    /*@Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }*/

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        keyBindConfigVisible.set(false);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
