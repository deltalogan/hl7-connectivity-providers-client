package com.hl7client.ui.util;

import com.hl7client.controller.SplashController;
import com.hl7client.model.result.Hl7Error;
import com.hl7client.model.result.Hl7ErrorOrigin;
import com.hl7client.model.result.Hl7Result;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AcceptAction<T> extends AbstractAction {

    private final Window owner;
    private final URL splashResource;
    private final Supplier<Hl7Result<T>> task;
    private final Consumer<Hl7Result<T>> onResult;

    public AcceptAction(
            String name,
            Window owner,
            URL splashResource,
            Supplier<Hl7Result<T>> task,
            Consumer<Hl7Result<T>> onResult
    ) {
        super(name);
        this.owner = Objects.requireNonNull(owner);
        this.splashResource = splashResource;
        this.task = Objects.requireNonNull(task);
        this.onResult = Objects.requireNonNull(onResult);
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {

        setEnabled(false);

        SplashController splash = new SplashController(owner);
        splash.show(splashResource);

        SwingWorker<Hl7Result<T>, Void> worker = new SwingWorker<>() {

            @Override
            protected Hl7Result<T> doInBackground() {
                try {
                    return task.get();
                } catch (Exception ex) {
                    return Hl7Result.error(
                            Hl7Error.technical(
                                    ex.getMessage() != null
                                            ? ex.getMessage()
                                            : "Error técnico inesperado",
                                    Hl7ErrorOrigin.TRANSPORTE
                            )
                    );
                }
            }

            @Override
            protected void done() {
                splash.close();
                setEnabled(true);

                try {
                    onResult.accept(get());
                } catch (Exception ex) {
                    onResult.accept(
                            Hl7Result.error(
                                    Hl7Error.technical(
                                            "Error técnico inesperado",
                                            Hl7ErrorOrigin.TRANSPORTE
                                    )
                            )
                    );
                }
            }
        };

        worker.execute();
    }
}
