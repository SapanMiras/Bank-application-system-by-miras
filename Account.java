package org.example;

import java.math.BigDecimal;

public class Account {
    public long id;
    public long bankId;
    public long customerId;
    public String ownerName;
    public BigDecimal balance;

    public Account(long id, long bankId, long customerId, String ownerName, BigDecimal balance) {
        this.id = id;
        this.bankId = bankId;
        this.customerId = customerId;
        this.ownerName = ownerName;
        this.balance = balance;
    }
}
