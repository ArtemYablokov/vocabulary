package com.yablokovs.vocabulary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


//@Configuration
public class BeforeShutDownChangelog {

    @Bean(destroyMethod = "close")
    public MyBean getMyBean() {
        return new MyBean();
    }

    class MyBean {
        public void close() throws IOException, InterruptedException {

            System.out.println("START");

            Path path = Path.of("/Users/apple/MAC/dev/vocabulary/src/main/resources/liquibase/generated");

            // TODO: 12/01/23 Empty up directory (because it contains old data)
            FileSystemUtils.deleteRecursively(path);
            Files.createDirectory(path);

            // TODO: 12/01/23 execute script of creation changelogs
            ProcessBuilder pb = new ProcessBuilder("/Users/apple/MAC/dev/vocabulary/myshellScript.sh");
            Process p = pb.start();
            int exitCode = p.waitFor();

            System.out.println("STOP + exitCode = " + exitCode);

        }
    }
}
