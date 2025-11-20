package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Home.Adapter;

import java.util.Date;

public class DateHeader {
    public Date date;
    public double incomeTotal;
    public double expenseTotal;

    public DateHeader(Date date, double incomeTotal, double expenseTotal) {
        this.date = date;
        this.incomeTotal = incomeTotal;
        this.expenseTotal = expenseTotal;
    }
}