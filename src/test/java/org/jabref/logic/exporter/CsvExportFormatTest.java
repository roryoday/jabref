package org.jabref.logic.exporter;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jabref.logic.layout.LayoutFormatterPreferences;
import org.jabref.logic.xmp.XmpPreferences;
import org.jabref.model.database.BibDatabaseContext;
import org.jabref.model.database.BibDatabaseMode;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.BibEntryTypesManager;
import org.jabref.model.entry.field.StandardField;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class CsvExportFormatTest {

    public BibDatabaseContext databaseContext;
    private Exporter exportFormat;

    @BeforeEach
    public void setUp() {
        List<TemplateExporter> customFormats = new ArrayList<>();
        LayoutFormatterPreferences layoutPreferences = mock(LayoutFormatterPreferences.class, Answers.RETURNS_DEEP_STUBS);
        SavePreferences savePreferences = mock(SavePreferences.class);
        XmpPreferences xmpPreferences = mock(XmpPreferences.class);
        BibEntryTypesManager entryTypesManager = mock(BibEntryTypesManager.class);
        ExporterFactory exporterFactory = ExporterFactory.create(customFormats, layoutPreferences, savePreferences, xmpPreferences, BibDatabaseMode.BIBTEX, entryTypesManager);

        exportFormat = exporterFactory.getExporterByName("oocsv").get();

        databaseContext = new BibDatabaseContext();
    }

    @AfterEach
    public void tearDown() {
        exportFormat = null;
    }

    @Test
    public void testPerformExportForSingleAuthor(@TempDir Path testFolder) throws Exception {
        Path path = testFolder.resolve("ThisIsARandomlyNamedFile");

        BibEntry entry = new BibEntry();
        entry.setField(StandardField.AUTHOR, "Someone, Van Something");
        List<BibEntry> entries = Arrays.asList(entry);

        exportFormat.export(databaseContext, path, entries);

        List<String> lines = Files.readAllLines(path);
        assertEquals(2, lines.size());
        assertEquals(
                "10,\"\",\"\",\"Someone, Van Something\",\"\",\"\",,,\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"",
                lines.get(1));
    }

    @Test
    public void testPerformExportForMultipleAuthors(@TempDir Path testFolder) throws Exception {
        Path path = testFolder.resolve("ThisIsARandomlyNamedFile");

        BibEntry entry = new BibEntry();
        entry.setField(StandardField.AUTHOR, "von Neumann, John and Smith, John and Black Brown, Peter");
        List<BibEntry> entries = Arrays.asList(entry);

        exportFormat.export(databaseContext, path, entries);

        List<String> lines = Files.readAllLines(path);
        assertEquals(2, lines.size());
        assertEquals(
                "10,\"\",\"\",\"von Neumann, John; Smith, John; Black Brown, Peter\",\"\",\"\",,,\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"",
                lines.get(1));
    }

    @Test
    public void testPerformExportForSingleEditor(@TempDir Path testFolder) throws Exception {
        Path path = testFolder.resolve("ThisIsARandomlyNamedFile");
        File tmpFile = path.toFile();
        BibEntry entry = new BibEntry();
        entry.setField(StandardField.EDITOR, "Someone, Van Something");
        List<BibEntry> entries = Arrays.asList(entry);

        exportFormat.export(databaseContext, tmpFile.toPath(), entries);

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        assertEquals(2, lines.size());
        assertEquals(
                "10,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Someone, Van Something\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"",
                lines.get(1));
    }

    @Test
    public void testPerformExportForMultipleEditors(@TempDir Path testFolder) throws Exception {
        Path path = testFolder.resolve("ThisIsARandomlyNamedFile");
        File tmpFile = path.toFile();
        BibEntry entry = new BibEntry();
        entry.setField(StandardField.EDITOR, "von Neumann, John and Smith, John and Black Brown, Peter");
        List<BibEntry> entries = Arrays.asList(entry);

        exportFormat.export(databaseContext, tmpFile.toPath(), entries);

        List<String> lines = Files.readAllLines(tmpFile.toPath());
        assertEquals(2, lines.size());
        assertEquals(
                "10,\"\",\"\",\"\",\"\",\"\",,,\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"von Neumann, John; Smith, John; Black Brown, Peter\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"",
                lines.get(1));
    }
    @Test
    public void testFieldWithDelimiters(@TempDir Path testFolder) throws Exception {
        Path path = testFolder.resolve("ThisIsARandomlyNamedFile");

        BibEntry entry = new BibEntry();

        entry.setField(StandardField.TITLE, "\"test1,\",\"test2,\"\"");

        List<BibEntry> entries = Arrays.asList(entry);

        exportFormat.export(databaseContext, path, entries);
        //make sure single quotes replace with double quotes
        List<String> lines = Files.readAllLines(path);
        assertEquals(2, lines.size());
        assertEquals(
                "10,\"\",\"\",\"\",\"\"\"test1,\"\",\"\"test2,\"\"\"\",\"\",,,\"\",\"\",,\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\"",
                lines.get(1));
    }

    @Test
    public void testCSVFileWithFieldDelimiters(@TempDir Path testFolder) throws Exception {
        Path path = testFolder.resolve("ThisIsARandomlyNamedFile");

        String delimitedName = "\"test1,\",\"test2,\"";
        BibEntry entry = new BibEntry();
        entry.setField(StandardField.TITLE, delimitedName);

        List<BibEntry> entries = Arrays.asList(entry);
        exportFormat.export(databaseContext, path, entries);
        List<String> lines = Files.readAllLines(path);
        Reader in = new StringReader(lines.get(1));
        // read data as csv file
        CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT);
        CSVRecord list = parser.getRecords().get(0);
        // number of columns = 31
        assertEquals(31, list.size());
        //make sure author does NOT split to two different columns
        assertEquals(delimitedName, list.get(4));
    }
}
