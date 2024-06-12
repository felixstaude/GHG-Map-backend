package de.felixstaude.ghgmap;

import de.felixstaude.ghgmap.database.DatabaseGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    @Autowired
    private DatabaseGenerator databaseGenerator;

    public static void main(String[] args){
        SpringApplication.run(Main.class, args);

    }

    @PostConstruct
    public void init(){
        databaseGenerator.generateTable();
    }
}
