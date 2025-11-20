package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reminder_Screen.Workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class EMICheckWorker extends Worker {

    public EMICheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
//        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
//        EMIDao emiDao = db.emiDao();
//        TransactionDao txnDao = db.transactionDao();
//
//        Date today = new Date();
//        List<EMI> dueEmis = emiDao.getDueEmis(today);
//
//        for (EMI emi : dueEmis) {
//            // Safety: if already completed, skip
//            if (emi.paid_installments >= emi.tenure_months) {
//                emi.status = "Completed";
//                emiDao.update(emi);
//                continue;
//            }
//
//            // 1) Insert auto transaction
//            Transaction txn = new Transaction();
//            txn.user_id = 1; // TODO: derive user_id as you do elsewhere
//            txn.amount = emi.emi_amount;
//            txn.notes = "Auto EMI Payment - " + emi.loan_name + " (" + emi.lender_name + ")";
//            txn.date = today;
//            txn.type = "Expense";
//            txn.created_at = today;
//            txn.updated_at = today;
//            txnDao.insert(txn);
//
//            // 2) Update EMI
//            emi.paid_installments++;
//
//            // Advance next_due_date by 1 month
//            Calendar due = Calendar.getInstance();
//            if (emi.next_due_date != null) {
//                due.setTime(emi.next_due_date);
//            } else if (emi.start_date != null) {
//                due.setTime(emi.start_date);
//                due.add(Calendar.MONTH, emi.paid_installments);
//            } else {
//                due.setTime(today);
//            }
//            due.add(Calendar.MONTH, 1);
//            emi.next_due_date = due.getTime();
//
//            // Completed?
//            if (emi.paid_installments >= emi.tenure_months) {
//                emi.status = "Completed";
//            }
//
//            emiDao.update(emi);
//        }

        return Result.success();
    }
}
