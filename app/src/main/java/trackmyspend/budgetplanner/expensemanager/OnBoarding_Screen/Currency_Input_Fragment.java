package trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.google.firebase.analytics.FirebaseAnalytics;

import trackmyspend.budgetplanner.expensemanager.AdManage.PriorityBannerController;
import trackmyspend.budgetplanner.expensemanager.DB.AppDatabase;
import trackmyspend.budgetplanner.expensemanager.DB.entities.User;
import trackmyspend.budgetplanner.expensemanager.MainActivity;
import trackmyspend.budgetplanner.expensemanager.OnBoarding_Screen.adapter.CurrencyAdapter;
import trackmyspend.budgetplanner.expensemanager.R;
import trackmyspend.budgetplanner.expensemanager.Util.CurrencyItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class Currency_Input_Fragment extends Fragment {

    private ListView currencyList;
    private EditText searchInput;
    private View btnSave;
    private ImageView ivBack;
    private AppDatabase db;

    private CurrencyItem selectedCurrency;
    private final List<CurrencyItem> currencyItems = new ArrayList<>();
    private CurrencyAdapter adapter;
    private OnCurrencySavedListener callback;

    private boolean isFromProfile = false;

    public interface OnCurrencySavedListener {
        void onCurrencySaved();
    }

    public Currency_Input_Fragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCurrencySavedListener) {
            callback = (OnCurrencySavedListener) context;
        }
    }

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_currency__input, container, false);

        currencyList = root.findViewById(R.id.currencyList);
        searchInput = root.findViewById(R.id.searchInput);
        btnSave = root.findViewById(R.id.btnSaveCurrency);
        ivBack = root.findViewById(R.id.ivBack);
        View ivBack = root.findViewById(R.id.ivBack);

        FrameLayout bannerContainer = root.findViewById(R.id.banner_container);
        PriorityBannerController.show(
                requireActivity(),
                bannerContainer,
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig(),
                trackmyspend.budgetplanner.expensemanager.AdManage.AdsManager.getConfig().get("banner_type_small")
        );

        // ✅ Detect entry point
        if (getArguments() != null) {
            isFromProfile = getArguments().getBoolean("fromProfile", false);
        }

        // ✅ Show back button if from Profile
        if (isFromProfile) {
            ivBack.setVisibility(View.VISIBLE);
            ivBack.setOnClickListener(v -> requireActivity().onBackPressed());
        }

        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "expense_manager_db")
                .fallbackToDestructiveMigration()
                .build();

        setupCurrencyList();

        adapter = new CurrencyAdapter(requireContext(), currencyItems, item -> {
            selectedCurrency = item;
            TextView saveText = (TextView) btnSave;

            // Enable button (auto applies active background + white text)
            saveText.setEnabled(true);
        });
        currencyList.setAdapter(adapter);



        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

