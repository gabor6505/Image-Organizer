package net.gabor6505.imageorganizer.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

public class ImageLabel extends JLabel implements ComponentListener {

    private BufferedImage rawImage;

    public ImageLabel() {
        addComponentListener(this);
    }

    public void setImage(BufferedImage rawImage) {
        this.rawImage = rawImage;
        updateIcon();
    }

    private void updateIcon() {
        if (rawImage == null) return;

        int w = rawImage.getWidth();
        int h = rawImage.getHeight();
        float scaleW = (float) getWidth() / w;
        float scaleH = (float) getHeight() / h;
        if (scaleW > 1f) scaleW = 1f;
        if (scaleH > 1f) scaleH = 1f;

        if (scaleW > scaleH) {
            w = -1;
            h = (int) (h * scaleH);
        } else {
            w = (int) (w * scaleW);
            h = -1;
        }
        setIcon(new ImageIcon(rawImage.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
    }

    @Override
    public void componentResized(ComponentEvent e) {
        updateIcon();
    }

    @Override
    public void componentMoved(ComponentEvent e) { }

    @Override
    public void componentShown(ComponentEvent e) { }

    @Override
    public void componentHidden(ComponentEvent e) { }
}
