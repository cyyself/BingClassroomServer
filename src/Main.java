import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Main {
    static ServerSocket server;
    public static Map<Long,Shape> graph_store;
    public static Set<PrintStream> clients;
    public static void main(String[] argv) throws IOException {
        graph_store = new ConcurrentHashMap<Long,Shape>();//ConcurrentHashMap自带线程安全
        clients = new HashSet<PrintStream>();
        try {
            server = new ServerSocket(2333);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Server listening at port:" + server.getLocalPort());
        System.out.println("Local IP:");
        printAllInterfaceIP();
        Socket client;
        while (true) {
            client = server.accept();
            System.out.println("New Client from " +client.getRemoteSocketAddress().toString());
            new ClientThread(client).start();
        }
    }
    public static void broadcast(String s) {
        for (PrintStream x : clients) {
            x.println(s);
            x.flush();
        }
    }
    public static void printAllInterfaceIP() {
        Set<String> result = new HashSet<String>();
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> addresses=ni.getInetAddresses();
                while(addresses.hasMoreElements()){
                    ip = addresses.nextElement();
                    if (!ip.isLoopbackAddress() && !ip.isLinkLocalAddress()) {
                        result.add(ip.getHostAddress());
                    }
                }
            }
            for (String x: result) {
                System.out.println("\t"+x);
            }
        } catch (Exception e) {

        }
    }
}
class ClientThread extends Thread {
    public Socket client;
    public BufferedReader buf;
    public PrintStream out;
    public boolean conn = true;
    public ClientThread(Socket _client) {
        client = _client;
    }
    public void run() {
        try {
            buf = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintStream(client.getOutputStream(),true);
            Main.clients.add(out);
        } catch (IOException e) {
            conn = false;
            e.printStackTrace();
            return;
        }
        //send existing graph
        for (Map.Entry<Long, Shape> entry : Main.graph_store.entrySet()) {
            out.println(entry.getValue().serialize()+" "+entry.getKey());
        }
        //sync all
        while (conn) {
            try {
                String data = buf.readLine();
                if (data == null || data.equals("")) {
                    conn = false;
                    break;
                }
                String[] param = data.split(" ");
                if (param[0].equals("clear")) {
                    Main.graph_store.clear();
                    Main.broadcast("clear");
                }
                else if (param[0].equals("delete")) {
                    Main.graph_store.remove(Long.parseLong(param[1]));
                    Main.broadcast("delete "+param[1]);
                }
                else if (param[0].equals("circle")) {
                    MyCircle to_put = new MyCircle(
                            new MyPoint(Integer.parseInt(param[1]),Integer.parseInt(param[2])),
                            new MyPoint(Integer.parseInt(param[3]),Integer.parseInt(param[4])),
                            new Color(Integer.parseInt(param[5]),Integer.parseInt(param[6]),Integer.parseInt(param[7]))
                    );
                    to_put.size = Integer.parseInt(param[8]);
                    to_put.move_enable = param[9].equals("1");
                    Main.graph_store.put(Long.parseLong(param[10]),to_put);
                    Main.broadcast(data);
                }
                else if (param[0].equals("line")) {
                    MyLine to_put = new MyLine(
                            new MyPoint(Integer.parseInt(param[1]),Integer.parseInt(param[2])),
                            new MyPoint(Integer.parseInt(param[3]),Integer.parseInt(param[4])),
                            new Color(Integer.parseInt(param[5]),Integer.parseInt(param[6]),Integer.parseInt(param[7]))
                    );
                    to_put.size = Integer.parseInt(param[8]);
                    to_put.move_enable = param[9].equals("1");
                    Main.graph_store.put(Long.parseLong(param[10]),to_put);
                    Main.broadcast(data);
                }
                else if (param[0].equals("rect")) {
                    MyRectangle to_put = new MyRectangle(
                            new MyPoint(Integer.parseInt(param[1]),Integer.parseInt(param[2])),
                            new MyPoint(Integer.parseInt(param[3]),Integer.parseInt(param[4])),
                            new Color(Integer.parseInt(param[5]),Integer.parseInt(param[6]),Integer.parseInt(param[7]))
                    );
                    to_put.size = Integer.parseInt(param[8]);
                    to_put.move_enable = param[9].equals("1");
                    Main.graph_store.put(Long.parseLong(param[10]),to_put);
                    Main.broadcast(data);
                }
                else if (param[0].equals("rectf")) {
                    MyRectangleFill to_put = new MyRectangleFill(
                            new MyPoint(Integer.parseInt(param[1]),Integer.parseInt(param[2])),
                            new MyPoint(Integer.parseInt(param[3]),Integer.parseInt(param[4])),
                            new Color(Integer.parseInt(param[5]),Integer.parseInt(param[6]),Integer.parseInt(param[7]))
                    );
                    to_put.size = Integer.parseInt(param[8]);
                    to_put.move_enable = param[9].equals("1");
                    Main.graph_store.put(Long.parseLong(param[10]),to_put);
                    Main.broadcast(data);
                }
                else if (param[0].equals("triangle")) {
                    MyTriangle to_put = new MyTriangle(
                            new MyPoint(Integer.parseInt(param[1]),Integer.parseInt(param[2])),
                            new MyPoint(Integer.parseInt(param[3]),Integer.parseInt(param[4])),
                            new MyPoint(Integer.parseInt(param[10]),Integer.parseInt(param[11])),
                            new Color(Integer.parseInt(param[5]),Integer.parseInt(param[6]),Integer.parseInt(param[7]))
                    );
                    to_put.size = Integer.parseInt(param[8]);
                    to_put.move_enable = param[9].equals("1");
                    Main.graph_store.put(Long.parseLong(param[12]),to_put);
                    Main.broadcast(data);
                }
                else System.out.println("error msg");
            } catch (IOException | NullPointerException e) {
                conn = false;
            }
        }
        Main.clients.remove(out);
    }
}