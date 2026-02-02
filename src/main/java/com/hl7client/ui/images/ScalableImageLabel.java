package com.hl7client.ui.images;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class ScalableImageLabel extends JLabel {

    private BufferedImage image;
    private boolean gifMode;

    public void setImage(URL imageUrl) {
        image = null;
        gifMode = false;
        setIcon(null);

        if (imageUrl == null) {
            repaint();
            return;
        }

        String path = imageUrl.getPath().toLowerCase();

        // -------------------------------------------------
        // GIF → delegar a Swing
        // -------------------------------------------------
        if (path.endsWith(".gif")) {
            gifMode = true;
            setIcon(new ImageIcon(imageUrl));
            return;
        }

        // -------------------------------------------------
        // Imagen estática
        // -------------------------------------------------
        try {
            image = ImageIO.read(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gifMode || image == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            int w = getWidth();
            int h = getHeight();

            double imgRatio = (double) image.getWidth() / image.getHeight();
            double cmpRatio = (double) w / h;

            int drawW, drawH;
            if (cmpRatio > imgRatio) {
                drawH = h;
                drawW = (int) (h * imgRatio);
            } else {
                drawW = w;
                drawH = (int) (w / imgRatio);
            }

            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;

            g2.drawImage(image, x, y, drawW, drawH, null);
        } finally {
            g2.dispose();
        }
    }
}
