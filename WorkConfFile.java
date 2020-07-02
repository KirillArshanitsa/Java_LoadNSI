package devops;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class WorkConfFile {
    private String propertyFile;
    private String standAndServer;
    private String sshUser;
    private String sshPass;
    private String folderForLoad;
    private String httpPort;
    private String appLogFilePath;

    WorkConfFile(String propertyFile, String standAndServer){
        this.propertyFile = propertyFile;
        this.standAndServer = standAndServer;
    }

    public String getSshUser() {
        return sshUser;
    }

    public String getSshPass() {
        return sshPass;
    }

    public String getFolderForLoad() {
        return folderForLoad;
    }
    public String getHttpPort() {
        return httpPort;
    }
    public String getAppLogFilePath() {
        return appLogFilePath;
    }


    void getInfo(){
        try (InputStream input = new FileInputStream(propertyFile)) {
            Properties prop = new Properties();
            prop.load(input);
            sshUser = prop.getProperty(standAndServer + ".sshUser");
            sshPass = prop.getProperty(standAndServer + ".sshPass");
            folderForLoad = prop.getProperty(standAndServer + ".folderForLoad");
            httpPort = prop.getProperty(standAndServer + ".httpPort");
            appLogFilePath = prop.getProperty(standAndServer + ".appLogFilePath");

        } catch (IOException ex) {
            System.out.println("Ошибка при загрузке конф из файла " + propertyFile);
            ex.printStackTrace();
            System.exit(1);
        }

        //TODO переписать
        if (sshUser == null){
            System.out.println("Ошибка - из конф. файла не получен sshUser");
            System.exit(1);
        }
        if(sshPass == null){
            System.out.println("Ошибка - из конф. файла не получен sshPass");
            System.exit(1);
        }
        if (folderForLoad == null){
            System.out.println("Ошибка - из конф. файла не получен folderForLoad");
            System.exit(1);
        }
        if (httpPort == null){
            System.out.println("Ошибка - из конф. файла не получен httpPort");
            System.exit(1);
        }
        if (appLogFilePath == null){
            System.out.println("Ошибка - из конф. файла не получен appLogFilePath");
            System.exit(1);
        }
        System.out.println("Удаление файла = " + new File(propertyFile).delete());
    }

}
