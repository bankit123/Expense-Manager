package trackmyspend.budgetplanner.expensemanager.DB;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.*;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import trackmyspend.budgetplanner.expensemanager.DB.dao.AccountDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.CategoryDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.ColorDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.EMIDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.IconDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.RecurringScheduleDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.RecurringTransactionDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.ReminderDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.SubtypeDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.TransactionDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.TransferDao;
import trackmyspend.budgetplanner.expensemanager.DB.dao.UserDao;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Account;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Category;
import trackmyspend.budgetplanner.expensemanager.DB.entities.ColorEntity;
import trackmyspend.budgetplanner.expensemanager.DB.entities.EMI;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Icon;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.RecurringTransactionSchedule;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Reminder;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Subtype;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transaction;
import trackmyspend.budgetplanner.expensemanager.DB.entities.Transfer;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;

import java.util.Date;
import java.util.concurrent.Executors;

@Database(entities = {
        User.class,
        Account.class,
        Subtype.class,
        Category.class,
        Transaction.class,
        Transfer.class,
        Icon.class,
        ColorEntity.class,
        EMI.class,
        Reminder.class,
        RecurringTransaction.class,
        RecurringTransactionSchedule.class
}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract AccountDao accountDao();

    public abstract CategoryDao categoryDao();

    public abstract TransactionDao transactionDao();

    public abstract TransferDao transferDao();

    public abstract IconDao iconDao();

    public abstract ColorDao colorDao();

    public abstract SubtypeDao subtypeDao();//

    public abstract RecurringScheduleDao recurringScheduleDao();


    public abstract EMIDao emiDao();

    public abstract ReminderDao reminderDao();

    public abstract RecurringTransactionDao recurringTransactionDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "expense_manager_db")
                            .addCallback(roomCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .build();

                }
            }
        }
        return INSTANCE;
    }

    // 🔹 Migration from version 1 → 2
