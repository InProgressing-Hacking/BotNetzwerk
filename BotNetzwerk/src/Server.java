import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

    private static int width = 1920 / 2, height = 1080 / 2;
    private static JFrame j;
    private static Draw draw;
    private static String title = "BotControl - Version 0.1 - What happens when the state falls?";

    private static Server server;

    private static ArrayList<String> messages = new ArrayList<>();

    private static int whoScrollConsole = 0;
    private static int maxDrawStringHeightConsole = 50;
    private static int maxDrawStringWidthConsole = 135;
    private static int mustRemoveTConsole = 21;

    private static JButton consoleButton, botsView;
    private static JTextField consoleCommands;

    private static Color backgroundConsole = new Color(32, 11, 102, 255);
    private static Color fordergroundConsole = new Color(29, 122, 19, 255);

    private static ModuleManager moduleManager;

    public static void main(String[] args) {
        server = new Server();
        server.createWindow("Start");
    }

    private void createWindow(String use) {
        moduleManager = new ModuleManager();
        if (use.equals("Start")) {
            j = new JFrame();
            j.setTitle(title);
            j.setSize(width, height);
            j.setLocationRelativeTo(null);
            j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            j.setResizable(false);
            j.setLayout(null);
            j.addMouseWheelListener(new MouseWheel());

            consoleButton = new JButton("Console");
            consoleButton.setBackground(new Color(1, 1, 1, 255));
            consoleButton.setForeground(new Color(226, 232, 255, 255));
            consoleButton.setBounds(0, 0, 150, 30);
            consoleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Draw.use = "Console";
                    j.setTitle(title + " Console");
                    repaint();
                    updateGuiObjects();
                }
            });
            consoleButton.setVisible(true);
            j.add(consoleButton);

            botsView = new JButton("BotView");
            botsView.setBackground(new Color(1, 1, 1, 255));
            botsView.setForeground(new Color(226, 232, 255, 255));
            botsView.setBounds(150, 0, 150, 30);
            botsView.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Draw.use = "BotView";
                    j.setTitle(title + " BotView");
                    repaint();
                    updateGuiObjects();
                }
            });
            botsView.setVisible(true);
            j.add(botsView);

            consoleCommands = new JTextField();
            consoleCommands.setForeground(fordergroundConsole);
            consoleCommands.setBackground(backgroundConsole);
            consoleCommands.setBounds(0, height-64, width, 25);
            consoleCommands.setFont(new Font("Impact", Font.PLAIN, 16));
            consoleCommands.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(!consoleCommands.getText().equals("")) {
                        if (!moduleManager.check(consoleCommands.getText())) {
                            addMessage("Cannot find command \"" + consoleCommands.getText() + "\"!");
                        }
                        consoleCommands.setText("");
                    }
                }
            });
            consoleCommands.setVisible(true);
            j.add(consoleCommands);

            Draw.use = "Console";

            draw = new Draw();
            draw.setBounds(0, 0, width, height);
            draw.setVisible(true);
            j.add(draw);

            j.setVisible(true);
            update(60);
            updateGuiObjects();
            registerModules();
        }
    }

    private class MouseWheel implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(e.getWheelRotation() == -1){
                if(whoScrollConsole > 1) {
                    whoScrollConsole--;
                }
            }
            if(e.getWheelRotation() == 1){
                whoScrollConsole++;
            }
        }
    }

    private static class Draw extends JLabel {
        private static String use;

        @Override
        protected void paintComponent(Graphics g1) {
            super.paintComponent(g1);
            Graphics2D g = (Graphics2D) g1;

            g.setColor(backgroundConsole);
            g.fillRect(0, 0, width, height);

            if(Draw.use.equals("Console")) {
                g.setColor(fordergroundConsole);
                Font strings = new Font("Impact", Font.PLAIN, 15);

                int y = 50;
                if (messages.size() > whoScrollConsole + maxDrawStringHeightConsole) {
                    for (int i = whoScrollConsole; i < whoScrollConsole + maxDrawStringHeightConsole; i++) {
                        for (String text : splitString(maxDrawStringWidthConsole, messages.get(i))) {
                            drawCustomString(g, strings, text, 5, y);
                            y += 15;
                        }
                    }
                } else {
                    for (int i = whoScrollConsole; i < messages.size(); i++) {
                        for (String text : splitString(maxDrawStringWidthConsole, messages.get(i))) {
                            drawCustomString(g, strings, text, 5, y);
                            y += 15;
                        }
                    }
                }
            }
        }
    }

    private static void repaint(){
        draw.repaint();
    }

    private static void update(int fps){
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 0, 1000 / fps);
    }

    private static List<String> splitString(int max, String message){
        List<String> r = new ArrayList<>();
        if(message.length() < max){
            r.add(message);
        } else {
            int i = 0;
            do {
                if(i+max > message.length()){
                    r.add(message.substring(i));
                } else {
                    r.add(message.substring(i, i+max));
                }
                i += max;
            } while (i < message.length());
        }
        return r;
    }

    private static void drawCustomString(Graphics2D g, Font font, String text, int posx, int posy){
        g.setFont(font);
        if(text.contains("§")) {
            String[] sp = text.split("§");
            for (int i = sp.length; i > 0; i--) {
                if (sp[i - 1].startsWith("0")) {
                    g.setColor(new Color(19, 90, 109, 255));
                }
                if (sp[i - 1].startsWith("1")) {
                    g.setColor(Color.GREEN);
                }
                if (sp[i - 1].startsWith("2")) {
                    g.setColor(Color.RED);
                }
                if (sp[i - 1].startsWith("3")) {
                    g.setColor(Color.GRAY);
                }
                if (sp[i - 1].startsWith("4")) {
                    g.setColor(Color.GREEN);
                }
                if (sp[i - 1].startsWith("5")) {
                    g.setColor(Color.MAGENTA);
                }
                if (sp[i - 1].startsWith("6")) {
                    g.setColor(Color.PINK);
                }
                if (sp[i - 1].startsWith("7")) {
                    g.setColor(Color.ORANGE);
                }
                if (sp[i - 1].startsWith("8")) {
                    g.setColor(Color.RED);
                }
                if (sp[i - 1].startsWith("9")) {
                    g.setColor(Color.WHITE);
                }
                if (sp[i - 1].startsWith("0")) {
                    g.setColor(Color.YELLOW);
                }
                String set = "";
                for (int x = -1; x < i - 1; x++) {
                    set += replaceStringForDrawing(sp[x + 1]);
                }
                g.drawString(set, posx, posy);
            }
        } else {
            g.drawString(text, posx, posy);
        }
    }

    private static String replaceStringForDrawing(String s){
        if(s.startsWith("0")){
            s = s.replaceAll("0", "");
        } else if(s.startsWith("1")){
            s = s.replaceAll("1", "");
        } else if(s.startsWith("2")){
            s = s.replaceAll("2", "");
        } else if(s.startsWith("3")){
            s = s.replaceAll("3", "");
        } else if(s.startsWith("4")){
            s = s.replaceAll("4", "");
        } else if(s.startsWith("5")){
            s = s.replaceAll("5", "");
        } else if(s.startsWith("6")){
            s = s.replaceAll("6", "");
        } else if(s.startsWith("7")){
            s = s.replaceAll("7", "");
        } else if(s.startsWith("8")){
            s = s.replaceAll("8", "");
        } else if(s.startsWith("9")){
            s = s.replaceAll("9", "");
        } else if(s.startsWith("10")) {
            s = s.replaceAll("10", "");
        }
        return s;
    }

    private static void updateGuiObjects(){
        consoleCommands.setVisible(false);

        consoleButton.setVisible(false);
        botsView.setVisible(false);

        if(Draw.use.equals("Console")){
            consoleButton.setVisible(true);
            botsView.setVisible(true);
            consoleCommands.setVisible(true);
        }
        if(Draw.use.equals("BotView")){
            consoleButton.setVisible(true);
            botsView.setVisible(true);
        }
    }

    private void addMessage(String message){
        messages.add(message);
        if(getMessages().size() > maxDrawStringHeightConsole - mustRemoveTConsole) {
            whoScrollConsole++;
        }
    }

    private void setMessage(String message, int index){
        messages.set(index, message);
    }

    private void setLasteMessagte(String message){
        messages.set(messages.size()-1, message);
    }

    private ArrayList<String> getMessages(){
        return messages;
    }

    public static class EFile {
        public static String getOwner(Path path){
            String r = null;
            try {
                FileOwnerAttributeView ownerAttributeView = Files.getFileAttributeView(path, FileOwnerAttributeView.class);
                UserPrincipal owner = ownerAttributeView.getOwner();
                r = owner.getName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return r;
        }
    }

    private abstract class Module {
        private String name;
        private String description;
        private String command;

        public Module(String name, String description, String command){
            this.name = name;
            this.description = description;
            this.command = command;
        }

        public abstract void run(String[] args);
    }

    private static class ModuleManager {
        private static List<Module> sp = new ArrayList<>();

        private void addModule(Module m) {
            sp.add(m);
        }

        private List<Module> getModules() {
            return sp;
        }

        private Module getModule(String name) {
            Module r = null;
            for (Module m : getModules()) {
                if (m.name.equals(name)) {
                    r = m;
                }
            }
            return r;
        }

        private boolean check(String command) {
            boolean found = false;
            if (!command.equals("help")) {
                for (Module m : getModules()) {
                    if (command.startsWith(m.command)) {
                        m.run(command.split(" "));
                        found = true;
                    }
                }
            } else {
                found = true;
                for(Module m : getModules()){
                    new Server().addMessage(m.name + " -> " + m.command + " - " + m.description);
                }
            }
            return found;
        }
    }

    private void registerModules() {
        moduleManager.addModule(new ModuleStartServer());
        moduleManager.addModule(new ModuleCreateWorldList());
    }

    private class ModuleStartServer extends Module {

        public ModuleStartServer() {
            super("StartServer", "startserver <port>", "startserver");
        }

        @Override
        public void run(String[] args) {
            int port = Integer.parseInt(args[1]);
        }
    }

    private class ModuleCreateWorldList extends Module {

        public ModuleCreateWorldList() {
            super("Worldlist", "worldlist <use> <length> <file>", "createworldlist");
        }

        @Override
        public void run(String[] args) {
            String use = args[1];
            int length = Integer.valueOf(args[2]);
            File file = new File(args[3] + ".worldlist");
            new Thread(new runWorldList(use.toCharArray(), length, file)).start();
        }

        private class runWorldList implements Runnable {
            private char[] use;
            private int length;
            private File file;

            double max;
            int doedps = 0;

            public runWorldList(char[] use, int length, File file){
                this.use = use;
                this.length = length;
                this.file = file;
                max = Math.pow(use.length, length);
            }

            private List<String> sp = new ArrayList<>();
            private String prefix = "";

            @Override
            public void run() {
                addMessage("Generating passwords.. 0% complete.");
                for(int i = 0; i <= 100; i++) {
                    if(recurseHack("", i)) {
                        break;
                    }
                }
                addMessage("Save file.. 0% complete.");
                save(file, sp);
            }

            private void save(File file, List<String> s){
                try {
                    FileWriter writer = new FileWriter(file);

                    int i = 0;
                    for(String ps : s) {
                        writer.write(ps);
                        writer.write(System.getProperty("line.separator"));
                        i++;
                      //  double doed = (100 / (max / i)) / 100;
                        long doed = Math.round(100 / max * i);
                        setLasteMessagte("Save file.. " + doed + "% complete.");
                    }
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            private boolean recurseHack(String prefix, int n) {
                if(prefix.length() < length+1) {
                    doedps++;
                    long doed = Math.round(100 / max * doedps) / 100;
                    setLasteMessagte("Generating passwords.. " + doed + "% complete.");
                    if(n == 0) {
                        sp.add(prefix);
                        return false;
                    }
                    for(char c : use) {
                        if(recurseHack(prefix+c, n-1)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }
}
