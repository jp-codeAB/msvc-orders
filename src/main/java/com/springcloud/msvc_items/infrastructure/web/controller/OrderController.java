package com.springcloud.msvc_items.infrastructure.web.controller;

import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderItem;
import com.springcloud.msvc_items.domain.model.OrderStatus;
import com.springcloud.msvc_items.domain.ports.in.CreateOrderUseCase;
import com.springcloud.msvc_items.domain.ports.in.IManageOrderUseCase;
import com.springcloud.msvc_items.domain.ports.out.ISalesReporterPort;
import com.springcloud.msvc_items.domain.ports.out.NotificationServicePort;
import com.springcloud.msvc_items.infrastructure.security.AuthUser;
import com.springcloud.msvc_items.infrastructure.mapper.IOrderMapper;
import com.springcloud.msvc_items.infrastructure.web.dto.request.OrderRequest;
import com.springcloud.msvc_items.infrastructure.web.dto.request.StatusUpdateRequest;
import com.springcloud.msvc_items.infrastructure.web.dto.response.OrderResponse;
import com.springcloud.msvc_items.shared.exception.custom.BusinessException;
import com.springcloud.msvc_items.shared.exception.custom.InvalidOrderStatusTransitionException;
import com.springcloud.msvc_items.shared.exception.custom.OrderNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final IManageOrderUseCase manageOrderUseCase;
    private final NotificationServicePort notificationServicePort;
    private final ISalesReporterPort salesReporterPort;
    private final IOrderMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public ResponseEntity<List<OrderResponse>> listAll(
            @AuthenticationPrincipal AuthUser authUser) {

        boolean isAdmin = authUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Order> orders;
        if (isAdmin) {
            orders = manageOrderUseCase.findAllOrders();
        } else {
            orders = manageOrderUseCase.findAllOrdersByCustomer(authUser.getId());
        }

        List<OrderResponse> response = orders.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderAccessGuard.isOwner(#id, authentication.principal.id)")
    public ResponseEntity<OrderResponse> getById(
            @PathVariable("id") Long id) {
        Order order = manageOrderUseCase.findOrderById(id)
                .orElseThrow(() -> new OrderNotFoundException(String.format("Orden con ID %d no encontrada", id)));

        return ResponseEntity.ok(mapper.toResponse(order));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public Long createOrderFromService(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody OrderRequest request) {

        List<OrderItem> domainItems = request.getItems().stream()
                .map(mapper::toDomainItem)
                .collect(Collectors.toList());
        Order orderToCreate = new Order(request.getCustomerId(), domainItems);
        Order createdOrder = createOrderUseCase.createOrder(orderToCreate);
        String customerEmail = authUser.getEmail();
        if (createdOrder.getStatus() == OrderStatus.PENDING) {
            notificationServicePort.notifyOrderStatusChange(createdOrder, customerEmail);
        }
        return createdOrder.getId();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable("id") Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getNewStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Estado inválido proporcionado: " + request.getNewStatus());
        }
        boolean isAdmin = authUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isClient = authUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"));

        if (isAdmin) {
            if (newStatus != OrderStatus.IN_DISPATCH && newStatus != OrderStatus.DELIVERED) {
                throw new InvalidOrderStatusTransitionException(
                        "El Administrador solo puede establecer el estado a IN_DISPATCH o DELIVERED.");
            }
        } else if (isClient) {
            if (newStatus != OrderStatus.PENDING && newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                throw new InvalidOrderStatusTransitionException(
                        "El Cliente solo puede establecer el estado a CONFIRMED o CANCELLED.");
            }
            if (!manageOrderUseCase.isOwner(id, authUser.getId())) {
                throw new AccessDeniedException(
                        "Acceso denegado. Un cliente solo puede modificar el estado de sus propias órdenes.");
            }
        } else {
            throw new AccessDeniedException("Su rol no tiene permisos para modificar el estado de una orden.");
        }
        Order updatedOrder = manageOrderUseCase.updateOrderStatus(id, newStatus);
        String customerEmail = authUser.getEmail();
        notificationServicePort.notifyOrderStatusChange(updatedOrder, customerEmail);
        return ResponseEntity.ok(mapper.toResponse(updatedOrder));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @orderAccessGuard.isPendingOwner(#id, authentication.principal.id)")
    public void deleteOrder(@PathVariable("id") Long id) {
        manageOrderUseCase.deleteOrder(id);
    }

    @GetMapping("/{orderId}/owner/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isOwnerInternal(
            @PathVariable("orderId") Long orderId,
            @PathVariable("userId") Long userId) {

        boolean isOwner = manageOrderUseCase.isOwner(orderId, userId);

        log.info("Internal ownership check for Order ID {} by User ID {}: {}", orderId, userId, isOwner);
        return ResponseEntity.ok(isOwner);
    }

    @GetMapping("/report/sales/confirmed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getConfirmedSalesReports() {
        List<Order> confirmedOrders = manageOrderUseCase.findConfirmedOrders();
        byte[] pdfBytes = salesReporterPort.generateConfirmedSalesReport(confirmedOrders);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "sales_report_confirmed_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        headers.setContentDispositionFormData("attachment", filename);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
