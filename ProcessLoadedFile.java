package devops;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

class ProcessLoadedFile {
    private Path pathToFile;
    private String fileName;
    private List<Path> filesForLoad;

    ProcessLoadedFile(String pathToFile){
        this.pathToFile = Paths.get(pathToFile);
        fileName = this.pathToFile.getFileName().toString();
    }


    private boolean checkFileIsZip(){
        String fileExtension =  fileName.substring(fileName.lastIndexOf(".") + 1);
        if (fileExtension.equalsIgnoreCase("zip")) return true;
        else return false;

    }

    private void unzipFile() {
        System.out.println("Обработка архива " + pathToFile);
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(pathToFile.toString()))) {
            ZipFile zipFile = new ZipFile(pathToFile.toString());
            Enumeration<? extends ZipEntry> a =  zipFile.entries();
            int num = 0;
            System.out.println("Файлы в архиве: ");
            while (a.hasMoreElements()){
                System.out.println(a.nextElement());
                ++num;
            }
            if (num > 1){
                System.out.println("\n\nВ архиве обнаружено более одного элемента");
                System.out.println("Пока поддерживается работа только с 1 файлом!");
                System.exit(1);
            }

            ZipEntry entry;
            byte [] buffer = new byte[1024];

            while ((entry = zipIn.getNextEntry()) != null) {
                String zipFileName = entry.getName();
                if (zipFileName.contains(" ")) {
                    System.out.println("Заменяем пробелы в имени файла " + zipFileName);
                    zipFileName = entry.getName().replaceAll(" ", "_");
                }
                if (pathToFile.getParent() == null){
                    System.out.println("Распаковка " + entry.getName() + " в " + zipFileName);
                    try(FileOutputStream fos = new FileOutputStream(zipFileName)) {
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    //c 11 java
                    //Files.write(Paths.get(zipFileName), zipIn.readAllBytes());
                    //Files.write(Paths.get(zipFileName), byteData);
                    filesForLoad.add(Paths.get(zipFileName));
                }
                else{
                    Path pathToUnZipFile = pathToFile.resolveSibling(zipFileName);
                    System.out.println("Распаковка " + entry.getName() + " в " + pathToUnZipFile);
                    try(FileOutputStream fos = new FileOutputStream(pathToUnZipFile.toString())) {
                        int len;
                        while ((len = zipIn.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }

                    }
                    //c 11 java
                    //Files.write(Paths.get(zipFileName), byteData);
                    //Files.write(pathToUnZipFile, zipIn.readAllBytes());
                    filesForLoad.add(pathToUnZipFile);
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    List<Path> getFilesForLoad(){
        filesForLoad = new ArrayList<>();
        if (checkFileIsZip()) {
            System.out.println("Обнаружен zip файл - " + pathToFile);
            unzipFile();
        }
        else {
//            if (fileName.contains(" ")){
//                System.out.println("Заменяем пробелы в файле " + fileName);
//                fileName =  fileName.replaceAll(" ", "");
//                Files.move(pathToFile, Paths.get())
//            }
            filesForLoad.add(Paths.get(fileName).toAbsolutePath());
        }
        return filesForLoad;
    }

//    public static void main(String[] args) {
//        ProcessLoadedFile my = new ProcessLoadedFile("D:\\LoadNsi\\22 2.zip");
//        System.out.println(my.getFilesForLoad());
//    }

}
