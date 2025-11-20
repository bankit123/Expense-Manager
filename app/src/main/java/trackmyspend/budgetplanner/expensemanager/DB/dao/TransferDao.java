package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.room.*;
import java.util.List;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transfer;


@Dao
public interface TransferDao {
    @Insert long insert(Transfer transfer);
    @Update void update(Transfer transfer);
    @Delete void delete(Transfer transfer);

    @Query("SELECT * FROM transfers WHERE user_id = :userId")
    List<Transfer> getTransfersByUser(long userId);
}

