package devops;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

class WebLoad {
    private static final String DISABLEDLOADING = "Loading is disabled!";
    private String webLoadUrl;

    WebLoad(String webLoadUrl){
        this.webLoadUrl = webLoadUrl;
    }

    void startWebLoadNsi(){
        System.out.println("Вызов " + webLoadUrl);
        try {
            URL prodCatLoadUrl = new URL(webLoadUrl);
            InputStream inStream = prodCatLoadUrl.openStream();
            Scanner in = new Scanner(inStream);
            while (in.hasNextLine()) {
                String strFromUrl = in.nextLine();
                if (strFromUrl.equalsIgnoreCase(DISABLEDLOADING)) {
                    System.out.println("Загрузка отключена, нужно вкл. параметр в ");
                    System.exit(1);
                }
                System.out.println(strFromUrl);
            }
        }
        catch (IOException ex){
            System.out.println("\n\n\nОшибка при обращении к " + webLoadUrl);
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

}
