package Application;

import Entite.Product;
import Entite.TopProductStat;
import Services.ServiceProduct;
import Services.ServiceReport;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Génère un rapport PDF des produits (stock, valeur, top ventes).
 */
public class ProductReportPdfGenerator {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void export(File file) throws Exception {
        ServiceProduct serviceProduct = new ServiceProduct();
        ServiceReport serviceReport = new ServiceReport();

        List<Product> allProducts = serviceProduct.afficher();
        int productCount = allProducts.size();
        long totalStockQuantity = allProducts.stream()
                .mapToLong(Product::getQuantity)
                .sum();
        double totalStockValue = allProducts.stream()
                .mapToDouble(p -> p.getQuantity() * p.getPrice())
                .sum();
        List<TopProductStat> topProducts;
        double totalSalesRevenue;

        try {
            topProducts = serviceReport.getTopProducts(20);
            totalSalesRevenue = serviceReport.getTotalRevenue();
        } catch (SQLException ex) {
            topProducts = new ArrayList<>();
            for (Product p : allProducts) {
                double revenue = p.getPrice() * p.getQuantity();
                topProducts.add(new TopProductStat(p.getName(), p.getQuantity(), revenue));
            }
            topProducts.sort(Comparator.comparingDouble(TopProductStat::getTotalRevenue).reversed());
            if (topProducts.size() > 20) {
                topProducts = topProducts.subList(0, 20);
            }
            totalSalesRevenue = topProducts.stream()
                    .mapToDouble(TopProductStat::getTotalRevenue)
                    .sum();
        }

        Document document = new Document(PageSize.A4, 36, 36, 50, 36);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
        Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font sectionTitleFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.BLACK);
        Font textFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);

        Paragraph title = new Paragraph("Rapport Produits - MARKTLY", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);

        Paragraph generatedAt = new Paragraph(
                "Généré le " + LocalDateTime.now().format(DATE_TIME_FORMATTER),
                subtitleFont
        );
        generatedAt.setAlignment(Element.ALIGN_CENTER);
        generatedAt.setSpacingAfter(15);

        document.add(title);
        document.add(generatedAt);

        Paragraph globalSectionTitle = new Paragraph("Vue d'ensemble du stock", sectionTitleFont);
        globalSectionTitle.setSpacingAfter(5);
        document.add(globalSectionTitle);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidths(new float[]{2f, 1.5f});
        summaryTable.setSpacingAfter(15);

        addSummaryRow(summaryTable, "Nombre de produits", String.valueOf(productCount), textFont);
        addSummaryRow(summaryTable, "Quantité totale en stock", String.valueOf(totalStockQuantity), textFont);
        addSummaryRow(summaryTable, "Valeur totale du stock", String.format("%.2f DT", totalStockValue), textFont);

        document.add(summaryTable);

        Paragraph tableSectionTitle = new Paragraph("Détail des produits (score de vente)", sectionTitleFont);
        tableSectionTitle.setSpacingAfter(5);
        document.add(tableSectionTitle);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setSpacingAfter(15);
        table.setWidths(new float[]{3f, 1.2f, 1f, 1.7f, 1.2f});

        addHeaderCell(table, "Nom", textFont);
        addHeaderCell(table, "Prix", textFont);
        addHeaderCell(table, "Qté", textFont);
        addHeaderCell(table, "Score vente", textFont);
        addHeaderCell(table, "% du total", textFont);

        for (TopProductStat p : topProducts) {
            double unitPrice = p.getTotalQuantity() > 0
                    ? p.getTotalRevenue() / p.getTotalQuantity()
                    : 0.0;
            double percentage = totalSalesRevenue > 0
                    ? (p.getTotalRevenue() / totalSalesRevenue) * 100.0
                    : 0.0;

            table.addCell(new Phrase(p.getProductName(), textFont));
            table.addCell(new Phrase(String.format("%.2f DT", unitPrice), textFont));
            table.addCell(new Phrase(String.valueOf(p.getTotalQuantity()), textFont));
            table.addCell(new Phrase(String.format("%.2f DT", p.getTotalRevenue()), textFont));
            table.addCell(new Phrase(String.format("%.1f %%", percentage), textFont));
        }

        document.add(table);
        document.add(new Paragraph(" "));

        Paragraph top5Title = new Paragraph("Top 5 produits (score vente)", sectionTitleFont);
        top5Title.setSpacingAfter(5);
        document.add(top5Title);
        int max = Math.min(5, topProducts.size());
        for (int i = 0; i < max; i++) {
            TopProductStat p = topProducts.get(i);
            String line = String.format(
                    "%d. %s (%.2f DT, score: %.2f DT)",
                    i + 1,
                    p.getProductName(),
                    p.getTotalQuantity() > 0 ? p.getTotalRevenue() / p.getTotalQuantity() : 0.0,
                    p.getTotalRevenue()
            );
            Paragraph item = new Paragraph(line, textFont);
            item.setIndentationLeft(15);
            document.add(item);
        }

        document.close();
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 250));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(4);
        labelCell.setBorderWidth(0.5f);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(4);
        valueCell.setBorderWidth(0.5f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
