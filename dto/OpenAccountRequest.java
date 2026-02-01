package org.example.dto;

import java.math.BigDecimal;

public class OpenAccountRequest {
    public long customerId;
    public long bankId;
    public BigDecimal initialBalance;
}