// ✅ When user clears text, also hide keyboard
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm = (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });



        btnSave.setOnClickListener(v -> {
            if (selectedCurrency == null) {
                Toast.makeText(requireContext(), "Please select a currency.", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserCurrency(selectedCurrency, v);
        });


        return root;
    }


    private void updateUserCurrency(CurrencyItem currency, View view) {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        String mode = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? "Dark" : "Light";

        Executors.newSingleThreadExecutor().execute(() -> {
            User user = db.userDao().getFirstUser();
            if (user != null) {
                user.currency_name = currency.currencyName;
                user.currency_code = currency.code;
                user.currency_symbol = currency.symbol;
                user.locale_tag = currency.localeTag;
                user.mode = mode;
                user.updated_at = new Date();
                db.userDao().update(user);
            }

            // ✅ Return to main thread for next UI action
            requireActivity().runOnUiThread(() -> {
                if (isFromProfile) {
                    // ⚠️ Show warning dialog before applying currency change
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Change Currency")
                            .setMessage("Changing the currency will only update the symbol — your existing amounts will NOT be converted based on exchange rates.")
                            .setCancelable(false)
                            .setPositiveButton("Continue", (dialog, which) -> {
                                dialog.dismiss();
                                applyCurrencyChange(); // proceed
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    // 🆕 Onboarding → directly go to MainActivity
                    applyCurrencyChangeOnBoard();
                }
            });
        });
    }


    private void applyCurrencyChange() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(300); // Wait for DB update
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Currency set successfully!", Toast.LENGTH_SHORT).show();

                if (callback != null) callback.onCurrencySaved();

                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }

    private void applyCurrencyChangeOnBoard() {

        FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(requireContext());
        analytics.logEvent("onboarding_btn", null);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(300); // Wait for DB update
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Currency set successfully!", Toast.LENGTH_SHORT).show();

                if (callback != null) callback.onCurrencySaved();


                Intent intent = new Intent(requireContext(), Allow_Notification_Activity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        });
    }



    private void setupCurrencyList() {
        currencyItems.clear();

        currencyItems.add(new CurrencyItem("؋", "AFN", "Afghan Afghani", "fa_AF"));
        currencyItems.add(new CurrencyItem("د.ج", "DZD", "Algerian Dinar", "ar_DZ"));
        currencyItems.add(new CurrencyItem("֏", "AMD", "Armenian Dram", "hy_AM"));
        currencyItems.add(new CurrencyItem("₼", "AZN", "Azerbaijani Manat", "az_AZ"));
        currencyItems.add(new CurrencyItem("৳", "BDT", "Bangladeshi Taka", "bn_BD"));
        currencyItems.add(new CurrencyItem("Nu.", "BTN", "Bhutanese Ngultrum", "dz_BT"));
        currencyItems.add(new CurrencyItem("₮", "MNT", "Mongolian Tögrög", "mn_MN"));
        currencyItems.add(new CurrencyItem("₺", "TRY", "Turkish Lira", "tr_TR"));
        currencyItems.add(new CurrencyItem("₹", "INR", "Indian Rupee", "en_IN"));
        currencyItems.add(new CurrencyItem("₨", "PKR", "Pakistani Rupee", "en_PK"));
        currencyItems.add(new CurrencyItem("₨", "LKR", "Sri Lankan Rupee", "si_LK"));
        currencyItems.add(new CurrencyItem("₨", "NPR", "Nepalese Rupee", "ne_NP"));
        currencyItems.add(new CurrencyItem("؋", "AFN", "Afghan Afghani", "fa_AF"));
        currencyItems.add(new CurrencyItem("﷼", "IRR", "Iranian Rial", "fa_IR"));
        currencyItems.add(new CurrencyItem("﷼", "IQD", "Iraqi Dinar", "ar_IQ"));
        currencyItems.add(new CurrencyItem("﷼", "SAR", "Saudi Riyal", "ar_SA"));
        currencyItems.add(new CurrencyItem("د.إ", "AED", "UAE Dirham", "ar_AE"));
        currencyItems.add(new CurrencyItem("ر.ع.", "OMR", "Omani Rial", "ar_OM"));
        currencyItems.add(new CurrencyItem("ر.ق", "QAR", "Qatari Riyal", "ar_QA"));
        currencyItems.add(new CurrencyItem("د.ك", "KWD", "Kuwaiti Dinar", "ar_KW"));
        currencyItems.add(new CurrencyItem("₪", "ILS", "Israeli New Shekel", "he_IL"));
        currencyItems.add(new CurrencyItem("¥", "JPY", "Japanese Yen", "ja_JP"));
        currencyItems.add(new CurrencyItem("₩", "KRW", "South Korean Won", "ko_KR"));
        currencyItems.add(new CurrencyItem("₫", "VND", "Vietnamese Dong", "vi_VN"));
        currencyItems.add(new CurrencyItem("₭", "LAK", "Lao Kip", "lo_LA"));
        currencyItems.add(new CurrencyItem("៛", "KHR", "Cambodian Riel", "km_KH"));
        currencyItems.add(new CurrencyItem("₱", "PHP", "Philippine Peso", "en_PH"));
        currencyItems.add(new CurrencyItem("฿", "THB", "Thai Baht", "th_TH"));
        currencyItems.add(new CurrencyItem("₫", "VND", "Vietnamese Dong", "vi_VN"));
        currencyItems.add(new CurrencyItem("₭", "LAK", "Lao Kip", "lo_LA"));
        currencyItems.add(new CurrencyItem("₮", "MNT", "Mongolian Tögrög", "mn_MN"));
        currencyItems.add(new CurrencyItem("₸", "KZT", "Kazakhstani Tenge", "kk_KZ"));
        currencyItems.add(new CurrencyItem("сом", "KGS", "Kyrgyzstani Som", "ky_KG"));
        currencyItems.add(new CurrencyItem("₴", "UAH", "Ukrainian Hryvnia", "uk_UA"));
        currencyItems.add(new CurrencyItem("₼", "AZN", "Azerbaijani Manat", "az_AZ"));
        currencyItems.add(new CurrencyItem("₾", "GEL", "Georgian Lari", "ka_GE"));
        currencyItems.add(new CurrencyItem("₮", "MNT", "Mongolian Tögrög", "mn_MN"));
        currencyItems.add(new CurrencyItem("₸", "KZT", "Kazakhstani Tenge", "kk_KZ"));
        currencyItems.add(new CurrencyItem("₺", "TRY", "Turkish Lira", "tr_TR"));
        currencyItems.add(new CurrencyItem("₾", "GEL", "Georgian Lari", "ka_GE"));
        currencyItems.add(new CurrencyItem("₮", "MNT", "Mongolian Tögrög", "mn_MN"));
        currencyItems.add(new CurrencyItem("₩", "KRW", "South Korean Won", "ko_KR"));
        currencyItems.add(new CurrencyItem("¥", "JPY", "Japanese Yen", "ja_JP"));
        currencyItems.add(new CurrencyItem("₫", "VND", "Vietnamese Dong", "vi_VN"));
        currencyItems.add(new CurrencyItem("₭", "LAK", "Lao Kip", "lo_LA"));
        currencyItems.add(new CurrencyItem("៛", "KHR", "Cambodian Riel", "km_KH"));
        currencyItems.add(new CurrencyItem("₱", "PHP", "Philippine Peso", "en_PH"));
        currencyItems.add(new CurrencyItem("₮", "MNT", "Mongolian Tögrög", "mn_MN"));
// 🇪🇺 Europe
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro", "de_DE"));
        currencyItems.add(new CurrencyItem("£", "GBP", "British Pound", "en_GB"));
        currencyItems.add(new CurrencyItem("₣", "CHF", "Swiss Franc", "de_CH"));
        currencyItems.add(new CurrencyItem("zł", "PLN", "Polish Zloty", "pl_PL"));
        currencyItems.add(new CurrencyItem("kr", "SEK", "Swedish Krona", "sv_SE"));
        currencyItems.add(new CurrencyItem("kr", "NOK", "Norwegian Krone", "nb_NO"));
        currencyItems.add(new CurrencyItem("kr", "DKK", "Danish Krone", "da_DK"));
        currencyItems.add(new CurrencyItem("Ft", "HUF", "Hungarian Forint", "hu_HU"));
        currencyItems.add(new CurrencyItem("Kč", "CZK", "Czech Koruna", "cs_CZ"));
        currencyItems.add(new CurrencyItem("лв", "BGN", "Bulgarian Lev", "bg_BG"));
        currencyItems.add(new CurrencyItem("lei", "RON", "Romanian Leu", "ro_RO"));
        currencyItems.add(new CurrencyItem("₴", "UAH", "Ukrainian Hryvnia", "uk_UA"));
        currencyItems.add(new CurrencyItem("ден", "MKD", "Macedonian Denar", "mk_MK"));
        currencyItems.add(new CurrencyItem("₽", "RUB", "Russian Ruble", "ru_RU"));
        currencyItems.add(new CurrencyItem("kn", "HRK", "Croatian Kuna", "hr_HR"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Ireland)", "en_IE"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Portugal)", "pt_PT"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Spain)", "es_ES"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Italy)", "it_IT"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (France)", "fr_FR"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Netherlands)", "nl_NL"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Belgium)", "fr_BE"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Finland)", "fi_FI"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Greece)", "el_GR"));
        currencyItems.add(new CurrencyItem("€", "EUR", "Euro (Austria)", "de_AT"));

