package com.panaut.loancalculator.Loan;

import java.io.Serializable;

/**
 * Created by ivan.cojbasic on 7/14/2016.
 */
public abstract class PlanElement implements Serializable, Comparable<PlanElement>
{
    public int Index;
    public double Instalment;

    public int compareTo(PlanElement element)
    {
        return Index - element.Index;
    }
}