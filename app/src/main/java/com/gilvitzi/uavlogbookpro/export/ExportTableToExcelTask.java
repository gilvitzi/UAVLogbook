package com.gilvitzi.uavlogbookpro.export;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gilvitzi.uavlogbookpro.R;
import com.gilvitzi.uavlogbookpro.activity.DatabaseActivity;
import com.gilvitzi.uavlogbookpro.database.GetTableValues;
import com.gilvitzi.uavlogbookpro.util.OnResult;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/*
 * Class ExportTableToExcel gets a query string and filepath and exports the query result to an Excel file (.xls) in the given path
 *  
 */
public class ExportTableToExcelTask extends ExportTable {

    public static final String FILE_EXTENTION_XLS = ".xls";
    private final String LOG_TAG = "ExportTableToExcel";
	private final ProgressDialog dialog;

	private String fileName;
	private String filePath;

	//Google Analytics
    private Tracker mTracker;

    public OnResult<String> onFinished;

	public ExportTableToExcelTask(Activity activity, String fileName, String filePath, String query){
        super(activity, query);
        this.activity = activity;
	    this.fileName = fileName;
	    this.filePath = filePath + "/" + fileName + FILE_EXTENTION_XLS;
		this.dialog = new ProgressDialog(this.activity);
    }

    @Override
    public void onResult(boolean success, GetTableValues.QueryResults queryResults) {
        ExcelFileBuilder excelFileBuilder = new ExcelFileBuilder(queryResults);
        success = excelFileBuilder.build();
        onFinished.onResult(success, "");
    }

    private class ExcelFileBuilder {
        private final List<GetTableValues.ColumnNameType> columns;
        private final List<List<String>> rows;

        public ExcelFileBuilder(GetTableValues.QueryResults queryResults) {
            columns = queryResults.columns;
            rows = queryResults.rows;
        }

        private boolean build() {
            HSSFWorkbook hwb = new HSSFWorkbook();
            HSSFSheet sheet = hwb.createSheet("logbook");

            //Headers Row:
            HSSFRow row = sheet.createRow((short) 0);
            putHeadersToExcelRow(row);

            //Data Rows:
            for (int r = 0; r < rows.size(); r++) {
                List<String> record = rows.get(r);
                row = sheet.createRow((short) 1 + r);
                putSessionToExcelRow(row, record);
            }

            boolean success = writeFileToDisk(hwb);
            return success;
        }

        private boolean writeFileToDisk(HSSFWorkbook hwb) {
            FileOutputStream fileOut;
            boolean success = true;
            try {
                fileOut = new FileOutputStream(filePath);
                hwb.write(fileOut);
                fileOut.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                success = false;
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        private void putSessionToExcelRow(HSSFRow row, List<String> record) {
            HSSFCell cell;

            for (int i = 0; i < record.size(); i++) {
                cell = row.createCell(i);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(record.get(i));
            }
        }

        private void putHeadersToExcelRow(HSSFRow row) {
            HSSFCell cell;
            for (int i = 0; i < columns.size(); i++) {
                cell = row.createCell(i);
                cell.setCellType(Cell.CELL_TYPE_STRING);
                cell.setCellValue(columns.get(i).Name);
            }
        }
    }
}