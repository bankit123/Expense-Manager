package trackmyspend.budgetplanner.expensemanager.Menu_Screen.Reward_Screen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import trackmyspend.budgetplanner.expensemanager.R;

public class RewardFragment extends Fragment {

    public RewardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reward, container, false);



        return view;
    }
}