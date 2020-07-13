import org.intellij.clojure.nrepl.NReplClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
    private ServerSocket serverSocket;

    private Server() throws  IOException{
        serverSocket = new ServerSocket(5470);
        Server.ShutdownHook shutdownHook = new Server.ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        while (true) {
            try{
                Socket sc=serverSocket.accept();
                cServer c=new cServer(sc);
            }
            catch (IOException e) {
                e.printStackTrace();
                serverSocket.close();
            }
        }
    }

    class ShutdownHook extends Thread{

        public void run(){
            try{
                serverSocket.close();}
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

private class cServer extends Thread{
        private Socket socket;
        private DataOutputStream dout;
        private DataInputStream din;
        private Request client;

        cServer(Socket s){
            socket=s;
            cServer.ShutdownHook shutdownHook = new cServer.ShutdownHook();
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            try {
                dout = new DataOutputStream(socket.getOutputStream());
                din = new DataInputStream(socket.getInputStream());
                client=new Request();
                client.stage=0;
                dialog();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void dialog() throws IOException{
            try{
                while (true){
                    String text=din.readUTF();
                    text=answer(text);
                    dout.writeUTF(text);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                socket.close();
            }
        }

        private String answer(String text){

            switch (client.stage){
                case (0):
                    return begin(text);
                case (1):
                    return name(text);
                case (2):
                    return FamName(text);
                case (3):
                    return Patron(text);
                case (4):
                    return check(text);
                case (5):
                    return Birthdate(text);
                case (6):
                    return email(text);
                case (7):
                    return phone(text);
                case (8):
                    return check2(text);
                case (9):
                    return "Анкета заполненна";
                default:
                    return "Извините, что-то пошло не так";
            }
        }

        private String begin(String text) {
            if (text.toLowerCase().contains("да")){
                client.stage=1;
                return "Введите ваше имя";
            }
            if (text.toLowerCase().contains("нет")) return "В таком случае данный чат-бот вам не поможет";
            return "Извините, не могу понять ваш ответ";
        }

    private String name(String text) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);
        if (text.contains(" ")||text.length()<2||m.find()) return "Некоректное имя";
        String a=text.substring(0,1).toUpperCase();
        String b=text.substring(1).toLowerCase();
        client.Name=a+b;
        client.stage=2;
        return "Введите вашу фамилию";
    }

    private String FamName(String text) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);
        if (text.contains(" ")||text.length()<2||m.find()) return "Некоректная фамилия";
        String a=text.substring(0,1).toUpperCase();
        String b=text.substring(1).toLowerCase();
        client.FamilyName=a+b;
        client.stage=3;
        return "Введите ваше отчество(если его нет, отправьте пустое сообщение)";
    }

    private String Patron(String text) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(text);
        if (text.contains(" ")||m.find()) return "Некоректное отчество";
        if (text.length()>=2){
            String a=text.substring(0,1).toUpperCase();
            String b=text.substring(1).toLowerCase();
            client.Patronymic=a+b;
        }
        else client.Patronymic="";
        client.stage=4;
        return "Ваше полное имя: "+client.FamilyName+" "+client.Name+" "+client.Patronymic+", верно?";
    }

    private String check(String text) {
        if (text.toLowerCase().contains("да")){
            client.stage=5;
            return "Введите вашу дату рождения в формате дд.мм.гггг";
        }
        if (text.toLowerCase().contains("нет")){
            client.stage=1;
            return "Введите ваше имя";
        }
        return "Извините, не могу понять ваш ответ";
    }

    private String Birthdate(String text) {
        try {
            Date date=new SimpleDateFormat("dd.MM.yyyy").parse(text);
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.YEAR,-100);
            Date earliest=instance.getTime();
            instance.add(Calendar.YEAR,82);
            Date latest=instance.getTime();
            if (date.before(earliest)||date.after(latest)) return "Недопустимое значение даты. Возможно вы ошиблись при вводе? Попробуйте ещё раз";
            client.stage=6;
            client.birthdate=date;
            return "Введите email";
        }
        catch (Exception e) {
            return "Некоректно введена дата. Попробуйте ещё раз";
        }
    }

    private String email(String text) {
        Pattern p = Pattern.compile("^[\\w-_.]*[\\w-_.]@[\\w].+[\\w]+[\\w]$");
        Matcher m = p.matcher(text);
        if (!m.matches()) return "Некоректный email";
        client.stage=7;
        client.email=text;
        return "Введите ваш номер телефона в виде набора цифр без пробелов и тире, с кодом страны и оператора.";
    }

    private String phone(String text) {
        Pattern p = Pattern.compile("\\d{11}");
        Matcher m = p.matcher(text);
        if (!m.matches()) return "Некоректно введён номер. Попробуйте ещё раз";
        client.phone=text;
        client.stage=8;
        Format formatter = new SimpleDateFormat("dd.MM.yyyy");
        return "Проверьте, пожалуйста, правильность информации:<br> " +
                "ФИО:"+client.FamilyName+" "+client.Name+" "+client.Patronymic+"<br>" +
                "Дата рождения: "+formatter.format(client.birthdate)+"<br>" +
                "Номер телефона:"+client.phone+"<br>" +
                "email: "+client.email+"<br>";
    }

    private String check2(String text) {
        if (text.toLowerCase().contains("да")){
            client.stage=9;
            return "Анкета заполненна";
        }
        if (text.toLowerCase().contains("нет")){
            client.stage=1;
            return "Введите ваше имя";
        }
        return "Извините, не могу понять ваш ответ";
    }


    private class Request{
            String Name;
            String FamilyName;
            String Patronymic;
            Date birthdate;
            String phone;
            String email;
            int stage;
        }

    class ShutdownHook extends Thread{

        public void run(){
            try{
                dout.flush();
                din.close();
                socket.close();}
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    }

    public static void main(String[] args) throws IOException {
        Server server=new Server();
    }
}
