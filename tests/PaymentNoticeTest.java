package kr.water.calculator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PaymentNoticeTest {
    private static int checks;
    private static void equal(Object expected, Object actual) {
        checks++;
        if (!expected.equals(actual)) throw new AssertionError("Expected: " + expected + "\nActual: " + actual);
    }
    private static PaymentNotice.Item house(String name, String previous, String current) {
        return new PaymentNotice.Item(name, Billing.usage(previous, current), Billing.total(previous, current, "1000", "5000"));
    }
    public static void main(String[] args) {
        String header="신정천기하이츠빌 10월 수도요금(관리비 청소비 공동전기수도 포함) 농협356-0037-8090-13 김미자";
        List<PaymentNotice.Item> houses=Arrays.asList(house("집1","100","125"),house("집2","200","201.125"));
        String notice=PaymentNotice.format(LocalDate.of(2026,10,17),houses);
        equal(header+"\n집1 25t 30,000원\n\n집2 1.125t 6,125원",notice);
        equal("신정천기하이츠빌 1월 수도요금(관리비 청소비 공동전기수도 포함) 농협356-0037-8090-13 김미자\n101호 0t 5,000원",PaymentNotice.format(LocalDate.of(2027,1,1),Arrays.asList(house(" 101호 ","100.000","100"))));
        equal(true,PaymentNotice.format(LocalDate.of(2026,11,30),houses).startsWith("신정천기하이츠빌 11월"));
        equal(true,PaymentNotice.format(LocalDate.of(2026,12,31),houses).startsWith("신정천기하이츠빌 12월"));
        Locale before=Locale.getDefault();
        try { Locale.setDefault(Locale.GERMANY); equal(notice,PaymentNotice.format(LocalDate.of(2026,10,17),houses)); }
        finally {Locale.setDefault(before);}
        ArrayList<PaymentNotice.Item> many=new ArrayList<>();for(int i=1;i<=30;i++)many.add(house("집"+i,"10","20"));
        equal(31,PaymentNotice.format(LocalDate.of(2026,10,17),many).split("\\n+").length);
        equal(true,PaymentNotice.format(LocalDate.of(2026,10,17),many).endsWith("집30 10t 15,000원"));
        System.out.println("PASS: "+checks+" payment notice checks");
    }
}
