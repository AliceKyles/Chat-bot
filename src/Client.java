import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import javax.swing.*;


public class Client extends JFrame{
    private Socket socket;
    private DataOutputStream dout;
    private DataInputStream din;
    private JTextArea textA;
    private JPanel scrollPanel;
    private JScrollPane mainArea;
    private int size;

    private Client() {
        super("Chat-bot");
        setBounds(100, 100, 700, 400);
        size=0;
        textA= new JTextArea(10,10);
        textA.setBorder(BorderFactory.createBevelBorder(0,Color.cyan,Color.CYAN));
        JButton buttonA=new JButton("Отправить");
        JButton buttonB=new JButton("Начать сначала");
        buttonA.setPreferredSize(new Dimension(150,40));
        buttonB.setPreferredSize(new Dimension(150,40));
        JPanel buttons=new JPanel();
        Action action=new SendMessage();
        Action action1=new Cancel();
        buttonA.addActionListener(action);
        buttonB.addActionListener(action1);
        buttons.add(buttonA);
        buttons.add(buttonB);
        scrollPanel=new JPanel();
        scrollPanel.setLayout(new BoxLayout(scrollPanel, BoxLayout.Y_AXIS));
        mainArea= new JScrollPane(scrollPanel);
        mainArea.setPreferredSize(new Dimension(100,350));
        receive("Здравствуйте! Хотите заполнить анкету?");
        resize();
        JPanel mainPanel=new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(mainArea);
        mainPanel.add(textA);
        add(mainPanel, BorderLayout.CENTER);
        add(buttons,BorderLayout.PAGE_END);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        InputMap input = textA.getInputMap();
        KeyStroke enter = KeyStroke.getKeyStroke("ENTER");
        input.put(enter, "text-submit");
        ActionMap actions = textA.getActionMap();
        actions.put("text-submit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonA.doClick();
            }
        });
        Client.ShutdownHook shutdownHook = new Client.ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    class SendMessage extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try{
                dout.writeUTF(textA.getText());
                send(textA.getText());
                textA.setText("");
                textA.setPreferredSize(new Dimension(700,50));
                receive(din.readUTF());
                resize();
            }
            catch (Exception e1) {
                e1.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private void resize(){
        scrollPanel.setPreferredSize(new Dimension (700,size*3/2));
        mainArea.validate();
        mainArea.getVerticalScrollBar().setValue(mainArea.getVerticalScrollBar().getMaximum());
    }

    class Cancel extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            try{
                din.close();
                dout.flush();
                socket.close();
                socket = new Socket("localhost", 5470);
                dout = new DataOutputStream(socket.getOutputStream());
                din = new DataInputStream(socket.getInputStream());
                receive("Здравствуйте! Хотите заполнить анкету?");
                resize();
            }
            catch (Exception e1) {
                e1.printStackTrace();
                System.exit(-1);
            }
        }
    }

    private void receive(String text){
        message("  Chat-bot: "+text);
    }

    private void send(String text){
        message("  Вы: "+text);
    }

    private void message(String text){
        scrollLabel labelPanel=new scrollLabel(text);
        scrollPanel.add(labelPanel.labelPanel);
    }

    private class scrollLabel{
        private JPanel labelPanel;

        scrollLabel(String text){
            JLabel label = new JLabel("<html><p style=\"width:500px\">"+text+"</p></html>");
            label.setOpaque(true);
            label.setVerticalAlignment(JLabel.TOP);
            int n=(label.getText().length()/120+1)*30+8*(text.length() - text.replace("<br>", "").length())/4;
            label.setPreferredSize(new Dimension(665,n));
            size+=n;
            label.setBackground(Color.white);
            label.setBorder(BorderFactory.createLineBorder(Color.black));
            labelPanel=new JPanel();
            labelPanel.add(label);
        }
    }

    private void run() throws IOException {
        socket = new Socket("localhost", 5470);
        try{
            dout = new DataOutputStream(socket.getOutputStream());
            din = new DataInputStream(socket.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
            dout.flush();
            din.close();
            socket.close();
            System.exit(-1);
        }


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

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.setVisible(true);
        client.run();
    }
}

