package com.holdempub.membermanager.util;

import com.holdempub.membermanager.domain.Member;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 회원 목록을 엑셀(.xlsx) 파일로 내보내기.
 */
public final class ExcelExportUtil {

    private ExcelExportUtil() {}

    public static void exportMembers(List<Member> members, Path filePath) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("회원 목록");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            Cell c0 = headerRow.createCell(0);
            c0.setCellValue("닉네임");
            c0.setCellStyle(headerStyle);
            Cell c1 = headerRow.createCell(1);
            c1.setCellValue("누적 점수");
            c1.setCellStyle(headerStyle);

            for (int i = 0; i < members.size(); i++) {
                Member m = members.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(m.getNickname());
                row.createCell(1).setCellValue(m.getScore());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(filePath)) {
                wb.write(out);
            }
        }
    }
}
