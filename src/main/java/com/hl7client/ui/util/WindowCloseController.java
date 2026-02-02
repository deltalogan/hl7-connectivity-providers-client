package com.hl7client.ui.util;

import com.hl7client.Application;
import com.hl7client.ApplicationCloseIntent;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class WindowCloseController {

    public static void install(
            JFrame frame,
            Application application,
            ApplicationCloseIntent intent
    ) {
        Objects.requireNonNull(frame);
        Objects.requireNonNull(application);
        Objects.requireNonNull(intent);

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                application.requestClose(intent);
            }
        });
    }
}
