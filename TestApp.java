public class TestApp {
    public static void main(String[] args) {

        BankService bank = new BankService();

        System.out.println("Login: " + bank.login("admin", "admin123"));

        Account acc = bank.createAcc("Athi", 5000, "Savings");
        System.out.println("New Account ID: " + acc.getAccId());

        System.out.println(bank.deposit(acc.getAccId(), 1000));

        System.out.println(bank.withdraw(acc.getAccId(), 500));

        System.out.println(bank.checkBal(acc.getAccId()));

        System.out.println(bank.getTransactionHistory(acc.getAccId()));
    }
}