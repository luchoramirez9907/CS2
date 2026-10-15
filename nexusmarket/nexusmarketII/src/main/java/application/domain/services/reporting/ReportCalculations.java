package application.domain.services.reporting;

import application.domain.models.Order;
import application.domain.valueobjects.OrderStatus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculations shared by the administrative report services. Revenue and
 * sales only count orders whose payment was confirmed (PAID, SHIPPED or
 * DELIVERED).
 */
final class ReportCalculations {

    private static final Set<OrderStatus> PAID_STATUSES =
            Set.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private ReportCalculations() {
    }

    static boolean isPaid(Order order) {
        return PAID_STATUSES.contains(order.getOrderStatus());
    }

    static <T> Map<String, Long> countBy(List<T> values, Function<T, String> classifier) {
        return values.stream().collect(Collectors.groupingBy(classifier, LinkedHashMap::new,
                Collectors.counting()));
    }

    static <T> BigDecimal sum(List<T> values, Function<T, BigDecimal> amount) {
        return values.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
