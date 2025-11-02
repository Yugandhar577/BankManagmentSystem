package com.bank.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Simple TransactionServlet for demo/testing purposes.
 * - GET /transactions?accountId={id} -> returns JSON list of transactions for account (or all if omitted)
 * - POST /transactions -> create a transaction (form params or JSON body)
 *
 * Supported transaction types: DEPOSIT, WITHDRAW, TRANSFER
 *
 * NOTE: This servlet uses in-memory storage for accounts and transactions. It's intended as a simple example.
 */
@WebServlet(name = "TransactionServlet", urlPatterns = {"/transactions"})
public class TransactionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Simple in-memory storage (thread-safe access via synchronized blocks)
    private static final Map<Long, Account> accounts = new HashMap<>();
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final AtomicLong txIdGenerator = new AtomicLong(1);
    private static final AtomicLong accountIdGenerator = new AtomicLong(1);

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize a couple of demo accounts
        synchronized (accounts) {
            if (accounts.isEmpty()) {
                long a1 = accountIdGenerator.getAndIncrement();
                long a2 = accountIdGenerator.getAndIncrement();
                accounts.put(a1, new Account(a1, new BigDecimal("1000.00")));
                accounts.put(a2, new Account(a2, new BigDecimal("500.00")));
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accountIdParam = req.getParameter("accountId");
        List<Transaction> copy;
        synchronized (transactions) {
            if (accountIdParam != null) {
                try {
                    long aid = Long.parseLong(accountIdParam);
                    copy = new ArrayList<>();
                    for (Transaction t : transactions) {
                        if (t.accountId == aid || t.targetAccountId == aid) copy.add(t);
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(resp, "{\"error\":\"invalid accountId\"}");
                    return;
                }
            } else {
                copy = new ArrayList<>(transactions);
            }
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        writeJson(resp, toJsonArray(copy));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Accept either form params or a simple JSON body
        String type = req.getParameter("type");
        String accountIdStr = req.getParameter("accountId");
        String targetAccountIdStr = req.getParameter("targetAccountId");
        String amountStr = req.getParameter("amount");

        if ((type == null || amountStr == null || accountIdStr == null) && "application/json".equalsIgnoreCase(req.getContentType())) {
            String body = readBody(req);
            type = type == null ? jsonString(body, "type") : type;
            accountIdStr = accountIdStr == null ? jsonNumber(body, "accountId") : accountIdStr;
            targetAccountIdStr = targetAccountIdStr == null ? jsonNumber(body, "targetAccountId") : targetAccountIdStr;
            amountStr = amountStr == null ? jsonNumber(body, "amount") : amountStr;
        }

        if (type == null || accountIdStr == null || amountStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"error\":\"missing required fields (type, accountId, amount)\"}");
            return;
        }

        TransactionType txType;
        try {
            txType = TransactionType.valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"error\":\"invalid transaction type\"}");
            return;
        }

        long accountId;
        long targetAccountId = -1;
        BigDecimal amount;
        try {
            accountId = Long.parseLong(accountIdStr);
            if (targetAccountIdStr != null && !targetAccountIdStr.trim().isEmpty()) {
                targetAccountId = Long.parseLong(targetAccountIdStr);
            }
            amount = new BigDecimal(amountStr);
            if (amount.signum() <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(resp, "{\"error\":\"invalid numeric fields\"}");
            return;
        }

        // Ensure accounts exist (auto-create for demo)
        synchronized (accounts) {
            accounts.computeIfAbsent(accountId, id -> new Account(id, BigDecimal.ZERO));
            if (txType == TransactionType.TRANSFER) {
                accounts.computeIfAbsent(targetAccountId, id -> new Account(id, BigDecimal.ZERO));
            }
        }

        // Execute transaction
        Transaction tx;
        synchronized (accounts) {
            Account src = accounts.get(accountId);
            Account dest = txType == TransactionType.TRANSFER ? accounts.get(targetAccountId) : null;

            switch (txType) {
                case DEPOSIT:
                    src.balance = src.balance.add(amount);
                    tx = new Transaction(txIdGenerator.getAndIncrement(), txType, accountId, -1, amount, Instant.now().toEpochMilli());
                    break;
                case WITHDRAW:
                    if (src.balance.compareTo(amount) < 0) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        writeJson(resp, "{\"error\":\"insufficient funds\"}");
                        return;
                    }
                    src.balance = src.balance.subtract(amount);
                    tx = new Transaction(txIdGenerator.getAndIncrement(), txType, accountId, -1, amount, Instant.now().toEpochMilli());
                    break;
                case TRANSFER:
                    if (dest == null) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        writeJson(resp, "{\"error\":\"targetAccountId required for transfer\"}");
                        return;
                    }
                    if (src.balance.compareTo(amount) < 0) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        writeJson(resp, "{\"error\":\"insufficient funds\"}");
                        return;
                    }
                    src.balance = src.balance.subtract(amount);
                    dest.balance = dest.balance.add(amount);
                    tx = new Transaction(txIdGenerator.getAndIncrement(), txType, accountId, targetAccountId, amount, Instant.now().toEpochMilli());
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    writeJson(resp, "{\"error\":\"unsupported transaction type\"}");
                    return;
            }
        }

        synchronized (transactions) {
            transactions.add(tx);
        }

        // Build response with transaction and updated balances
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"transaction\":").append(tx.toJson()).append(",");
        sb.append("\"balances\":{");
        Account a1 = accounts.get(accountId);
        sb.append("\"").append(accountId).append("\":").append(a1.balance.toPlainString());
        if (tx.targetAccountId != -1) {
            Account a2 = accounts.get(tx.targetAccountId);
            sb.append(",\"").append(tx.targetAccountId).append("\":").append(a2.balance.toPlainString());
        }
        sb.append("}}");

        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        writeJson(resp, sb.toString());
    }

    // ---- Helpers and simple models ----

    private static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    // Very small and permissive JSON extraction helpers (not a full JSON parser).
    private static String jsonString(String json, String key) {
        if (json == null) return null;
        String needle = "\"" + key + "\"";
        int idx = json.indexOf(needle);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + needle.length());
        if (colon == -1) return null;
        int firstQuote = json.indexOf("\"", colon + 1);
        if (firstQuote == -1) return null;
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static String jsonNumber(String json, String key) {
        if (json == null) return null;
        String needle = "\"" + key + "\"";
        int idx = json.indexOf(needle);
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx + needle.length());
        if (colon == -1) return null;
        int i = colon + 1;
        // skip spaces
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        int start = i;
        boolean inNumber = false;
        while (i < json.length()) {
            char c = json.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.' || c == '-' || c == '+') {
                inNumber = true;
                i++;
            } else {
                break;
            }
        }
        if (!inNumber) return null;
        return json.substring(start, i);
    }

    private static String toJsonArray(List<Transaction> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Transaction t : list) {
            if (!first) sb.append(",");
            sb.append(t.toJson());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private static void writeJson(HttpServletResponse resp, String json) throws IOException {
        try (PrintWriter out = resp.getWriter()) {
            out.write(json);
            out.flush();
        }
    }

    private enum TransactionType {
        DEPOSIT, WITHDRAW, TRANSFER
    }

    private static class Account {
        final long id;
        BigDecimal balance;

        Account(long id, BigDecimal balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    private static class Transaction {
        final long id;
        final TransactionType type;
        final long accountId;
        final long targetAccountId; // -1 when not applicable
        final BigDecimal amount;
        final long timestamp;

        Transaction(long id, TransactionType type, long accountId, long targetAccountId, BigDecimal amount, long timestamp) {
            this.id = id;
            this.type = type;
            this.accountId = accountId;
            this.targetAccountId = targetAccountId;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"id\":").append(id).append(",");
            sb.append("\"type\":\"").append(type.name()).append("\",");
            sb.append("\"accountId\":").append(accountId).append(",");
            sb.append("\"amount\":").append(amount.toPlainString()).append(",");
            sb.append("\"timestamp\":").append(timestamp);
            if (targetAccountId != -1) {
                sb.append(",\"targetAccountId\":").append(targetAccountId);
            }
            sb.append("}");
            return sb.toString();
        }
    }
}