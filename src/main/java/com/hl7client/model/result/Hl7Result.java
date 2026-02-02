package com.hl7client.model.result;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Hl7Result<T> {

    private final Hl7Status status;
    private final T data;
    private final Hl7Error issue;
    private final List<Hl7ItemError> details;

    private Hl7Result(
            Hl7Status status,
            T data,
            Hl7Error issue,
            List<Hl7ItemError> details
    ) {
        this.status = Objects.requireNonNull(status);
        this.data = data;
        this.issue = issue;
        this.details = details == null
                ? List.of()
                : Collections.unmodifiableList(details);
    }

    // ========= FACTORIES =========

    public static <T> Hl7Result<T> ok(T data) {
        return new Hl7Result<>(
                Hl7Status.OK,
                data,
                null,
                List.of()
        );
    }

    public static <T> Hl7Result<T> partial(
            T data,
            List<Hl7ItemError> details
    ) {
        return new Hl7Result<>(
                Hl7Status.PARTIAL,
                Objects.requireNonNull(data),
                null,
                details
        );
    }

    public static <T> Hl7Result<T> rejected(
            T data,
            Hl7Error functionalError
    ) {
        return new Hl7Result<>(
                Hl7Status.REJECTED,
                data,
                functionalError,
                List.of()
        );
    }

    public static <T> Hl7Result<T> error(Hl7Error technicalError) {
        return new Hl7Result<>(
                Hl7Status.ERROR,
                null,
                technicalError,
                List.of()
        );
    }

    // ========= HELPERS =========

    public boolean isOk() {
        return status == Hl7Status.OK;
    }

    public boolean isPartial() {
        return status == Hl7Status.PARTIAL;
    }

    public boolean isRejected() {
        return status == Hl7Status.REJECTED;
    }

    public boolean isError() {
        return status == Hl7Status.ERROR;
    }

    // ========= ACCESS =========

    public Hl7Status getStatus() {
        return status;
    }

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    public Optional<Hl7Error> getIssue() {
        return Optional.ofNullable(issue);
    }

    public List<Hl7ItemError> getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "Hl7Result{" +
                "status=" + status +
                ", data=" + data +
                ", issue=" + issue +
                ", details=" + details +
                '}';
    }
}
