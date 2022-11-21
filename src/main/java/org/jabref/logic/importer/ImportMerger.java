package org.jabref.logic.importer;

import org.jabref.logic.database.DatabaseMerger;
import org.jabref.logic.util.UpdateField;
import org.jabref.model.database.BibDatabase;
import org.jabref.preferences.PreferencesService;

import java.util.List;

public class ImportMerger {
    private final PreferencesService prefs;

    public ImportMerger(PreferencesService prefs) {
        this.prefs = prefs;
    }


    public ParserResult mergeImportResults(List<ImportFormatReader.UnknownFormatImport> imports) {
        BibDatabase resultDatabase = new BibDatabase();
        ParserResult result = new ParserResult(resultDatabase);
        for (ImportFormatReader.UnknownFormatImport importResult : imports) {
            if (importResult == null) {
                continue;
            }
            ParserResult parserResult = importResult.parserResult;
            resultDatabase.insertEntries(parserResult.getDatabase().getEntries());

            if (ImportFormatReader.BIBTEX_FORMAT.equals(importResult.format)) {
                // additional treatment of BibTeX
                new DatabaseMerger(prefs.getKeywordDelimiter()).mergeMetaData(
                        result.getMetaData(),
                        parserResult.getMetaData(),
                        importResult.parserResult.getPath().map(path -> path.getFileName().toString()).orElse("unknown"),
                        parserResult.getDatabase().getEntries());
            }
            // TODO: collect errors into ParserResult, because they are currently ignored (see caller of this method)
        }

        // set timestamp and owner
        UpdateField.setAutomaticFields(resultDatabase.getEntries(), prefs.getOwnerPreferences(), prefs.getTimestampPreferences()); // set timestamp and owner

        return result;
    }

}