// 🌍 Africa
        currencyItems.add(new CurrencyItem("₦", "NGN", "Nigerian Naira", "en_NG"));
        currencyItems.add(new CurrencyItem("R", "ZAR", "South African Rand", "en_ZA"));
        currencyItems.add(new CurrencyItem("USh", "UGX", "Ugandan Shilling", "en_UG"));
        currencyItems.add(new CurrencyItem("KSh", "KES", "Kenyan Shilling", "en_KE"));
        currencyItems.add(new CurrencyItem("TSh", "TZS", "Tanzanian Shilling", "sw_TZ"));
        currencyItems.add(new CurrencyItem("₵", "GHS", "Ghanaian Cedi", "en_GH"));
        currencyItems.add(new CurrencyItem("₦", "NGN", "Nigerian Naira", "en_NG"));
        currencyItems.add(new CurrencyItem("Le", "SLL", "Sierra Leonean Leone", "en_SL"));
        currencyItems.add(new CurrencyItem("₨", "MUR", "Mauritian Rupee", "en_MU"));
        currencyItems.add(new CurrencyItem("₨", "SCR", "Seychellois Rupee", "en_SC"));
        currencyItems.add(new CurrencyItem("E", "SZL", "Eswatini Lilangeni", "en_SZ"));
        currencyItems.add(new CurrencyItem("P", "BWP", "Botswana Pula", "en_BW"));
        currencyItems.add(new CurrencyItem("N$", "NAD", "Namibian Dollar", "en_NA"));
        currencyItems.add(new CurrencyItem("ZK", "ZMW", "Zambian Kwacha", "en_ZM"));
        currencyItems.add(new CurrencyItem("₲", "RWF", "Rwandan Franc", "rw_RW"));
        currencyItems.add(new CurrencyItem("₣", "BIF", "Burundian Franc", "fr_BI"));
        currencyItems.add(new CurrencyItem("₣", "CDF", "Congolese Franc", "fr_CD"));
        currencyItems.add(new CurrencyItem("₣", "XOF", "West African CFA Franc", "fr_SN"));
        currencyItems.add(new CurrencyItem("₣", "XAF", "Central African CFA Franc", "fr_CM"));
        currencyItems.add(new CurrencyItem("₣", "DJF", "Djiboutian Franc", "fr_DJ"));
        currencyItems.add(new CurrencyItem("Br", "ETB", "Ethiopian Birr", "am_ET"));
        currencyItems.add(new CurrencyItem("Sh", "SOS", "Somali Shilling", "so_SO"));
        currencyItems.add(new CurrencyItem("£", "SDG", "Sudanese Pound", "ar_SD"));
        currencyItems.add(new CurrencyItem("£", "SSP", "South Sudanese Pound", "en_SS"));
        currencyItems.add(new CurrencyItem("ج.م", "EGP", "Egyptian Pound", "ar_EG"));
        currencyItems.add(new CurrencyItem("د.ت", "TND", "Tunisian Dinar", "ar_TN"));
        currencyItems.add(new CurrencyItem("د.ج", "DZD", "Algerian Dinar", "ar_DZ"));
        currencyItems.add(new CurrencyItem("د.م.", "MAD", "Moroccan Dirham", "ar_MA"));
        currencyItems.add(new CurrencyItem("₣", "GNF", "Guinean Franc", "fr_GN"));
        currencyItems.add(new CurrencyItem("₣", "XPF", "CFP Franc (Polynesia)", "fr_PF"));
        currencyItems.add(new CurrencyItem("₣", "KMF", "Comorian Franc", "fr_KM"));
        currencyItems.add(new CurrencyItem("₣", "MGF", "Malagasy Ariary", "mg_MG"));
        currencyItems.add(new CurrencyItem("₣", "MGA", "Malagasy Ariary", "mg_MG"));
        currencyItems.add(new CurrencyItem("₣", "LSL", "Lesotho Loti", "en_LS"));
        currencyItems.add(new CurrencyItem("₣", "MZN", "Mozambican Metical", "pt_MZ"));
        currencyItems.add(new CurrencyItem("₣", "AOA", "Angolan Kwanza", "pt_AO"));
        currencyItems.add(new CurrencyItem("₣", "GMD", "Gambian Dalasi", "en_GM"));
        currencyItems.add(new CurrencyItem("₣", "LRD", "Liberian Dollar", "en_LR"));
        currencyItems.add(new CurrencyItem("₣", "MWK", "Malawian Kwacha", "en_MW"));
        currencyItems.add(new CurrencyItem("₣", "SHP", "Saint Helena Pound", "en_SH"));
        currencyItems.add(new CurrencyItem("₣", "STD", "São Tomé and Príncipe Dobra", "pt_ST"));
