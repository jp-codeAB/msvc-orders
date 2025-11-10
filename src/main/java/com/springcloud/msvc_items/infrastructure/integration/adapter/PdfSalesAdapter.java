package com.springcloud.msvc_items.infrastructure.integration.adapter;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.springcloud.msvc_items.domain.model.Order;
import com.springcloud.msvc_items.domain.model.OrderItem;
import com.springcloud.msvc_items.domain.ports.out.ISalesReporterPort;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfSalesAdapter implements ISalesReporterPort {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font TOTAL_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Override
    public byte[] generateConfirmedSalesReport(List<Order> confirmedOrders) {

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, os);
            document.open();

            String reportDate = LocalDateTime.now().format(FORMATTER);
            String reportTitle = String.format("REPORTE DE VENTAS ARKA - AL \n %s", reportDate);
            Paragraph title = new Paragraph(reportTitle, TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(String.format("Total de Órdenes Confirmadas Procesadas: %d", confirmedOrders.size()), NORMAL_FONT));
            document.add(new Paragraph("\n"));
            if (confirmedOrders.isEmpty()) {
                document.add(new Paragraph("No se encontraron órdenes en estado CONFIRMED para este reporte.", NORMAL_FONT));
                document.close();
                return os.toByteArray();
            }

            double grandTotal = 0.0;
            for (Order order : confirmedOrders) {
                grandTotal += order.getTotalAmount();
                document.add(new Paragraph("----------------------------------------------------------------------"));
                document.add(new Paragraph(String.format("ORDEN #%d | Cliente ID: %d", order.getId(), order.getCustomerId()), TOTAL_FONT));
                String createDate = order.getCreateAt() != null ? order.getCreateAt().format(FORMATTER) : "Fecha no disponible";
                document.add(new Paragraph(String.format("Fecha: %s | Total Orden: $%.2f", createDate, order.getTotalAmount()), NORMAL_FONT));
                document.add(new Paragraph("----------------------------------------------------------------------"));
                PdfPTable itemTable = new PdfPTable(4);
                itemTable.setWidthPercentage(100);
                itemTable.setSpacingBefore(5f);
                itemTable.setSpacingAfter(10f);
                addTableCell(itemTable, "PROD. ID", HEADER_FONT, Color.DARK_GRAY);
                addTableCell(itemTable, "CANTIDAD", HEADER_FONT, Color.DARK_GRAY);
                addTableCell(itemTable, "PRECIO UNIT.", HEADER_FONT, Color.DARK_GRAY);
                addTableCell(itemTable, "SUBTOTAL", HEADER_FONT, Color.DARK_GRAY);

                for (OrderItem item : order.getItems()) {
                    addTableCell(itemTable, String.valueOf(item.getProductId()), NORMAL_FONT, Color.WHITE);
                    addTableCell(itemTable, String.valueOf(item.getQuantity()), NORMAL_FONT, Color.WHITE);
                    addTableCell(itemTable, String.format("$%.2f", item.getPrice()), NORMAL_FONT, Color.WHITE);
                    addTableCell(itemTable, String.format("$%.2f", item.getSubTotal()), NORMAL_FONT, Color.WHITE);
                }
                document.add(itemTable);
            }
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("======================================================", TOTAL_FONT));
            Paragraph grandTotalParagraph = new Paragraph(String.format("GRAN TOTAL DE VENTAS CONFIRMADAS: $%.2f", grandTotal), TOTAL_FONT);
            grandTotalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(grandTotalParagraph);
            document.add(new Paragraph("======================================================", TOTAL_FONT));
            document.close();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error fatal al generar el reporte PDF.", e);
        }
    }

    private void addTableCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        table.addCell(cell);
    }
}