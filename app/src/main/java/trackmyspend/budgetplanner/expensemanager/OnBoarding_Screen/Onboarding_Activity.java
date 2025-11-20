package trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import trackmyspend.budgetplanner.expensemanager.MainActivity;
import trackmyspend.budgetplanner.expensemanager.R;

public class Onboarding_Activity extends AppCompatActivity
        implements Name_Input_Fragment.OnNameSavedListener,
        Currency_Input_Fragment.OnCurrencySavedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        String step = getIntent().getStringExtra("step");

        if ("currency".equals(step)) {
            loadFragment(new Currency_Input_Fragment(), false);
        } else {
            loadFragment(new Name_Input_Fragment(), false);
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        tx.replace(R.id.fragmentContainer, fragment);
        if (addToBackstack) tx.addToBackStack(null);
        tx.commit();
    }

    @Override
    public void onNameSaved() {
        // After name step → go to currency step
        loadFragment(new Currency_Input_Fragment(), true);
    }

    @Override
    public void onCurrencySaved() {
        // After currency step → launch main activity
        startActivity(new Intent(this, Allow_Notification_Activity.class));
        finish();
    }
}
