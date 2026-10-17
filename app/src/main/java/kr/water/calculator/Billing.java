package kr.water.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Billing {
    private Billing() {}
    public static BigDecimal number(String value) {
        if (value == null || !value.trim().matches("[0-9]{1,12}(\\.[0-9]{1,3})?"))
            throw new IllegalArgumentException("0 이상의 숫자를 입력해 주세요 (정수 12자리, 소수 3자리까지).");
        return new BigDecimal(value.trim());
    }
    public static BigDecimal usage(String previous, String current) {
        BigDecimal result = number(current).subtract(number(previous));
        if (result.signum() < 0) throw new IllegalArgumentException("이번달 수치는 지난달 수치 이상이어야 합니다.");
        return result;
    }
    public static BigDecimal total(String previous, String current, String rate, String fee) {
        return usage(previous, current).multiply(number(rate)).add(number(fee)).setScale(0, RoundingMode.HALF_UP);
    }
}
