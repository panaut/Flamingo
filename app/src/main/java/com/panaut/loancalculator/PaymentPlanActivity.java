package com.panaut.loancalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.panaut.loancalculator.Loan.Loan;
import com.panaut.loancalculator.Loan.PlanElement;

import java.text.NumberFormat;

public class PaymentPlanActivity extends AppCompatActivity {

    private Loan loan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_plan);

        loan = (Loan)getIntent().getSerializableExtra("loan");

        RelativeLayout loanDetails = (RelativeLayout)findViewById(R.id.loanDetails);

        NumberFormat curFormater = NumberFormat.getCurrencyInstance();

        TextView tView;

        tView = (TextView)loanDetails.findViewById(R.id.txtLdCapital);
        // tView.setText(String.format("%1$.0f €", loan.Capital));
        tView.setText(curFormater.format(loan.Capital));

        tView = (TextView)loanDetails.findViewById(R.id.txtLdPeriod);
        tView.setText(String.format("%1$d", loan.finalInstalmentCount));

        tView = (TextView)loanDetails.findViewById(R.id.txtLdInterestRate);
        // tView.setText(String.format("%1$.2f %%", loan.InterestPA));
        tView.setText(curFormater.format(loan.InterestPA));

        tView = (TextView)loanDetails.findViewById(R.id.txtLdInterest);
        // tView.setText(String.format("%1$.2f %%", loan.TotalInterestInPercent));
        tView.setText(String.format("%1$.2f %%", loan.TotalInterestInPercent));

        ListAdapter planElementAdapter = new PlanElementAdapter(this, loan.PaymentPlan);
        ListView listView = (ListView) findViewById(R.id.lvPaymentPlan);
        listView.setAdapter(planElementAdapter);

        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener()
                {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        PlanElement planElement = (PlanElement)parent.getItemAtPosition(position);
                        Toast.makeText(PaymentPlanActivity.this, Double.toString(planElement.Instalment), Toast.LENGTH_LONG);

                        return true;
                    }
                }
        );

        registerForContextMenu(listView);

        listView.setOnCreateContextMenuListener(
                new AdapterView.OnCreateContextMenuListener()
                {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        ListView listView = (ListView)v;
                        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
                        PlanElement planElement = (PlanElement)listView.getItemAtPosition(acmi.position);

                        String menuItem = String.format("Add partial payback at index %1$d", planElement.Index - 1);
                        menu.add(menuItem);
                        menuItem = String.format("Change interest rate for items %1$d", planElement.Index - 1);
                        menu.add(menuItem);
                    }
                }
        );
    }
}
