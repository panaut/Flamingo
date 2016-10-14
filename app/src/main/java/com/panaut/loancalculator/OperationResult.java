package com.panaut.loancalculator;

import android.content.Context;
import android.content.Intent;

import java.util.Objects;

/**
 * Created by ivan.cojbasic on 7/15/2016.
 */
public class OperationResult<T> {

    private boolean isSuccess;
    private Integer errorCode;
    private T resultObject;


    public boolean isSuccess()
    {
        return isSuccess;
    }

    public Integer errorCode()
    {
        return errorCode;
    }

    public String errorMessage(Context ctx)
    {
        if(errorCode != null)
        {
            String errorKey = "error_" + errorCode;

            int resId = ctx.getResources().getIdentifier(errorKey, "string", ctx.getPackageName());

            return ctx.getResources().getString(resId);
        } else
            return null;
    }

    public T getResultObject()
    {
        return resultObject;
    }

    public OperationResult(boolean isSuccess, T resultObject, Integer errorCode)
    {
        this.isSuccess = isSuccess;

        if (isSuccess) {
            this.resultObject = resultObject;
            this.errorCode = null;
        } else {
            this.resultObject = null;
            if(errorCode != null)
                this.errorCode = errorCode;
        }
    }
}
