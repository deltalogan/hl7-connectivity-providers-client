package com.hl7client.ui.dialogs;

import java.awt.*;
import java.net.URL;
import javax.swing.*;

public class SplashDialog extends JDialog {

    public SplashDialog(Window owner, URL gifUrl) {
        super(owner, ModalityType.APPLICATION_MODAL);
        // Configuración extra sin romper JFormDesigner
        setUndecorated(true);
        setResizable(false);
        setFocusableWindowState(false);
        initComponents();

        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gifLabel.setVerticalAlignment(SwingConstants.CENTER);

        // Dejar que Swing anime el GIF
        gifLabel.setIcon(new ImageIcon(gifUrl));

        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
        gifLabel = new JLabel();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridBagLayout());
        ((GridBagLayout)contentPane.getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)contentPane.getLayout()).rowHeights = new int[] {0, 0};
        ((GridBagLayout)contentPane.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
        ((GridBagLayout)contentPane.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
        contentPane.add(gifLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    // Generated using JFormDesigner Evaluation license - margarita85_362@lazer.lat
    private JLabel gifLabel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
