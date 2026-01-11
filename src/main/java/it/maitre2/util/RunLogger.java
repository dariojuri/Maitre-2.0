package it.maitre2.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class RunLogger implements AutoCloseable {
    private final FileWriter writer;

    public RunLogger(String filename){
        try{
            this.writer = new FileWriter(filename, false);
            log("# Run started at " + LocalDateTime.now());
        } catch (IOException e) {
            throw new RuntimeException("Cannot open log file: " + filename, e);
        }
    }

    public void log(String message){
        try{
            String line = message + "\n";
            System.out.println(line); //console
            writer.write(line);       //file
            writer.flush();
        } catch(IOException e){
            throw new RuntimeException("Error writing log", e);
        }
    }

    @Override
    public void close(){
        try{
            log("# Run closed");
            writer.close();
        } catch (IOException e){
            //ignora
        }
    }
}
