package it.maitre2.app;

import it.maitre2.model.TableState;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResultExporter {

    private static final String HEADER = "timestamp;strategy;seed;" +
            "durationMinutes;makespanMinutes;" +
            "assignedTasks;avgTaskWait;" +
            "utilizationCV;doneTables\n";

    public static void appendRow(String cvsPath, RunResult r,
                                 long seed,
                                 double durationMinutes,
                                 double makespanMinutes){
        int done = r.tablesByState.getOrDefault(TableState.DONE, 0);

        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        String line = String.format(
                "%s;%s;%d;%.3f;%.3f;%d;%.6f;%.6f;%d\n",
                ts,
        escape(r.strategyName),
        seed,
        durationMinutes,
        makespanMinutes,
        r.assignedTasks,
        r.avgTaskWait,
        r.utilizationCV,
        done);

        try{
            Path p = Paths.get(cvsPath);

            if(Files.notExists(p)){
                Files.writeString(p, HEADER, StandardOpenOption.CREATE);
            }
            Files.writeString(p, line, StandardOpenOption.APPEND);
        }catch (IOException e){
            throw new RuntimeException("Cannot write CSV: " + cvsPath, e);
        }
    }

    private static String escape(String s){
        if(s == null) return null;
        //Non ci sono virgole ma per sicurezza
        return s.replace(",", "_");
    }
}
