import java.sql.*;

public class BankService {

    // ----------------------------------------------------------------
    // LOGIN  →  sp_login(username, password)
    //   Returns ResultSet: message = 'SUCCESS' or 'INVALID'
    // ----------------------------------------------------------------
    public boolean login(String username, String password) {
        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_login(?, ?)}");
            cs.setString(1, username);
            cs.setString(2, password);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                return "SUCCESS".equals(rs.getString("message"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------------------------------------------
    // CREATE ACCOUNT  →  sp_create_account(name, balance, type)
    //   Returns ResultSet: acc_id = new account id
    // ----------------------------------------------------------------
    public Account createAcc(String name, double balance, String type) {
        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_create_account(?, ?, ?)}");
            cs.setString(1, name);
            cs.setDouble(2, balance);
            cs.setString(3, type);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                int newId = rs.getInt("acc_id");
                if (newId > 0) {
                    return new Account(newId, name, balance, type);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // FIND ACCOUNT  →  sp_get_account(acc_id)
    //   Returns ResultSet: acc_id, acc_name, balance, acc_type  (or empty)
    // ----------------------------------------------------------------
    public Account findAcc(int accId) {
        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_get_account(?)}");
            cs.setInt(1, accId);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                return new Account(
                    rs.getInt("acc_id"),
                    rs.getString("acc_name"),
                    rs.getDouble("balance"),
                    rs.getString("acc_type")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------------------------------------------
    // DEPOSIT  →  sp_deposit(acc_id, amount)
    //   Returns ResultSet: message = 'DEPOSIT SUCCESS' | 'INVALID AMOUNT' | 'ACCOUNT NOT FOUND'
    // ----------------------------------------------------------------
    public String deposit(int accId, double amount) {
        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_deposit(?, ?)}");
            cs.setInt(1, accId);
            cs.setDouble(2, amount);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                return formatMessage(rs.getString("message"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error during deposit.";
    }

    // ----------------------------------------------------------------
    // WITHDRAW  →  sp_withdraw(acc_id, amount)
    //   Returns ResultSet: message = 'WITHDRAW SUCCESS' | 'INVALID AMOUNT'
    //                               | 'ACCOUNT NOT FOUND' | 'INSUFFICIENT BALANCE'
    // ----------------------------------------------------------------
    public String withdraw(int accId, double amount) {
        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_withdraw(?, ?)}");
            cs.setInt(1, accId);
            cs.setDouble(2, amount);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                return formatMessage(rs.getString("message"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error during withdrawal.";
    }

    // ----------------------------------------------------------------
    // TRANSFER  →  sp_transfer(from_id, to_id, amount)
    //   Returns ResultSet: message = 'TRANSFER SUCCESS' | 'INVALID AMOUNT'
    //                               | 'SENDER NOT FOUND' | 'INSUFFICIENT BALANCE'
    //                               | 'RECEIVER NOT FOUND' | 'ERROR'
    // ----------------------------------------------------------------
    public String transfer(int fromId, int toId, double amount) {
        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_transfer(?, ?, ?)}");
            cs.setInt(1, fromId);
            cs.setInt(2, toId);
            cs.setDouble(3, amount);

            ResultSet rs = cs.executeQuery();

            if (rs.next()) {
                return formatMessage(rs.getString("message"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error during transfer.";
    }

    // ----------------------------------------------------------------
    // CHECK BALANCE  (reuses findAcc -> sp_get_account)
    // ----------------------------------------------------------------
    public String checkBal(int accId) {
        Account acc = findAcc(accId);
        if (acc != null) {
            return "Account  : " + acc.getAccName() + "\n"
                 + "Type     : " + acc.getAccType() + "\n"
                 + "Balance  : Rs." + String.format("%.2f", acc.getBalance());
        }
        return "Account not found.";
    }

    // ----------------------------------------------------------------
    // TRANSACTION HISTORY  →  sp_get_transactions(acc_id)
    //   Returns ResultSet: txn_id, acc_id, type, amount, date
    // ----------------------------------------------------------------
    public String getTransactionHistory(int accId) {
        StringBuilder sb = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {

            CallableStatement cs = conn.prepareCall("{CALL sp_get_transactions(?)}");
            cs.setInt(1, accId);

            ResultSet rs = cs.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;
                sb.append("ID: ").append(rs.getInt("txn_id"))
                  .append("  |  ").append(String.format("%-14s", rs.getString("type")))
                  .append("  |  Rs.").append(String.format("%10.2f", rs.getDouble("amount")))
                  .append("  |  ").append(rs.getTimestamp("date"))
                  .append("\n");
            }

            if (!found) {
                sb.append("No transactions found for account ").append(accId).append(".");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sb.append("Error fetching transactions.");
        }

        return sb.toString();
    }

    // ----------------------------------------------------------------
    // Helper: converts raw DB message strings into readable UI text
    // ----------------------------------------------------------------
    private String formatMessage(String msg) {
        if (msg == null) return "Unknown error.";
        return switch (msg) {
            case "DEPOSIT SUCCESS"      -> "Deposit successful!";
            case "WITHDRAW SUCCESS"     -> "Withdrawal successful!";
            case "TRANSFER SUCCESS"     -> "Transfer successful!";
            case "INVALID AMOUNT"       -> "Invalid amount. Must be greater than 0.";
            case "ACCOUNT NOT FOUND"    -> "Account not found.";
            case "INSUFFICIENT BALANCE" -> "Insufficient balance.";
            case "SENDER NOT FOUND"     -> "Sender account not found.";
            case "RECEIVER NOT FOUND"   -> "Receiver account not found.";
            case "ERROR"                -> "A database error occurred. Transaction rolled back.";
            default                     -> msg;
        };
    }
}