package kr.water.calculator;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Plain-text payment notice made from a completed calculation snapshot. */
public final class PaymentNotice {
    private PaymentNotice() {}

    public static final class Item {
        public final String name;
        public final BigDecimal usage, total;

        public Item(String name, BigDecimal usage, BigDecimal total) {
            this.name = name.trim();
            this.usage = usage;
            this.total = total;
        }
    }

    public static String format(LocalDate calculationDate, List<Item> items) {
        NumberFormat won = NumberFormat.getIntegerInstance(Locale.KOREA);
        StringBuilder text = new StringBuilder("신정천기하이츠빌 ")
                .append(calculationDate.getMonthValue())
                .append("월 수도요금(관리비 청소비 공동전기수도 포함) 농협356-0037-8090-13 김미자");
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            text.append(i == 0 ? "\n" : "\n\n")
                    .append(item.name).append(' ')
                    .append(item.usage.stripTrailingZeros().toPlainString()).append("t ")
                    .append(won.format(item.total)).append("원");
        }
        return text.toString();
    }
}
