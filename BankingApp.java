import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;


public class BankingApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}



class LoginFrame extends JFrame {

    private final JTextField  usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final BankService bank = new BankService();

    LoginFrame() {
        setTitle("Banking System – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 260);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Banking System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 10));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        main.add(form, BorderLayout.CENTER);


        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.addActionListener(e -> doLogin());

        getRootPane().setDefaultButton(loginBtn);

        JPanel south = new JPanel();
        south.add(loginBtn);
        main.add(south, BorderLayout.SOUTH);

        add(main);
        setVisible(true);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (bank.login(username, password)) {
            dispose();
            new DashboardFrame(bank);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Try again.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}



class DashboardFrame extends JFrame {

    private final BankService bank;
    private final JTextArea outputArea = new JTextArea(12, 45);

    DashboardFrame(BankService bank) {
        this.bank = bank;

        setTitle("Banking System – Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Banking Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        main.add(title, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new GridLayout(7, 1, 5, 8));
        btnPanel.setBorder(new TitledBorder("Operations"));

        String[] labels = {
            "Create Account", "Deposit", "Withdraw",
            "Transfer", "Check Balance", "Transaction History", "Logout"
        };

        for (String label : labels) {
            JButton btn = new JButton(label);
            btn.addActionListener(e -> handleAction(label));
            btnPanel.add(btn);
        }

        main.add(btnPanel, BorderLayout.WEST);

        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        outputArea.setText("Welcome! Select any operation from the left.\n");
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(new TitledBorder("Output"));
        main.add(scroll, BorderLayout.CENTER);

        add(main);
        setVisible(true);
    }

    private void handleAction(String action) {
        switch (action) {
            case "Create Account" -> createAccount();
            case "Deposit"        -> deposit();
            case "Withdraw"       -> withdraw();
            case "Transfer"       -> transfer();
            case "Check Balance"  -> checkBalance();
            case "Transaction History" -> transactionHistory();
            case "Logout"         -> logout();
        }
    }


    private void print(String text) {
        outputArea.setText(text + "\n");
    }

    private String ask(String prompt) {
        String val = JOptionPane.showInputDialog(this, prompt);
        return (val == null) ? null : val.trim();
    }

    private Integer askInt(String prompt) {
        String s = ask(prompt);
        if (s == null) return null;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private Double askDouble(String prompt) {
        String s = ask(prompt);
        if (s == null) return null;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }


    private void createAccount() {
        String name = ask("Enter account holder name:");
        if (name == null || name.isEmpty()) return;

        Double bal = askDouble("Enter initial deposit amount:");
        if (bal == null) return;

        String[] types = {"Savings", "Current"};
        int choice = JOptionPane.showOptionDialog(this, "Select account type:",
                "Account Type", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (choice < 0) return;

        Account acc = bank.createAcc(name, bal, types[choice]);
        if (acc != null) {
            print(" Account created successfully\n"
                + "  Account ID : " + acc.getAccId() + "\n"
                + "  Name       : " + acc.getAccName() + "\n"
                + "  Type       : " + acc.getAccType() + "\n"
                + "  Balance    : ₹" + String.format("%.2f", acc.getBalance()));
        } else {
            print(" Failed to create account. Check DB connection.");
        }
    }

    private void deposit() {
        Integer id = askInt("Enter Account ID:");
        if (id == null) return;

        Double amt = askDouble("Enter deposit amount:");
        if (amt == null) return;

        print(bank.deposit(id, amt));
    }

    private void withdraw() {
        Integer id = askInt("Enter Account ID:");
        if (id == null) return;

        Double amt = askDouble("Enter withdrawal amount:");
        if (amt == null) return;

        print(bank.withdraw(id, amt));
    }

    private void transfer() {
        Integer from = askInt("Enter Sender Account ID:");
        if (from == null) return;

        Integer to = askInt("Enter Receiver Account ID:");
        if (to == null) return;

        Double amt = askDouble("Enter transfer amount:");
        if (amt == null) return;

        print(bank.transfer(from, to, amt));
    }

    private void checkBalance() {
        Integer id = askInt("Enter Account ID:");
        if (id == null) return;

        print(bank.checkBal(id));
    }

    private void transactionHistory() {
        Integer id = askInt("Enter Account ID:");
        if (id == null) return;

        print(" Transaction History for Account " + id + " \n"
            + bank.getTransactionHistory(id));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame();
        }
    }
}