//    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase db) {
//            // ✅ Add new categories during migration (version 1 → 2)
//            db.execSQL("INSERT INTO categories (user_id, name, icon, colorHex, type) VALUES " +
//                    "(1, 'Investment Returns', 'ic_investment', '#A5D6A7', 'Income');"
//            );
//
//            // ⚙️ (Optional) add any new columns if needed in v2
//            // db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN currency TEXT DEFAULT 'INR'");
//        }
//    };

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {

            // ✅ Add new column for Transfer linkage
            // Default is NULL → safe for old data
            db.execSQL("ALTER TABLE transactions ADD COLUMN transfer_group_id INTEGER");

            // ✅ Add Transfer Category (only once)
            db.execSQL(
                    "INSERT INTO categories (user_id, name, icon, colorHex, type) VALUES " +
                            "(1, 'Transfer', 'ic_transfer_account', '#FFE0B2', 'none');"
            );

            // ✅ Add Investment Returns (from your example)
            db.execSQL(
                    "INSERT INTO categories (user_id, name, icon, colorHex, type) VALUES " +
                            "(1, 'Investment Returns', 'ic_investment', '#A5D6A7', 'Income');"
            );
        }
    };



    // 🔹 Template: version 2 → 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Add schema changes for version 3 here
            db.execSQL("ALTER TABLE users ADD COLUMN remaining_transaction_cnt INTEGER NOT NULL DEFAULT 0");
        }
    };

    // 🔹 Template: version 3 → 4 (future-proof)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // Add schema changes for version 4 here
        }
    };


    // ✅ New method to force close DB before restore
    public static void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
            INSTANCE = null;
        }
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            enableTriggers(db); // ✅ activate on fresh DB creation

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase database = INSTANCE;

                if (database != null) {
                    // ✅ Insert Default User
                    User defaultUser = new User();
                    defaultUser.created_at = new Date();
                    defaultUser.updated_at = new Date();
                    long userId = database.userDao().insert(defaultUser);

                    // Insert accounts and capture IDs
                    long cashWalletId = database.accountDao().insert(makeAccount(userId, "Cash Wallet", "ic_cash", "#FFE0B2", 0));
                    long bankAccountId = database.accountDao().insert(makeAccount(userId, "Bank Account", "ic_bank", "#C8E6C9", 0));
                    long cryptoId = database.accountDao().insert(makeAccount(userId, "CryptoCurrency", "ic_crypto", "#E1BEE7", 0));

                    String[][] defaultSubtypes = {
                            // 🔹 Bank Account Subtypes
                            {"Debit Card", "ic_card", "#FFE0B2", "BANK"},       // Pastel Orange – warm, friendly
                            {"Credit Card", "ic_credit_card", "#F8BBD0", "BANK"}, // Pastel Rose – soft pink tone
                            {"UPI Payment", "ic_upi", "#E5BEB5", "BANK"},       // Pastel Cyan – clean & techy
                            {"Net Banking", "ic_netbanking", "#9CAFAA", "BANK"}, // Pastel Indigo – calm & reliable
                            {"Bank Transfer", "ic_bank_transfer", "#90CAF9", "BANK"}, // Pastel Sky Blue – balanced depth
                            {"Cheque", "ic_cheque", "#C5E1A5", "BANK"},
                            {"Online Payment", "ic_transfer", "#C5E1A5", "BANK"},

                            // 🔹 Cash Wallet Subtypes
                            {"Cash in Hand", "ic_cash", "#B2DFDB", "CASH"},     // Pastel Teal – natural, not yellow

                            // 🔹 Crypto Subtypes
                            {"Crypto Wallet", "ic_crypto_wallet", "#A2AADB", "CRYPTO"}, // Pastel Violet – stylish, modern
                            {"Bitcoin", "ic_bitcoin", "#FFCCBC", "CRYPTO"},     // Pastel Coral – warm alternative to amber
                            {"Ethereum", "ic_ethereum", "#B3E5FC", "CRYPTO"}    // Pastel Blue – fresh, digital feel
                    };


                    for (String[] entry : defaultSubtypes) {
                        Subtype subtype = new Subtype();

                        // Choose which account ID to link based on the flag (BANK, CASH, CRYPTO)
                        switch (entry[3]) {
                            case "BANK":
                                subtype.account_id = bankAccountId;
                                break;
                            case "CASH":
                                subtype.account_id = cashWalletId;
                                break;
                            case "CRYPTO":
                                subtype.account_id = cryptoId;
                                break;
                        }

                        subtype.name = entry[0];
                        subtype.icon = entry[1];
                        subtype.backgroundColorHex = entry[2];

                        database.subtypeDao().insert(subtype);
                    }

                    String[][] defaultCategories = {
                            // 💸 Expense Categories
                            {"Transfer", "ic_transfer_account", "#FFE0B2", "none"},
                            {"Food", "ic_food", "#FFE0B2", "Expense"},              // Warm Pastel Orange
                            {"Transport", "ic_transport", "#B3E5FC", "Expense"},    // Soft Pastel Blue
                            {"Shopping", "ic_shopping", "#E1BEE7", "Expense"},      // Soft Pastel Purple
                            {"Bills", "ic_bills", "#FFCCBC", "Expense"},            // Soft Pastel Coral
                            {"Health", "ic_medicine", "#F8BBD0", "Expense"},        // Soft Pastel Pink
                            {"Entertainment", "ic_entertainment", "#FFC7A7", "Expense"}, // Balanced Violet
                            {"Education", "ic_education", "#C5CAE9", "Expense"},    // Pastel Indigo
                            {"Travel", "ic_travel", "#E6B2BA", "Expense"},          // Pastel Sky Blue
                            {"Coffee", "ic_coffee", "#FFDAB9", "Expense"},     // Pastel Peach (warm & soft)
                            {"Alcohol", "ic_alcohol", "#F8BBD0", "Expense"},   // Pastel Rose (matches tone)
                            {"Gym", "ic_gym", "#C8E6C9", "Expense"},           // Pastel Green (healthy vibe)
                            {"Salon", "ic_salon", "#E1BEE7", "Expense"},       // Pastel Lavender (beauty theme)
                            {"Delivery", "ic_delivery", "#B2EBF2", "Expense"},
                            {"Rent", "ic_rent", "#D7CCC8", "Expense"},              // Pastel Beige
                            {"Fuel", "ic_fuel", "#EBE5C2", "Expense"},              // Soft Peach
                            {"Insurance", "ic_insurance", "#F5B7B1", "Expense"},    // Gentle Rose
                            {"Pets", "ic_pets", "#C8E6C9", "Expense"},              // Balanced Green
                            {"Groceries", "ic_groceries", "#FFF3E0", "Expense"},    // Creamy Warm
                            {"EMI / Loan", "ic_loan", "#FFDBB6", "Expense"},         // Pastel Yellow-Green
                            {"Maintenance", "ic_maintenance", "#B2DFDB", "Expense"},// Aqua Teal
                            {"Subscriptions", "ic_subscription", "#E5E1DA", "Expense"}, // Soft Lavender
                            {"Party", "ic_party", "#FFCCBC", "Expense"},            // Light Lilac
                            {"Games", "ic_games", "#F0F4C3", "Expense"},            // Pastel Lime
                            {"Beauty", "ic_beauty", "#BADFDB", "Expense"},          // Soft Apricot
                            {"Charity", "ic_charity", "#A5D6A7", "Expense"},        // Minty Green

                            // 💰 Income Categories
                            {"Salary", "ic_salary", "#C8E6C9", "Income"},           // Soft Green
                            {"Gift", "ic_gift", "#C5CAE9", "Income"},               // Soft Indigo
                            {"Business", "ic_business", "#A5D6A7", "Income"},       // Mint Green
                            {"Bonus", "ic_bonus", "#B2DFDB", "Income"},             // Aqua Teal
                            {"Interest", "ic_interest", "#81D4FA", "Income"},       // Sky Blue
                            {"Investment", "ic_investment", "#BBDEFB", "Income"},   // Soft Blue
                            {"Freelance", "ic_freelance", "#E1BEE7", "Income"},     // Soft Purple
                            {"Refund", "ic_refund", "#C8E6C9", "Income"},           // Balanced Green
                            {"Rental Income", "ic_rent", "#D1C4E9", "Income"},      // Gentle Lavender
                            {"Cashback", "ic_cashback", "#F8BBD0", "Income"},       // Pastel Pink
                            {"Savings", "ic_saving", "#B2EBF2", "Income"},          // Soft Cyan
                            {"Dividends", "ic_dividend", "#C5D3E8", "Income"},      // Pastel Violet
                            {"Pension", "ic_pension", "#B2DFDB", "Income"},         // Pastel Teal
                            {"Side Hustle", "ic_side_hustle", "#F0F1C5", "Income"}, // Pastel Blue

                    };


                    for (String[] entry : defaultCategories) {
                        Category category = new Category();
                        category.user_id = userId;
                        category.name = entry[0];
                        category.icon = entry[1];
                        category.colorHex = entry[2];
                        category.type = entry[3];
                        database.categoryDao().insert(category);
                    }

                    // ✅ Insert Default Icons with type
                    String[][] defaultIcons = {
                            // 💸 Expense-related icons
                            {"Beauty", "ic_beauty", "Expense"},
                            {"Bills", "ic_bills", "Expense"},
                            {"Games", "ic_games", "Expense"},
                            {"Party", "ic_party", "Expense"},
                            {"Hotel", "ic_hotel", "Expense"},
                            {"Food", "ic_food", "Expense"},
                            {"Shopping", "ic_shopping", "Expense"},
                            {"Travel", "ic_travel", "Expense"},
                            {"Transport", "ic_transport", "Expense"},
                            {"Fuel", "ic_fuel", "Expense"},
                            {"Health", "ic_medicine", "Expense"},
                            {"Groceries", "ic_groceries", "Expense"},
                            {"Pets", "ic_pets", "Expense"},
                            {"Education", "ic_education", "Expense"},
                            {"Entertainment", "ic_entertainment", "Expense"},
                            {"Rent", "ic_rent", "Expense"},
                            {"Insurance", "ic_insurance", "Expense"},
                            {"Maintenance", "ic_maintenance", "Expense"},
                            {"Subscriptions", "ic_subscription", "Expense"},
                            {"Charity", "ic_charity", "Expense"},
                            {"Gadgets", "ic_gadgets", "Expense"},
                            {"Stationery", "ic_stationery", "Expense"},
                            {"EMI / Loan", "ic_emi", "Expense"},
                            {"Kids", "ic_kids", "Expense"},
                            {"Electronics", "ic_electronics", "Expense"},
                            {"Home", "ic_home", "Expense"},
                            {"Shopping Bag", "ic_bag", "Expense"},
                            {"Doctor", "ic_doctor", "Expense"},
                            {"Taxi", "ic_taxi", "Expense"},
                            {"Coffee", "ic_coffee", "Expense"},
                            {"Clothes", "ic_clothes", "Expense"},
                            {"Repair", "ic_repair", "Expense"},
                            {"Cleaning", "ic_cleaning", "Expense"},
                            {"Delivery", "ic_delivery", "Expense"},
                            {"Parking", "ic_parking", "Expense"},
                            {"Restaurant", "ic_restaurant", "Expense"},
                            {"Gym", "ic_gym", "Expense"},
                            {"Cigarette", "ic_cigarette", "Expense"},
                            {"Alcohol", "ic_alcohol", "Expense"},
                            {"Fast Food", "ic_fastfood", "Expense"},
                            {"Salon", "ic_salon", "Expense"},
                            {"Shoes", "ic_shoes", "Expense"},
                            {"Watch", "ic_watch", "Expense"},
                            {"Gift Box", "ic_gift", "Expense"},

                            // 💰 Income-related icons
                            {"Salary", "ic_salary", "Income"},
                            {"Gift", "ic_gift", "Income"},
                            {"Business", "ic_business", "Income"},
                            {"Bonus", "ic_bonus", "Income"},
                            {"Interest", "ic_interest", "Income"},
                            {"Investment", "ic_investment", "Income"},
                            {"Freelance", "ic_freelance", "Income"},
                            {"Refund", "ic_refund", "Income"},
                            {"Rental Income", "ic_rent", "Income"},
                            {"Cashback", "ic_cashback", "Income"},
                            {"Savings", "ic_saving", "Income"},
                            {"Dividends", "ic_dividend", "Income"},
                            {"Pension", "ic_pension", "Income"},
                            {"Side Hustle", "ic_side_hustle", "Income"},
                            {"Commission", "ic_commission", "Income"},
                            {"Crypto", "ic_crypto", "Income"},

                            // ⚖️ Both (usable for Expense or Income)
                            {"Star", "ic_star", "Both"},
                            {"Wallet", "ic_wallet", "Both"},
                            {"Bank", "ic_bank", "Both"},
                            {"Transfer", "ic_transfer", "Both"},
                            {"Credit Card", "ic_credit_card", "Both"},
                            {"Debit Card", "ic_card", "Both"},
                            {"Cash", "ic_cash", "Both"},
                            {"UPI", "ic_upi", "Both"},
                            {"Net Banking", "ic_netbanking", "Both"},
                            {"Crypto Wallet", "ic_crypto_wallet", "Both"},
                            {"cheque", "ic_cheque", "Both"},
                            {"Loan", "ic_loan", "Both"},
                            {"Voucher", "ic_voucher", "Both"},
                            {"Calendar", "ic_calendar", "Both"},

                    };


                    for (String[] entry : defaultIcons) {
                        Icon icon = new Icon();
                        icon.displayName = entry[0];
                        icon.drawableName = entry[1];
                        icon.type = entry[2];
                        database.iconDao().insert(icon);
                    }

                    // Suggested Base Colors (medium tones)
                    // Pastel background colors
                    String[] defaultColors = {
                            "#FFE0B2", // Light Orange
                            "#B2EBF2", // Light Cyan
                            "#FFCDD2", // Light Red
                            "#C8E6C9", // Light Green
                            "#C5CAE9", // Light Indigo
                            "#E1BEE7", // Light Purple
                            "#D7CCC8", // Light Brown
                            "#B3E5FC", // Light Blue
                            "#DCEDC8", // Light Lime
                            "#FFCCBC",  // Light Deep Orange

                            "#F8BBD0", // 🌷 Soft Pink
                            "#B2DFDB", // 🧊 Aqua Teal
                            "#F0F4C3", // 🌻 Light Yellow-Green
                            "#D1C4E9", // 🪄 Soft Lavender
                            "#BBDEFB", // 💧 Sky Blue
                            "#FFAB91", // 🍊 Light Coral Orange
                            "#CE93D8", // 💮 Soft Magenta
                            "#A5D6A7", // 🌱 Mint Green
                            "#BCAAA4", // 🪵 Warm Taupe Brown
                            "#F5B7B1", // 🍓 Blush Rose
                            "#AED581", // 🍈 Fresh Green
                            "#FFF3E0", // ☀️ Creamy Peach
                            "#B39DDB", // 🪻 Soft Violet
                            "#80DEEA", // 🌊 Aqua Blue
                            "#E6EE9C", // 🍋 Light Chartreuse
                            "#FFCC80", // 🧡 Warm Peach Orange
                            "#B0BEC5", // ⚙️ Light Steel Gray
                            "#E0F7FA", // 🌬️ Cool Cyan Mist
                            "#FFAB91", // 🍑 Warm Coral
                            "#CFCFCF", // ⚪ Neutral Gray (For balance)
                            "#F3E5F5", // 🌸 Light Lilac — soft, airy pastel purple
                            "#C8E6E0"
                    };


                    for (String colorHex : defaultColors) {
                        ColorEntity color = new ColorEntity();
                        color.hex = colorHex;
                        database.colorDao().insert(color);
                    }
                }
            });


        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            enableTriggers(db); // ✅ ensure correct logic after reopen or restore
        }

    };

    private static Account makeAccount(long userId, String name, String icon, String color, double amount) {
        Account account = new Account();
        account.user_id = userId;
        account.name = name;
        account.icon = icon;
        account.iconColorHex = color;
        account.amount = amount;
        account.created_at = new Date();
        account.updated_at = new Date();
        return account;
    }

    private static void enableTriggers(@NonNull SupportSQLiteDatabase db) {

        // Always ensure foreign key constraints are active
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Drop old triggers
        db.execSQL("DROP TRIGGER IF EXISTS trg_update_account_on_insert;");
        db.execSQL("DROP TRIGGER IF EXISTS trg_update_account_on_update;");
        db.execSQL("DROP TRIGGER IF EXISTS trg_adjust_account_on_subtype_delete;");
        db.execSQL("DROP TRIGGER IF EXISTS trg_adjust_account_on_category_delete;");

        // ✅ 1️⃣ INSERT — when new transaction is added
//        db.execSQL(
//                "CREATE TRIGGER trg_update_account_on_insert " +
//                        "AFTER INSERT ON transactions " +
//                        "WHEN NEW.subtype_id IS NOT NULL " +
//                        "BEGIN " +
//                        "   UPDATE accounts " +
//                        "   SET amount = amount + ( " +
//                        "       CASE " +
//                        "           WHEN NEW.type = 'Income' THEN NEW.amount " +
//                        "           WHEN NEW.type = 'Expense' THEN -NEW.amount " +
//                        "           ELSE 0 " +
//                        "       END " +
//                        "   ) " +
//                        "   WHERE account_id = ( " +
//                        "       SELECT s.account_id FROM subtypes s WHERE s.subtype_id = NEW.subtype_id LIMIT 1 " +
//                        "   ); " +
//                        "END;"
//        );

        db.execSQL(
                "CREATE TRIGGER trg_update_account_on_insert " +
                        "AFTER INSERT ON transactions " +
                        "WHEN NEW.subtype_id IS NOT NULL " +
                        "BEGIN " +
                        "   UPDATE accounts " +
                        "   SET amount = amount + ( " +
                        "       CASE " +
                        "           WHEN NEW.type = 'Income' THEN NEW.amount " +
                        "           WHEN NEW.type = 'Expense' THEN -NEW.amount " +
                        "           WHEN NEW.type = 'TransferDebit' THEN -NEW.amount " +
                        "           WHEN NEW.type = 'TransferCredit' THEN NEW.amount " +
                        "           ELSE 0 " +
                        "       END " +
                        "   ) " +
                        "   WHERE account_id = ( " +
                        "       SELECT s.account_id FROM subtypes s WHERE s.subtype_id = NEW.subtype_id LIMIT 1 " +
                        "   ); " +
                        "END;"
        );


        // ✅ 3️⃣ UPDATE — adjust only the delta if amount/type changes
        db.execSQL(
                "CREATE TRIGGER trg_update_account_on_update " +
                        "AFTER UPDATE ON transactions " +
                        "WHEN NEW.subtype_id IS NOT NULL " +
                        "BEGIN " +

                        // Step 1️⃣ — Undo the OLD effect
                        "   UPDATE accounts " +
                        "   SET amount = amount - ( " +
                        "       CASE " +
                        "           WHEN OLD.type = 'Income' THEN OLD.amount " +
                        "           WHEN OLD.type = 'Expense' THEN -OLD.amount " +
                        "           ELSE 0 END " +
                        "   ) " +
                        "   WHERE account_id = ( " +
                        "       SELECT s.account_id FROM subtypes s WHERE s.subtype_id = OLD.subtype_id LIMIT 1 " +
                        "   ); " +

                        // Step 2️⃣ — Apply the NEW effect
                        "   UPDATE accounts " +
                        "   SET amount = amount + ( " +
                        "       CASE " +
                        "           WHEN NEW.type = 'Income' THEN NEW.amount " +
                        "           WHEN NEW.type = 'Expense' THEN -NEW.amount " +
                        "           ELSE 0 END " +
                        "   ) " +
                        "   WHERE account_id = ( " +
                        "       SELECT s.account_id FROM subtypes s WHERE s.subtype_id = NEW.subtype_id LIMIT 1 " +
                        "   ); " +

                        "END;"
        );


//         ✅ 4️⃣ BEFORE DELETE on Subtype — manually adjust account (since cascade won't fire transaction triggers)
        db.execSQL(
                "CREATE TRIGGER trg_adjust_account_on_subtype_delete " +
                        "BEFORE DELETE ON subtypes " +
                        "BEGIN " +
                        "   UPDATE accounts " +
                        "   SET amount = amount - (" +
                        "       SELECT IFNULL(SUM(CASE " +
                        "           WHEN t.type = 'Income' THEN t.amount " +     // reverse income
                        "           WHEN t.type = 'Expense' THEN -t.amount " +   // reverse expense
                        "           ELSE 0 END), 0) " +
                        "       FROM transactions t WHERE t.subtype_id = OLD.subtype_id" +
                        "   ) " +
                        "   WHERE account_id = OLD.account_id; " +
                        "END;"
        );

//        db.execSQL(
//                "CREATE TRIGGER trg_adjust_account_on_subtype_delete " +
//                        "BEFORE DELETE ON subtypes " +
//                        "BEGIN " +
//                        "   UPDATE accounts " +
//                        "   SET amount = amount - (" +
//                        "       SELECT IFNULL(SUM(CASE " +
//                        "           WHEN t.type = 'Income' THEN t.amount " +           // income added → subtract
//                        "           WHEN t.type = 'Expense' THEN -t.amount " +         // expense subtracted → add back
//                        "           WHEN t.type = 'TransferCredit' THEN t.amount " +   // credit added → subtract back
//                        "           WHEN t.type = 'TransferDebit' THEN -t.amount " +   // debit subtracted → add back
//                        "           ELSE 0 END), 0) " +
//                        "       FROM transactions t " +
//                        "       WHERE t.subtype_id = OLD.subtype_id" +
//                        "   ) " +
//                        "   WHERE account_id = OLD.account_id; " +
//                        "END;"
//        );


        // ✅ 5️⃣ Category Delete — adjust all affected accounts
        db.execSQL(
                "CREATE TRIGGER trg_adjust_account_on_category_delete " +
                        "BEFORE DELETE ON categories " +
                        "BEGIN " +
                        "   UPDATE accounts " +
                        "   SET amount = amount - (" +
                        "       SELECT IFNULL(SUM(CASE " +
                        "           WHEN t.type = 'Income' THEN t.amount " +
                        "           WHEN t.type = 'Expense' THEN -t.amount " +
                        "           ELSE 0 END), 0) " +
                        "       FROM transactions t " +
                        "       JOIN subtypes s ON s.subtype_id = t.subtype_id " +
                        "       WHERE t.category_id = OLD.category_id AND s.account_id = accounts.account_id" +
                        "   ) " +
                        "   WHERE account_id IN (" +
                        "       SELECT DISTINCT s.account_id FROM transactions t " +
                        "       JOIN subtypes s ON s.subtype_id = t.subtype_id " +
                        "       WHERE t.category_id = OLD.category_id" +
                        "   ); " +
                        "END;"
        );

    }


}
