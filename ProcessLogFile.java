package devops;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;


public class ProcessLogFile {
    private final String partOfSuccessStr = " завершена УСПЕШНО";     
    private final String partOfFailStr = " завершена С ОШИБКАМИ";
    private Path pathToProcessingFile;
    private String loadingFileName;
    private String webLoadUrl;
    private long  timeoutMin;

    ProcessLogFile(String pathToFileStr, String loadingFileName, String webLoadUrl, String timeoutMin) {
        this.pathToProcessingFile = Paths.get(pathToFileStr);
        this.loadingFileName = loadingFileName;
        this.webLoadUrl = webLoadUrl;
        this.timeoutMin = Long.parseLong(timeoutMin);
    }

    void processLogFile(){
        System.out.println("Начало поиска сообщений о завершении загрузки в лог. файле " + pathToProcessingFile);
        System.out.println("Ждём появления фразы " + loadingFileName + partOfSuccessStr + " или " + loadingFileName + partOfFailStr);
        int tryLogFileRotation = 0;
        try {
            long fileSizeBefore = Files.size(pathToProcessingFile);
            System.out.println("Первоначальный размер файла - " + fileSizeBefore);
            //Дёргаем url для запуска загрузки
            WebLoad myWebLoad = new WebLoad(webLoadUrl);
            myWebLoad.startWebLoadNsi();
            //Выход по таймауту
            LocalDateTime startDateTime = LocalDateTime.now();
            LocalDateTime finshWorkDateTime = startDateTime.plusMinutes(timeoutMin);
            System.out.println("Время окончания работы скрипта - " + finshWorkDateTime);
            LocalDateTime currentRunDateTime = LocalDateTime.now();
            while (currentRunDateTime.isBefore(finshWorkDateTime)){
                System.out.println("Обработка файла с логами.");
                try {
                    long fileSizeAfter = Files.size(pathToProcessingFile);
                    System.out.println("Размер файла в байтах - " + fileSizeAfter);
                    if (fileSizeBefore < fileSizeAfter) {
                        System.out.println("Обнаружена дозапись в файл");
                        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(pathToProcessingFile.toString()))) {
                            System.out.println(in.available());
                            long skippedBytes = in.skip(fileSizeBefore);
                            System.out.println("Пропущено байт - " + skippedBytes);
                            int aByte = in.available();
                            System.out.println(aByte);
                            byte[] byteData = new byte[aByte];
                            //byte[] byteData = in.readAllBytes(); c 11 java
                            System.out.println("Прочитано байт - " +in.read(byteData));
                            String str = new String(byteData, StandardCharsets.UTF_8);
                            System.out.println(str);
                            if (str.contains(loadingFileName + partOfSuccessStr)) {
                                System.out.println("\n\n\n\nНайдена запись о успешном завершении загрузки!");
                                System.exit(0);
                            } else if (str.contains(loadingFileName + partOfFailStr)) {
                                System.out.println("\n\n\n\nНайдена запись о не успешном завершении загрузки!");
                                System.exit(1);
                            }
                            //Учёт прочитанных данных
                            fileSizeBefore = fileSizeAfter;
                        }
                    } else if (fileSizeAfter == fileSizeBefore) {
                        System.out.println("Размер файла не изменился, ждём запись в файл, идём спать 6 сек.");
                        Thread.sleep(6000);
                    } else if (fileSizeBefore > fileSizeAfter) {
                        System.out.println("Файл новый, произошла ратация логов.");
                        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(pathToProcessingFile.toString()))) {
                            int aByte = in.available();
                            System.out.println("Доступно байт " + aByte);
                            byte[] byteData = new byte[aByte];
                            //byte[] byteData = in.readAllBytes(); c 11 java
                            System.out.println("Прочитано байт - " +in.read(byteData));
                            String str = new String(byteData, "UTF-8");
                            System.out.println(str);
                            if (str.contains(loadingFileName + partOfSuccessStr)) {
                                System.out.println("\n\n\n\nНайдена запись о успешном завершении загрузки!");
                                System.exit(0);
                            } else if (str.contains(loadingFileName + partOfFailStr)) {
                                System.out.println("\n\n\n\nНайдена запись о не успешном завершении загрузки!");
                                System.exit(1);
                            }
                            //Учёт прочитанных данных
                            fileSizeBefore = fileSizeAfter;
                        }
                    }
                    //Обновление счетчика по времени
                    currentRunDateTime = LocalDateTime.now();
                }
                catch (NoSuchFileException ne){
                    if(tryLogFileRotation > 3){
                        System.out.println("Прошло " + tryLogFileRotation + " попыток ротации логов " + ne.getMessage());
                        System.exit(1);
                    }
                    ++tryLogFileRotation;
                    System.out.println("Проверка на ратирование лог файла, попытка - " + tryLogFileRotation);
                    System.out.println("Ждём 5 сек.");
                    try {
                        Thread.sleep(5000);
                    }
                    catch ( InterruptedException e){
                        e.printStackTrace();
                        System.exit(1);
                    }
                    //Обновление счетчика по времени
                    currentRunDateTime = LocalDateTime.now();
                }
            }
            //Время по циклу while вышло, ошибка.
            System.out.println("Время ожидания по таймауту вышло.");
            System.out.println("Время окончания работы скрипта - " + finshWorkDateTime);
            System.out.println("Время последнего выполнения анализатора файла - " + currentRunDateTime);
            System.exit(1);

        }
        catch (IOException | InterruptedException ex) {
            System.out.println("Ошибка при анализе лог файла " + pathToProcessingFile);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }


    public static void main(String[] args) {
//        String pathToFileStr = "C:\\Desktop\\Изменения.txt";
//        String loadingFileName = "FileForLoad.txt";
//        String loadUrl = "Url";
//        String timeoutMin = "1";
        String pathToFileStr = args[0];
        String loadingFileName = args[1];
        String loadUrl  = args[2];
        String timeoutMin = args[3];

        ProcessLogFile myProcessLogFile = new ProcessLogFile(pathToFileStr, loadingFileName, loadUrl, timeoutMin);
        myProcessLogFile.processLogFile();
    }
}
