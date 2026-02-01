package org.example;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.example.dto.*;

import java.sql.SQLException;
import java.util.Map;

public class RestServer {

    public static void main(String[] args) {
        BankService service = new BankService();

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        });

        // -------- Global error handling --------
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("error", e.getMessage()));
        });
        app.exception(SQLException.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", "Database error", "details", e.getMessage()));
        });
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("error", "Server error", "details", e.getMessage()));
        });

        // -------- Health --------
        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok")));

        // -------- Banks --------
        app.post("/api/banks", ctx -> {
            CreateBankRequest req = ctx.bodyAsClass(CreateBankRequest.class);
            long id = service.createBank(req.name);
            ctx.status(HttpStatus.CREATED);
            ctx.json(Map.of("id", id));
        });

        app.get("/api/banks", ctx -> ctx.json(service.listBanks()));

        app.get("/api/banks/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            ctx.json(service.getBank(id));
        });

        // -------- Customers --------
        app.post("/api/customers", ctx -> {
            CreateCustomerRequest req = ctx.bodyAsClass(CreateCustomerRequest.class);
            long id = service.createCustomer(req.fullName, req.email);
            ctx.status(HttpStatus.CREATED);
            ctx.json(Map.of("id", id));
        });

        app.get("/api/customers", ctx -> ctx.json(service.listCustomers()));

        app.get("/api/customers/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            ctx.json(service.getCustomer(id));
        });

        // -------- Accounts --------
        app.post("/api/accounts", ctx -> {
            OpenAccountRequest req = ctx.bodyAsClass(OpenAccountRequest.class);
            long id = service.openAccount(req.customerId, req.bankId, req.initialBalance);
            ctx.status(HttpStatus.CREATED);
            ctx.json(Map.of("id", id));
        });

        app.get("/api/accounts/{id}", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            ctx.json(service.getAccount(id));
        });

        app.get("/api/accounts/{id}/balance", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            ctx.json(Map.of("accountId", id, "balance", service.getBalance(id)));
        });

        app.post("/api/accounts/{id}/deposit", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            AmountRequest req = ctx.bodyAsClass(AmountRequest.class);
            service.deposit(id, req.amount);
            ctx.json(Map.of("status", "ok"));
        });

        app.post("/api/accounts/{id}/withdraw", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            AmountRequest req = ctx.bodyAsClass(AmountRequest.class);
            service.withdraw(id, req.amount);
            ctx.json(Map.of("status", "ok"));
        });

        app.post("/api/accounts/transfer", ctx -> {
            TransferRequest req = ctx.bodyAsClass(TransferRequest.class);
            service.transfer(req.fromId, req.toId, req.amount);
            ctx.json(Map.of("status", "ok"));
        });

        app.get("/api/accounts/{id}/transactions", ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            int limit = 50;
            String limitStr = ctx.queryParam("limit");
            if (limitStr != null) limit = Integer.parseInt(limitStr);
            ctx.json(service.listTransactions(id, limit));
        });

        int port = 7070;
        app.start(port);
        System.out.println("REST API started: http://localhost:" + port);
        System.out.println("Health:          http://localhost:" + port + "/api/health");
    }
}
