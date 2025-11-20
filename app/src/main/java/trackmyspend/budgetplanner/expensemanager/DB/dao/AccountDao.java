package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import java.util.List;

@Dao
public interface AccountDao {
    @Insert
    long insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM accounts")
    List<Account> getAllAccounts();

    @Query("SELECT * FROM accounts ORDER BY account_id ASC")
    LiveData<List<Account>> getAllAccountsLive();

    @Query("SELECT * FROM accounts WHERE user_id = :userId")
    LiveData<List<Account>> getAccountsByUser(long userId);

    @Query("UPDATE accounts SET amount = amount + :delta WHERE account_id = :accountId")
    void updateBalance(long accountId, double delta);

    @Query("DELETE FROM accounts WHERE account_id = :accountId")
    void deleteAccountById(long accountId);

    // ✅ New alias to match adapter’s call
    @Delete
    default void deleteAccount(Account account) {
        delete(account); // reuse existing delete()
    }
}
