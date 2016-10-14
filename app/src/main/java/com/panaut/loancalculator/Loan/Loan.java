package com.panaut.loancalculator.Loan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ivan.cojbasic on 7/14/2016.
 */
public class Loan implements Serializable {

    private Loan()
    {

    }

    public enum RecalculationMode
    {
        CONSTANT_INSTALMENT,
        CONSTANT_INTERVAL_COUNT
    }

    public double initialInstalment;

    public int initialInstalmentCount;

    public int finalInstalmentCount;

    public double Capital;

    public double InterestPA;

    public double TotalPayback;

    public double TotalInterest;

    public double TotalInterestInPercent;

    public List<PlanElement> PaymentPlan;

    public static Loan CalculatePaymentPlan(
            double capital,
            double instalment,
            double interestPA)
    {
        return CalculatePaymentPlan(
                capital,
                instalment,
                interestPA,
                null,
                RecalculationMode.CONSTANT_INSTALMENT );
    }

    public static Loan CalculatePaymentPlan(
            double capital,
            double instalment,
            double interestPA,
            List<PrematurePayment> prematurePayments,
            RecalculationMode mode)
    {
        int intervalCount = FindIntervalCount(capital, 360, 0, instalment, interestPA / 1200);

        return CalculatePaymentPlan(capital, intervalCount, interestPA, prematurePayments, mode);
    }

    public static Loan CalculatePaymentPlan(
            double capital,
            int intervalCount,
            double interestPA)
    {
        return CalculatePaymentPlan(
                capital,
                intervalCount,
                interestPA,
                null,
                RecalculationMode.CONSTANT_INSTALMENT);
    }

    public static Loan CalculatePaymentPlan(
            double capital,
            final int intervalCount,
            double interestPA,
            List<PrematurePayment> prematurePayments,
            RecalculationMode mode)
    {
        List<PlanElement> planElements = new ArrayList<>();

        if (prematurePayments == null)
             prematurePayments = new ArrayList<>();

        prematurePayments.add(new PrematurePayment() {{ Instalment = 0; Index = intervalCount; }});

        Collections.sort(prematurePayments);

        double interest = interestPA / 1200;
        int curInterval = 1;
        double curCapital = capital;
        int curIntervalCount = intervalCount;
        int initialInstalmentCount = curIntervalCount;
        double curInstalment = CalculateInstalment(capital, intervalCount, interest);
        double initialInstalment = curInstalment;

        for (Iterator<PrematurePayment> i = prematurePayments.iterator(); i.hasNext();)
        {
            PrematurePayment payment = i.next();

            List<PlanElement> newPlanElements = new ArrayList<>();

            newPlanElements.addAll(
                    PlanConstantPayments(
                            curCapital,
                            curInstalment,
                            curInterval,
                            payment.Index - curInterval + 1,
                            interest));

            planElements.addAll(newPlanElements);

            curIntervalCount -= newPlanElements.size();

            // Get the last element
            PlannedPayment lastInsertedElement = (PlannedPayment)planElements
                    .get(planElements.size() - 1);

            curCapital = lastInsertedElement.CapitalLeft - lastInsertedElement.CapitalPayback - payment.Instalment;

            if (curCapital > 0)
            {
                curInterval = payment.Index + 1;

                if (mode == RecalculationMode.CONSTANT_INSTALMENT)
                {
                    curIntervalCount = FindIntervalCount(
                            curCapital,
                            curIntervalCount,
                            0,
                            curInstalment,
                            interest);
                }
                else
                {
                    curInstalment = CalculateInstalment(curCapital, curIntervalCount, interest);
                }
            }
            else
            {
                payment.Instalment = lastInsertedElement.CapitalLeft - curInstalment;
            }

            if (payment.Instalment > 0)
                planElements.add(payment);

            if (curCapital <= 0)
                break;
        }

        double totalPayback = GetPlannedPaymentsSum(planElements);
        double totalInterest = totalPayback - capital;
        double interestInPercent = 100 * totalInterest / capital;

        Loan retVal = new Loan();

        retVal.initialInstalment = initialInstalment;
        retVal.initialInstalmentCount = initialInstalmentCount;
        retVal.finalInstalmentCount = GetPlannedPaymentHighestIndex(planElements);
        retVal.Capital = capital;
        retVal.InterestPA = interestPA;
        retVal.TotalPayback = totalPayback;
        retVal.TotalInterest = totalInterest;
        retVal.TotalInterestInPercent = interestInPercent;
        retVal.PaymentPlan = planElements;

        return retVal;
    }

    private static int GetPlannedPaymentHighestIndex(List<PlanElement> plan)
    {
        int maxIndex = -1;

        for (Iterator<PlanElement> i = plan.iterator(); i.hasNext();)
        {
            PlanElement element = i.next();
            if(element.Index > maxIndex)
                maxIndex = element.Index;
        }

        return maxIndex;
    }

    private static double GetPlannedPaymentsSum(List<PlanElement> plan)
    {
        double sum = 0;

        for (Iterator<PlanElement> i = plan.iterator(); i.hasNext();)
        {
            PlanElement element = i.next();

            sum += element.Instalment;
        }

        return sum;
    }

    private static List<PlannedPayment> PlanConstantPayments(
            double capital,
            double installment,
            int startIntervalIndex,
            int intervalCount,
            double interest)
    {
        List<PlannedPayment> retVal = new ArrayList<>();

        for (int paymentIndex = startIntervalIndex; paymentIndex < startIntervalIndex + intervalCount; paymentIndex++)
        {
            double interestAmount = capital * interest;

            if (installment >= capital * (1 + interest))
                installment = capital * (1 + interest);

            double capitalPayback = installment - interestAmount;

            PlannedPayment payment = new PlannedPayment();
            payment.Index = paymentIndex;
            payment.Instalment = installment;
            payment.InterestPayback = interestAmount;
            payment.CapitalPayback = capitalPayback;
            payment.CapitalLeft = capital;

            capital = capital - capitalPayback;

            retVal.add(payment);

            if(capital == 0)
                break;
        }

        return retVal;
    }

    public static double CalculateInstalment(
            double capital,
            int intervalCount,
            double interest)
    {
        return capital * Math.pow(1 + interest, intervalCount) * interest / (Math.pow(1 + interest, intervalCount) - 1);
    }

    public static int FindIntervalCount(
            double capital,
            int IntervalCountMax,
            int IntervalCountMin,
            double instalment,
            double interest)
    {
        int curIntervalOffset = (IntervalCountMax - IntervalCountMin) / 2;

        if (curIntervalOffset == 0)
            return IntervalCountMax;

        int curIntervalCount = IntervalCountMin + curIntervalOffset;

        double curInstalment = CalculateInstalment(capital, curIntervalCount, interest);

        if (curInstalment < instalment)
            return FindIntervalCount(capital, curIntervalCount, IntervalCountMin, instalment, interest);

        return FindIntervalCount(capital, IntervalCountMax, curIntervalCount, instalment, interest);
    }
}
