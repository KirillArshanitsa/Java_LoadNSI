package devops;

import com.jcraft.jsch.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


class WorkSsh {
    private String host;
    private String osUser;
    private String osPassword;
    private int port = 22;
    private int sshTimeout;
    private Session currentSession;

     WorkSsh(String host, String osUser, String osPassword, int sshTimeout ) {
        this.host = host;
        this.osUser = osUser;
        this.osPassword = osPassword;
        this.sshTimeout = sshTimeout;
    }

    void createSshSession(){
        JSch jsch = new JSch();
        try {
            Session currentSession = jsch.getSession(osUser, host, port);
            String userHomeDir = System.getProperty("user.home");
            Paths.get(userHomeDir, ".ssh", "known_hosts");
            jsch.setKnownHosts(Paths.get(userHomeDir, ".ssh", "known_hosts").toString());
            jsch.addIdentity(Paths.get(userHomeDir, ".ssh", "id_rsa").toString());
            currentSession.setPassword(osPassword);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            currentSession.setConfig(config);
            currentSession.connect(sshTimeout);
            this.currentSession = currentSession;
        }
        catch (JSchException ex){
            System.out.println("Ошибка при подключении к " + host + " под пользователем " + osUser);
            System.out.println(ex.getMessage());
            System.exit(1);
        }

    }

    void copyFileToServer(Map<Path, String> filesInfo) {
        System.out.println("Работа с сервером " + host);
        try {
            ChannelSftp sftpChannel = (ChannelSftp) currentSession.openChannel("sftp");
            sftpChannel.connect();
            for (Path pathToLocalFiles : filesInfo.keySet()){
                System.out.println("Копирование на сервер " + host + " из " + pathToLocalFiles + " в " + filesInfo.get(pathToLocalFiles));
                sftpChannel.put(pathToLocalFiles.toString(), filesInfo.get(pathToLocalFiles));
            }
            sftpChannel.disconnect();
            currentSession.disconnect();
            System.out.println("Выполнено.");
        } catch (Exception e) {
            System.out.println("Ошибки при копировании файлов на сервер " + host);
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }


    void runLoadNsiShellScript(String command) {
        System.out.println("Выполнение команды " + command);
        try {
            Channel channel = currentSession.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            ((ChannelExec) channel).setPty(true);
            InputStream in = channel.getInputStream();
            channel.connect(sshTimeout);
            Scanner inScanner = new Scanner(in, "UTF-8");
            String tempStr;
            while (inScanner.hasNextLine()) {
                tempStr = inScanner.nextLine();
                System.out.println(tempStr);
            }
            int exitStatus= channel.getExitStatus();
            if (exitStatus != 0) {
                System.out.println("Обнаружена ошибка при выполенении команды - " + command);
                System.out.println("Получен код ответа не равный 0, код ответа - " + exitStatus);
                channel.disconnect();
                System.exit(1);
            }
            System.out.println("Команда " + command + " выполнена.");
            channel.disconnect();
            currentSession.disconnect();
        } catch (Exception e) {
            System.out.println("Ошибки при выполнении команды " + command);
            e.printStackTrace();
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }


//    public static void main(String[] args) throws IOException {
//        String pathToServerFolder = "";
//        String pathToLocalFile = "C:\\Desktop\\LoadNsi";
//
//        String server = "server";
//        String osUser = "";
//        String osPassword = "";
//        int port = 22;
//        int sshTimeout = 10000;
//
//        Files.list(Path.of(pathToLocalFile)).parallel().forEach( e ->{
//            if(e.getFileName().toString().contains(" ")){
//                String newFileName = e.getFileName().toString().replaceAll( " ", "_");
//                try {
//                    Files.move(e, Path.of(pathToLocalFile, newFileName));
//                }
//                catch (IOException ex){
//                    System.out.println("Ошибка перименования файла " + e);
//                    System.out.println(ex.getMessage());
//                    System.exit(1);
//                }
//            }
//            WorkSsh mySshCopyFilesToServer = new WorkSsh(server, osUser, osPassword, port, sshTimeout, pathToServerFolder, e.toString());
//            mySshCopyFilesToServer.copyFileToServer();
//        });
//        WorkSsh mySshCopyFilesToServer = new WorkSsh(server, osUser, osPassword, port, sshTimeout, pathToServerFolder,  pathToLocalFile);
//        mySshCopyFilesToServer.copyFileToServer();
//    }

}
