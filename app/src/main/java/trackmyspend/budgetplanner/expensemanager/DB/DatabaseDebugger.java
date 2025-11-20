package trackmyspend.budgetplanner.expensemanager.DB;

import android.util.Log;

import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;

import java.util.List;
import java.util.concurrent.Executors;

public class DatabaseDebugger {

    private static final String TAG = "DB_DEBUG";

    public static void logDatabase(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Users
                List<User> users = db.userDao().getAllUsers();
                for (User u : users) {
                    Log.d(TAG, "User → id=" + u.user_id + ", name=" + u.name + ", currency=" + u.currency_name);
                }

                // Accounts
                List<Account> accounts = db.accountDao().getAllAccounts();
                for (Account a : accounts) {
                    Log.d(TAG, "Account → id=" + a.account_id + ", name=" + a.name +
                            ", amount=" + a.amount + ", icon=" + a.icon + ", color=" + a.iconColorHex);
                }

                // Subtypes
                List<Subtype> subtypes = db.subtypeDao().getAllSubtypes();
                for (Subtype s : subtypes) {
                    Log.d(TAG, "Subtype → id=" + s.subtype_id + ", accountId=" + s.account_id +
                            ", name=" + s.name + ", icon=" + s.icon + ", bg=" + s.backgroundColorHex);
                }

                // Categories
                List<Category> categories = db.categoryDao().getAllCategories();
                for (Category c : categories) {
                    Log.d(TAG, "Category → id=" + c.category_id + ", name=" + c.name +
                            ", icon=" + c.icon + ", color=" + c.colorHex + ", type=" + c.type);
                }

                // Transactions
                List<Transaction> transactions = db.transactionDao().getAllTransactions();
                for (Transaction t : transactions) {
                    Log.d(TAG, "Transaction → id=" + t.transaction_id +
                            ", userId=" + t.user_id +
                            ", subtypeId=" + t.subtype_id +
                            ", categoryId=" + t.category_id +
                            ", amount=" + t.amount +
                            ", notes=" + t.notes +
                            ", type=" + t.type +
                            ", date=" + t.date);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error logging database", e);
            }
        });
    }
}
