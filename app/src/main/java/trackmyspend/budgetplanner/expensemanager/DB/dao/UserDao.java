package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import trackmyspend.budgetplanner.expensemanager.DB.entities.User;

import java.util.Date;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);
    @Update
    void update(User user);
    @Delete
    void delete(User user);

    @Query("SELECT * FROM users LIMIT 1")
    User getFirstUser();

    @Query("SELECT * FROM users WHERE user_id = :id")
    User getUserById(long id);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("UPDATE users SET mode = :mode, updated_at = :updatedAt WHERE user_id = :userId")
    void updateMode(long userId, String mode, Date updatedAt);

    @Query("SELECT mode FROM users WHERE user_id = :userId LIMIT 1")
    String getUserMode(long userId);

    @Query("UPDATE users SET remaining_transaction_cnt = remaining_transaction_cnt + :addValue WHERE user_id = :userId")
    void addRemainingTransactions(long userId, int addValue);

}
