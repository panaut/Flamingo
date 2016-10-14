package com.panaut.loancalculator.Loan;

import com.panaut.loancalculator.Loan.PlanElement;

import java.io.Serializable;

/**
 * Created by ivan.cojbasic on 7/14/2016.
 */
public class PlannedPayment extends PlanElement implements Serializable
{
    public double InterestPayback;

    public double CapitalPayback;

    public double CapitalLeft;
}