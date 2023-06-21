package org.nastation.demo.util;

import org.nachain.core.base.Amount;
import org.nachain.core.base.Unit;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author John | NaChain
 * @since 07/24/2021 2:49
 */
public class NumberUtil {

    public static double bigIntToNacDouble(BigInteger bi) {

        if (bi.intValue() == 0) {
            return 0D;
        }

        Amount amount = Amount.of(bi);
        BigDecimal bigDecimal = amount.toDecimal(Unit.NAC);
        return MathUtil.round(bigDecimal.doubleValue(), 9);
    }

    public static BigInteger nacDoubleToBigInt(double value) {
        return Amount.of(new BigDecimal(value), Unit.NAC).toBigInteger();
    }

}
