package com.zaikabharat.services;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.zaikabharat.models.Recipe;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class PdfExportService {

    public void exportShoppingList(File destination, Collection<Recipe> recipes) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(destination));
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font bodyFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("ZaikaBharat - Weekly Shopping List\n\n", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            if (recipes.isEmpty()) {
                document.add(new Paragraph("No recipes planned for the week.", bodyFont));
            } else {
                // Collect unique ingredients
                Set<String> allIngredients = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                for (Recipe r : recipes) {
                    if (r.getIngredients() != null) {
                        for (String item : r.getIngredients()) {
                            allIngredients.add(item.trim());
                        }
                    }
                }

                document.add(new Paragraph("Ingredients to Buy:\n\n", sectionFont));

                for (String ingredient : allIngredients) {
                    document.add(new Paragraph("• " + ingredient, bodyFont));
                }
                
                document.add(new Paragraph("\n\nPlanned Recipes:\n\n", sectionFont));
                Set<String> recipeNames = new TreeSet<>();
                for (Recipe r : recipes) recipeNames.add(r.getName());
                
                for(String name : recipeNames) {
                    document.add(new Paragraph("- " + name, bodyFont));
                }
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
