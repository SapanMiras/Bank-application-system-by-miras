package org.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    public long id;
    public Long fromAccountId;
    public Long toAccountId;
    public String type;
    public BigDecimal amount;
    public String note;
    public LocalDateTime createdAt;

    public Transaction(long id, Long fromAccountId, Long toAccountId, String type,
                       BigDecimal amount, String note, LocalDateTime createdAt) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.type = type;
        this.amount = amount;
        this.note = note;
        this.createdAt = createdAt;
    }
}
