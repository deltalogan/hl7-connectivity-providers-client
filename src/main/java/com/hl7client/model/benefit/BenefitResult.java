package com.hl7client.model.benefit;

import java.util.Collections;
import java.util.List;

public class BenefitResult {

    private final List<BenefitItem> items;

    public BenefitResult(List<BenefitItem> items) {
        this.items = items != null ? items : Collections.emptyList();
    }

    public List<BenefitItem> getItems() {
        return items;
    }

    public int totalLength() {
        return items.stream()
                .mapToInt(BenefitItem::length)
                .sum();
    }
}
