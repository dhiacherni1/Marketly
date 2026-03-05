package Application;

import Entite.Order;
import Entite.OrderItem;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Génère une facture au format PDF à partir d'une commande.
 * Utilisé pour l'export fichier et l'envoi par e-mail.
 */
public class InvoicePdfGenerator {
    public static void export(Order order, File file) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        document.add(new Paragraph("FACTURE N° " + order.getId()));
        document.add(new Paragraph(" "));

        String clientName = order.getCustomer() != null
                ? order.getCustomer().getPrenom() + " " + order.getCustomer().getNom()
                : "-";
        document.add(new Paragraph("Client : " + clientName));
        document.add(new Paragraph("Adresse : " + (order.getAddress() != null ? order.getAddress() : "-")));
        document.add(new Paragraph("Téléphone : " + (order.getPhone() != null ? order.getPhone() : "-")));
        if (order.getPaymentMethod() != null && !order.getPaymentMethod().isEmpty()) {
            document.add(new Paragraph("Mode de paiement : " + order.getPaymentMethod()));
        }
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setSpacingAfter(10);
        table.addCell(headerCell("Produit"));
        table.addCell(headerCell("Quantité"));
        table.addCell(headerCell("Prix unit."));
        table.addCell(headerCell("Total ligne"));
        java.util.List<OrderItem> items = order.getItems();
        if (items != null) {
            for (OrderItem item : items) {
                table.addCell(cell(item != null ? item.getProductName() : null));
                table.addCell(cell(item != null ? String.valueOf(item.getQuantity()) : "0"));
                table.addCell(cell(item != null ? String.format("%.2f", item.getUnitPrice()) : "0.00"));
                table.addCell(cell(item != null ? String.format("%.2f", item.getLineTotal()) : "0.00"));
            }
        }
        document.add(table);

        double total = order.getTotal();
        document.add(new Paragraph(" "));
        document.add(new Paragraph("TOTAL : " + String.format("%.2f", total) + " €"));
        document.close();
    }

    private static PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setGrayFill(0.9f);
        return cell;
    }

    private static PdfPCell cell(String text) {
        return new PdfPCell(new Phrase(text != null ? text : ""));
    }
}
