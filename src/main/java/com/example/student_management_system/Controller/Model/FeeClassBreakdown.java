package com.example.student_management_system.Controller.Model;

import java.math.BigDecimal;

public class FeeClassBreakdown {

    private final String className;
    private final BigDecimal expected;
    private final BigDecimal collected;
    private final BigDecimal outstanding;

    public FeeClassBreakdown(
            String className,
            BigDecimal expected,
            BigDecimal collected,
            BigDecimal outstanding
    ) {
        this.className = className;
        this.expected = expected == null ? BigDecimal.ZERO : expected;
        this.collected = collected == null ? BigDecimal.ZERO : collected;
        this.outstanding = outstanding == null ? BigDecimal.ZERO : outstanding;
    }

    public String getClassName() {
        return className;
    }

    public BigDecimal getExpected() {
        return expected;
    }

    public BigDecimal getCollected() {
        return collected;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }
}
