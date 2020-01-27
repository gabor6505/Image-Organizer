package net.gabor6505.imageorganizer.components;

import net.gabor6505.imageorganizer.PreferenceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class KeyBindButton extends JButton implements MouseListener, KeyListener, FocusListener {

    private final static List<Integer> reservedKeys = Arrays.asList(KeyEvent.VK_LEFT, KeyEvent.VK_KP_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_KP_RIGHT, KeyEvent.VK_ESCAPE);

    private int keyCode;
    private final JTextField folderNameTextField;

    private boolean editingMode = false;
    private boolean focused = false;

    public KeyBindButton(int keyCode, JTextField correspondingTextField) {
        this.keyCode = keyCode;
        this.folderNameTextField = correspondingTextField;
        addMouseListener(this);
        addKeyListener(this);
        addFocusListener(this);
        setText(getKeyText(keyCode));
    }

    private void setState(boolean editing) {
        editingMode = editing;
        if (editing) {
            setText("> " + getKeyText(keyCode) + " <");
            setForeground(new Color(0, 148, 14));
        } else {
            setText(getKeyText(keyCode));
            setForeground(Color.BLACK);
        }
    }

    // TODO add support for non us layout keys (like á, é, ó)
    private String getKeyText(int keyCode) {
        String text = KeyEvent.getKeyText(keyCode);
        if (text.toLowerCase().contains("unknown")) return "Unknown key";
        if (keyCode == -1) return "UNSET";
        else return text;
    }

    public int getKeyCode() {
        return keyCode;
    }

    private boolean validateKeyCode(int code) {
        if (code <= 0) return false;
        if (reservedKeys.contains(code)) return false;

        for (Integer keyCode : PreferenceManager.getKeyBindMap().getKeyCodes()) {
            if (code == keyCode) return false;
        }

        return true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!editingMode) setState(true);
    }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void focusGained(FocusEvent e) {
        focused = true;
    }

    @Override
    public void focusLost(FocusEvent e) {
        focused = false;
        if (editingMode) setState(false);
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        if (focused && editingMode) {
            int newKeyCode = e.getKeyCode();
            if (keyCode == newKeyCode) {
                setState(false);
                return;
            }
            if (!validateKeyCode(newKeyCode)) return;

            if (keyCode == -1) {
                PreferenceManager.addKeyBind(newKeyCode, folderNameTextField.getText());
            } else {
                PreferenceManager.updateKeyBindCode(keyCode, newKeyCode);
            }
            this.keyCode = newKeyCode;
            setState(false);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }
}
