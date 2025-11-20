package trackmyspend.budgetplanner.expensemanager.DB.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.*;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;

import java.util.List;

@Dao
public interface SubtypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Subtype subtype);

    @Update
    void updateSubtype(Subtype subtype);

    @Delete
    void deleteSubtype(Subtype subtype);

    @Query("SELECT * FROM subtypes")
    List<Subtype> getAllSubtypes();  // ✅ For background thread use

    @Query("SELECT * FROM subtypes WHERE account_id = :accountId ORDER BY subtype_id ASC")
    LiveData<List<Subtype>> getSubtypesByAccountIdLive(long accountId);

    @Query("SELECT * FROM subtypes WHERE subtype_id = :id LIMIT 1")
    Subtype getSubtypeById(long id);

    @Query("SELECT account_id FROM subtypes WHERE subtype_id = :subtypeId LIMIT 1")
    long getAccountIdBySubtypeId(long subtypeId);


    @Query("SELECT a.account_id AS account_id, a.name AS account_name " +
            "FROM accounts a " +
            "INNER JOIN subtypes s ON s.account_id = a.account_id " +
            "WHERE s.subtype_id = :subtypeId " +
            "LIMIT 1")
    AccountInfo getAccountInfoBySubtypeId(long subtypeId);

    // ✅ Make this public static so Room can use it
    public static class AccountInfo {
        public long account_id;
        public String account_name;
    }

    @Query("DELETE FROM subtypes WHERE account_id = :accountId")
    void deleteSubtypesByAccountId(long accountId);

    @Query("SELECT * FROM subtypes WHERE account_id = :accountId ORDER BY subtype_id ASC")
    List<Subtype> getSubtypesByAccountId(long accountId);


    @Query("SELECT * FROM subtypes")
    LiveData<List<Subtype>> getAllSubtypesLive();

}