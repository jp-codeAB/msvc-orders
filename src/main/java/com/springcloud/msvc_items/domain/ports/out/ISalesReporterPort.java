package com.springcloud.msvc_items.domain.ports.out;

import com.springcloud.msvc_items.domain.model.Order;
import java.util.List;

public interface ISalesReporterPort {
    byte[] generateConfirmedSalesReport(List<Order> confirmedOrders);
}