// 🇺🇸 North & Central America
        currencyItems.add(new CurrencyItem("$", "USD", "United States Dollar", "en_US"));
        currencyItems.add(new CurrencyItem("$", "CAD", "Canadian Dollar", "en_CA"));
        currencyItems.add(new CurrencyItem("$", "MXN", "Mexican Peso", "es_MX"));
        currencyItems.add(new CurrencyItem("$", "BZD", "Belize Dollar", "en_BZ"));
        currencyItems.add(new CurrencyItem("$", "BBD", "Barbados Dollar", "en_BB"));
        currencyItems.add(new CurrencyItem("$", "BSD", "Bahamian Dollar", "en_BS"));
        currencyItems.add(new CurrencyItem("$", "BMD", "Bermudian Dollar", "en_BM"));
        currencyItems.add(new CurrencyItem("$", "KYD", "Cayman Islands Dollar", "en_KY"));
        currencyItems.add(new CurrencyItem("$", "XCD", "East Caribbean Dollar", "en_AG"));
        currencyItems.add(new CurrencyItem("$", "JMD", "Jamaican Dollar", "en_JM"));
        currencyItems.add(new CurrencyItem("$", "TTD", "Trinidad and Tobago Dollar", "en_TT"));
        currencyItems.add(new CurrencyItem("$", "HTG", "Haitian Gourde", "ht_HT"));
        currencyItems.add(new CurrencyItem("₡", "CRC", "Costa Rican Colón", "es_CR"));
        currencyItems.add(new CurrencyItem("Q", "GTQ", "Guatemalan Quetzal", "es_GT"));
        currencyItems.add(new CurrencyItem("L", "HNL", "Honduran Lempira", "es_HN"));
        currencyItems.add(new CurrencyItem("C$", "NIO", "Nicaraguan Córdoba", "es_NI"));
        currencyItems.add(new CurrencyItem("B/.", "PAB", "Panamanian Balboa", "es_PA"));
        currencyItems.add(new CurrencyItem("$", "USD", "United States Dollar (Panama)", "es_PA"));
        currencyItems.add(new CurrencyItem("$", "DOP", "Dominican Peso", "es_DO"));
        currencyItems.add(new CurrencyItem("$", "CUP", "Cuban Peso", "es_CU"));
        currencyItems.add(new CurrencyItem("CUC$", "CUC", "Cuban Convertible Peso", "es_CU"));

