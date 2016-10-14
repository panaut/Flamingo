package com.panaut.loancalculator;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.panaut.loancalculator.Loan.Loan;
import com.panaut.loancalculator.Loan.PrematurePayment;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private enum CalculationMode
    {
        GIVEN_PERIOD,
        GIVEN_INSTALMENT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void calculationTypeChanged(View view)
    {
        CalculationMode calculationMode = getCalculationMode();

        ShowParameterSetLayout(calculationMode);
    }

    private CalculationMode getCalculationMode() {
        boolean modePeriod = ((RadioButton)findViewById(R.id.rbLayoutCalculationModePeriod)).isChecked();

        CalculationMode calculationMode;

        if(modePeriod)
            calculationMode = CalculationMode.GIVEN_PERIOD;
        else
            calculationMode = CalculationMode.GIVEN_INSTALMENT;

        return calculationMode;
    }

    private void ShowParameterSetLayout(CalculationMode calculationMode)
    {
        LinearLayout parameterLayout;
        LinearLayout otherLayout;

        if(calculationMode == CalculationMode.GIVEN_INSTALMENT) {
            parameterLayout = (LinearLayout) findViewById(R.id.layoutSetInstalment);
            otherLayout = (LinearLayout) findViewById(R.id.layoutSetPeriodCount);
        }
        else
        {
            otherLayout = (LinearLayout) findViewById(R.id.layoutSetInstalment);
            parameterLayout = (LinearLayout) findViewById(R.id.layoutSetPeriodCount);
        }

        parameterLayout.setVisibility(View.VISIBLE);
        otherLayout.setVisibility(View.GONE);
    }

    public void additionalPaymentModeChanged(View view)
    {
        boolean usePP = ((RadioButton)findViewById(R.id.rbLayoutPrematurePaybackModeYes)).isChecked();

        LinearLayout layout = (LinearLayout) findViewById(R.id.layoutPrematurePayback);

        if(usePP)
            layout.setVisibility(View.VISIBLE);
        else
            layout.setVisibility(View.GONE);
    }

    public void displayDetails(View view)
    {
        OperationResult<Loan> result = CalculateLoan(true);
        displayOperationResult(result);
    }

    public void displayOverview(View view)
    {
        OperationResult<Loan> result = CalculateLoan(false);
        displayOperationResultAlert(result);
    }

    private void displayOperationResult(OperationResult<Loan> result)
    {
        if(result.isSuccess()) {

            Loan loan = result.getResultObject();

            Intent intent = new Intent(this, PaymentPlanActivity.class);
            intent.putExtra("loan", loan);

            startActivity(intent);

        } else {
            Toast.makeText(
                    getApplicationContext(),
                    result.errorMessage(getApplicationContext()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void displayOperationResultAlert(OperationResult<Loan> result)
    {
        if(result.isSuccess()) {

            Loan loan = result.getResultObject();

            String messageFormat = getString(R.string.mainActivity_loanOverview);

            NumberFormat curFormatter = NumberFormat.getCurrencyInstance();

            String strCapital = curFormatter.format(loan.Capital);
            String strInstalment = curFormatter.format(loan.initialInstalment);
            String strTotalInterest = curFormatter.format(loan.TotalInterest);

            String message =
                    String.format(
                            messageFormat,
                            strCapital,
                            loan.initialInstalmentCount,
                            strInstalment,
                            strTotalInterest,
                            loan.TotalInterestInPercent);

            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage(message);
            dlgAlert.setTitle(getString(R.string.mainActivity_loanOverview_Title));
            dlgAlert.setPositiveButton("OK", null);
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    result.errorMessage(getApplicationContext()),
                    Toast.LENGTH_LONG).show();
        }
    }

    private OperationResult CalculateLoan(boolean allowRecalculations)
    {
        OperationResult<Loan> result = null;

        double capital = 0;
        String strCapital = ((TextView)findViewById(R.id.txtCapital)).getText().toString();
        if(TextUtils.isEmpty(strCapital)) {
            result = new OperationResult<>(false, null, 1001);
        }
        else
            capital = Double.parseDouble(strCapital);

        double participation = 0;
        String strParticipation = ((TextView)findViewById(R.id.txtParticipation)).getText().toString();
        if(TextUtils.isEmpty(strParticipation)) {
            if(result == null)
                result = new OperationResult<>(false, null, 1008);
        }
        else if(Double.parseDouble(strParticipation) < 0 || Double.parseDouble(strParticipation) >= 100)
        {
            if(result == null)
                result = new OperationResult<>(false, null, 1009);
        }
        else {
            participation = Double.parseDouble(strParticipation);
            capital = capital * (100 - participation) / 100;
        }

        double interest = 0;
        String strInterest = ((TextView)findViewById(R.id.txtInterest)).getText().toString();
        if(TextUtils.isEmpty(strInterest)) {
            if(result == null)
                result = new OperationResult<>(false, null, 1004);
        }
        else
            interest = Double.parseDouble(strInterest);

        int periodCount = 0;
        String strPeriodCount = ((TextView)findViewById(R.id.txtPeriodCount)).getText().toString();
        double instalment = 0;
        String strInstalment = ((TextView)findViewById(R.id.txtInstalment)).getText().toString();

        if(getCalculationMode() == CalculationMode.GIVEN_PERIOD) {
            if (TextUtils.isEmpty(strPeriodCount)) {
                if(result == null)
                    result = new OperationResult<>(false, null, 1002);
            } else
                periodCount = Integer.parseInt(strPeriodCount);
        } else {
            if (TextUtils.isEmpty(strInstalment)) {
                if(result == null)
                    result = new OperationResult<>(false, null, 1003);
            } else
                instalment = Integer.parseInt(strInstalment);
        }

        boolean doRecalculate;
        RadioButton rbDoRecalculate = (RadioButton)findViewById(R.id.rbLayoutPrematurePaybackModeYes);
        if(rbDoRecalculate.isChecked())
            doRecalculate = true;
        else
            doRecalculate = false;

        if (!allowRecalculations && doRecalculate && result == null)
            result = new OperationResult<>(false, null, 1007);

        double yearlyPayback = 0;
        if(doRecalculate)
        {
            String strYearlyPayback = ((TextView)findViewById(R.id.txtYearlyPayback)).getText().toString();
            if(TextUtils.isEmpty(strYearlyPayback)) {
                if(result == null)
                    result = new OperationResult<>(false, null, 1005);
            }
            else
                yearlyPayback = Double.parseDouble(strYearlyPayback);
        }

        Loan.RecalculationMode recalculationMode;
        RadioButton rbRecalculateWithConstantPeriod = (RadioButton)findViewById(R.id.rbPPConstantPeriod);
        if(rbRecalculateWithConstantPeriod.isChecked())
            recalculationMode = Loan.RecalculationMode.CONSTANT_INTERVAL_COUNT;
        else
            recalculationMode = Loan.RecalculationMode.CONSTANT_INSTALMENT;

        Loan loan = null;

        if(result == null && getCalculationMode() == CalculationMode.GIVEN_PERIOD) {
            try {
                if (doRecalculate) {
                    List<PrematurePayment> paybacks = generatePaybacks(periodCount, yearlyPayback);

                    loan = Loan.CalculatePaymentPlan(
                            capital,
                            periodCount,
                            interest,
                            paybacks,
                            recalculationMode);
                } else {
                    loan = Loan.CalculatePaymentPlan(capital, periodCount, interest);
                }
                result = new OperationResult<>(true, loan, null);
            } catch (Exception ex) {
                result = new OperationResult<>(false, null, 1006);
            }
        }
        else if(result == null && getCalculationMode() == CalculationMode.GIVEN_INSTALMENT) {
            try {
                if (doRecalculate) {

                    periodCount = Loan.FindIntervalCount(
                            capital,
                            360,
                            0,
                            instalment,
                            interest);

                    List<PrematurePayment> paybacks = generatePaybacks(periodCount, yearlyPayback);

                    loan = Loan.CalculatePaymentPlan(
                            capital,
                            instalment,
                            interest,
                            paybacks,
                            recalculationMode);
                } else {
                    loan = Loan.CalculatePaymentPlan(capital, instalment, interest);
                }

                result = new OperationResult<>(true, loan, null);
            } catch (Exception ex) {
                result = new OperationResult<>(false, null, 1006);
            }
        }

        return result;
    }

    private static List<PrematurePayment> generatePaybacks(int periodCount, double yearlyPayback)
    {
        // Prepare 'Yearly paybacks'
        List<PrematurePayment> yearlyPayments = new ArrayList<>();

        for (int i = 12; i <= periodCount; i += 12)
        {
            final int curInd = i;
            final double yPayBck = yearlyPayback;

            PrematurePayment payback = new PrematurePayment(){{
                Index = curInd;
                Instalment = yPayBck; }};

            yearlyPayments.add(payback);
        }

        return yearlyPayments;
    }
}
