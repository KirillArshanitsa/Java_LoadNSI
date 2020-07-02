package devops;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWork {
    public static void main(String[] args) {
//        String nsiFileName =  "D:\\LoadNsi\\file.zip";
//        String standAndServer = "Stand.server;
//        String propertyFile = "D:\\LoadNsi\\config.txt";
//        String partUrlForLoadNsi = "product-catalog/excel/init";
//        String jarFileStrPath = "D:\\LoadNsi\\LoadNsi.jar";

        String nsiFileName = args[0];
        String standAndServer = args[1];
        String propertyFile = args[2];
        String partUrlForLoadNsi = args[3];
        String timeoutMin  = args[4];
        Path jarFile = Paths.get("LoadNsi.jar");

        //TODO посмотреть как заменить динамически - jarFile
        //Path jarFile = Paths.get(jarFileStrPath).toAbsolutePath();
        int sshTimeout = 14000;
        ProcessLoadedFile myProcessLoadedFile = new ProcessLoadedFile(nsiFileName);
        List<Path> filesForLoad = myProcessLoadedFile.getFilesForLoad();
        WorkConfFile myWorkConfFile = new WorkConfFile(propertyFile, standAndServer);
        myWorkConfFile.getInfo();
        String server = standAndServer.substring(standAndServer.indexOf(".") + 1);
        System.out.println("Работы выполняются на сервере " + server);
        WorkSsh mySshCopyFilesToServer = new WorkSsh(server,
                myWorkConfFile.getSshUser(), myWorkConfFile.getSshPass(), sshTimeout);
        mySshCopyFilesToServer.createSshSession();
        Map<Path, String> filesForCopy = new HashMap<>();
        for (Path p: filesForLoad){
            filesForCopy.put(p, myWorkConfFile.getFolderForLoad());
        }
        filesForCopy.put(jarFile, "/tmp");

        System.out.println("Копируем файлы для загрузки и скрипт для обработки логов на сервер " + server);
        mySshCopyFilesToServer.copyFileToServer(filesForCopy);
        System.out.println("Копирование выполнено");

        //Запуск самого скрипта обработки
        //TODO filesForLoad.get(0) - временно пока не доделана обработка множества файлов
        String runParselog = "java -cp /tmp/" + jarFile.getFileName() + " com.sbt.devops.ProcessLogFile " + myWorkConfFile.getAppLogFilePath() +
                " " + filesForLoad.get(0).getFileName();
        String webLoadUrl = "http://" + server + ":" + myWorkConfFile.getHttpPort() + "/" + partUrlForLoadNsi;
        String sshCommand = runParselog + " " + webLoadUrl + " " + timeoutMin;
        System.out.println("Запуск анализатора " + sshCommand);
        mySshCopyFilesToServer.createSshSession();
        mySshCopyFilesToServer.runLoadNsiShellScript(sshCommand);
    }
}