// 🇧🇷 South America
        currencyItems.add(new CurrencyItem("R$", "BRL", "Brazilian Real", "pt_BR"));
        currencyItems.add(new CurrencyItem("$", "ARS", "Argentine Peso", "es_AR"));
        currencyItems.add(new CurrencyItem("$", "CLP", "Chilean Peso", "es_CL"));
        currencyItems.add(new CurrencyItem("$", "COP", "Colombian Peso", "es_CO"));
        currencyItems.add(new CurrencyItem("S/.", "PEN", "Peruvian Sol", "es_PE"));
        currencyItems.add(new CurrencyItem("Bs.", "BOB", "Bolivian Boliviano", "es_BO"));
        currencyItems.add(new CurrencyItem("$U", "UYU", "Uruguayan Peso", "es_UY"));
        currencyItems.add(new CurrencyItem("₲", "PYG", "Paraguayan Guarani", "es_PY"));
        currencyItems.add(new CurrencyItem("$", "GYD", "Guyanese Dollar", "en_GY"));
        currencyItems.add(new CurrencyItem("$", "SRD", "Surinamese Dollar", "nl_SR"));
        currencyItems.add(new CurrencyItem("$", "VEF", "Venezuelan Bolívar", "es_VE"));
        currencyItems.add(new CurrencyItem("$", "USD", "United States Dollar (Ecuador)", "es_EC"));

