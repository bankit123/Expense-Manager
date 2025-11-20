package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import trackmyspend.budgetplanner.expensemanager.DB.Graph.CategorySummary;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {

    // Insert a new transaction
    @Insert
    long insert(Transaction transaction);

    // Update an existing transaction
    @Update
    void update(Transaction transaction);

    // Delete a transaction
    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions")
    List<Transaction> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByPeriod(long userId, Date startDate, Date endDate);

    @Query("SELECT * FROM transactions WHERE transaction_id = :id LIMIT 1")
    Transaction getTransactionByIdSync(long id);

    @Query("SELECT t.* FROM transactions t " +
            "INNER JOIN subtypes s ON t.subtype_id = s.subtype_id " +
            "INNER JOIN accounts a ON s.account_id = a.account_id " +
            "WHERE a.account_id = :accountId " +
            "AND (:type IS NULL OR :type = 'All' OR t.type = :type) " +
            "AND (:subtypeId = -1 OR t.subtype_id = :subtypeId) " +
            "AND (:start IS NULL OR :end IS NULL OR (t.date BETWEEN :start AND :end)) " +
            "ORDER BY t.date DESC")
    LiveData<List<Transaction>> getFilteredTransactions(
            long accountId,
            String type,
            long subtypeId,
            Date start,
            Date end
    );


    @Query("SELECT c.name AS categoryName, c.colorHex AS colorHex, c.icon AS categoryIcon, " +
            "SUM(t.amount) AS totalAmount " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.category_id " +
            "WHERE t.user_id = :userId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate " +
            "GROUP BY c.category_id, c.name, c.colorHex, c.icon")
    LiveData<List<CategorySummary>> getCategorySummary(long userId, String type, Date startDate, Date endDate);

    @Query("DELETE FROM transactions WHERE transfer_group_id = :groupId")
    void deleteTransferGroup(long groupId);

    @Query("SELECT * FROM transactions WHERE transfer_group_id = :groupId")
    List<Transaction> getTransactionsByTransferGroupId(long groupId);

    @Query("SELECT COUNT(*) FROM transactions WHERE subtype_id = :subtypeId AND (type = 'TransferCredit' OR type = 'TransferDebit')")
    int countTransferTransactionsForSubtype(long subtypeId);

    @Query("SELECT COUNT(*) > 0 FROM transactions WHERE subtype_id = :subtypeId AND (type = 'TransferDebit' OR type = 'TransferCredit')")
    boolean hasTransferBySubtypeId(long subtypeId);




}
