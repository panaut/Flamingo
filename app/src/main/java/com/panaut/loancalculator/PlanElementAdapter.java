package com.panaut.loancalculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.panaut.loancalculator.Loan.PlanElement;
import com.panaut.loancalculator.Loan.PlannedPayment;

import java.text.NumberFormat;
import java.util.List;

/**
 * Created by ivan.cojbasic on 7/16/2016.
 */
public class PlanElementAdapter extends ArrayAdapter<PlanElement> {

    public PlanElementAdapter(Context context, List<PlanElement> payments) {
        super(context, R.layout.payment_plan_row, payments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NumberFormat curFormater = NumberFormat.getCurrencyInstance();

        LayoutInflater myInflater = LayoutInflater.from(getContext());

        PlanElement planElement = getItem(position);

        View rowView = myInflater.inflate(R.layout.payment_plan_row, parent, false);

        if(planElement.getClass() == PlannedPayment.class)
        {
            LinearLayout defaultLayout = (LinearLayout)rowView.findViewById(R.id.regularView);
            defaultLayout.setVisibility(View.VISIBLE);

            LinearLayout otherLayout = (LinearLayout)rowView.findViewById(R.id.yearlyPaybackView);
            otherLayout.setVisibility(View.GONE);

            PlannedPayment plannedPayment = (PlannedPayment)planElement;

            TextView index =  (TextView)defaultLayout.findViewById(R.id.txtIndex);
            index.setText(String.format("%1$d.", plannedPayment.Index));

            TextView instalment =  (TextView)defaultLayout.findViewById(R.id.txtInstalment);
            // instalment.setText(String.format("%1$.2f €", plannedPayment.Instalment));
            instalment.setText(curFormater.format(plannedPayment.Instalment));

            TextView interest =  (TextView)defaultLayout.findViewById(R.id.txtInterest);
            // interest.setText(String.format("%1$.2f €", plannedPayment.InterestPayback));
            interest.setText(curFormater.format(plannedPayment.InterestPayback));

            TextView capitalLeft =  (TextView)defaultLayout.findViewById(R.id.txtCapital);
            // capitalLeft.setText(String.format("%1$.2f €", plannedPayment.CapitalLeft));
            capitalLeft.setText(curFormater.format(plannedPayment.CapitalLeft));
        }
        else {
            LinearLayout defaultLayout = (LinearLayout)rowView.findViewById(R.id.regularView);
            defaultLayout.setVisibility(View.GONE);

            LinearLayout otherLayout = (LinearLayout)rowView.findViewById(R.id.yearlyPaybackView);
            otherLayout.setVisibility(View.VISIBLE);

            TextView instalment =  (TextView)otherLayout.findViewById(R.id.txtYearlyPayback);
            // instalment.setText(String.format("%1$.2f €", planElement.Instalment));
            instalment.setText(curFormater.format(planElement.Instalment));
        }

        return rowView;
    }
}