// 🇦🇺 Oceania & Pacific
        currencyItems.add(new CurrencyItem("A$", "AUD", "Australian Dollar", "en_AU"));
        currencyItems.add(new CurrencyItem("NZ$", "NZD", "New Zealand Dollar", "en_NZ"));
        currencyItems.add(new CurrencyItem("K", "PGK", "Papua New Guinean Kina", "en_PG"));
        currencyItems.add(new CurrencyItem("S$", "SGD", "Singapore Dollar", "en_SG"));
        currencyItems.add(new CurrencyItem("WS$", "WST", "Samoan Tala", "sm_WS"));
        currencyItems.add(new CurrencyItem("FJ$", "FJD", "Fijian Dollar", "en_FJ"));
        currencyItems.add(new CurrencyItem("T$", "TOP", "Tongan Paʻanga", "to_TO"));
        currencyItems.add(new CurrencyItem("VT", "VUV", "Vanuatu Vatu", "bi_VU"));
        currencyItems.add(new CurrencyItem("K", "PGK", "Papua New Guinean Kina", "en_PG"));
        currencyItems.add(new CurrencyItem("$", "SBD", "Solomon Islands Dollar", "en_SB"));
        currencyItems.add(new CurrencyItem("₣", "XPF", "CFP Franc (Pacific Franc)", "fr_PF"));
        currencyItems.add(new CurrencyItem("$", "TVD", "Tuvaluan Dollar", "en_TV"));
        currencyItems.add(new CurrencyItem("$", "NAD", "Nauruan Dollar (uses AUD)", "en_NR"));
        currencyItems.add(new CurrencyItem("$", "AUD", "Australian Dollar (Norfolk Island)", "en_NF"));
        currencyItems.add(new CurrencyItem("$", "AUD", "Australian Dollar (Kiribati)", "en_KI"));
        currencyItems.add(new CurrencyItem("$", "AUD", "Australian Dollar (Tuvalu)", "en_TV"));
        currencyItems.add(new CurrencyItem("$", "AUD", "Australian Dollar (Tokelau)", "en_TK"));
        currencyItems.add(new CurrencyItem("$", "AUD", "Australian Dollar (Cocos Islands)", "en_CC"));
        currencyItems.add(new CurrencyItem("$", "AUD", "Australian Dollar (Christmas Island)", "en_CX"));


        currencyItems.sort((a, b) -> a.currencyName.compareToIgnoreCase(b.currencyName));
    }
}
