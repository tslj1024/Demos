package a.netty.server;

public class ServerStart {
    public static void main(String[] args) throws Exception {
        VisitorSocket.startServer();
        ServerSocket.startServer();
    }
}
