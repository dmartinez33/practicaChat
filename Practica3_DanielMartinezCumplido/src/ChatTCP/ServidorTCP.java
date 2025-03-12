package ChatTCP;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorTCP {
    // Conjunto de clientes conectados
    private static Set<String> usuariosConectados = new HashSet<>();
    private static Map<String, DataOutputStream> clientes = new HashMap<>();

    //SERVIDOR PRINCIPAL
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Servidor de chat en espera de clientes...");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                new Manejador(clienteSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // MANEJADOR
    private static class Manejador extends Thread {
        private Socket socket;
        private String nickname;

        public Manejador(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                // Se reciben los flujos de entrada y salida
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                // Pedir y validar nickname
                dataOutputStream.writeUTF("Ingrese su nickname:");
                nickname = bufferedReader.readLine();
                while (usuariosConectados.contains(nickname)) {
                    dataOutputStream.writeUTF("Nickname ya en uso, ingrese otro:");
                    nickname = bufferedReader.readLine();
                }
                usuariosConectados.add(nickname);
                clientes.put(nickname, dataOutputStream);
                dataOutputStream.writeUTF("Bienvenido al chat, " + nickname);

                // Notificar a los demás clientes
                for (DataOutputStream writer : clientes.values()) {
                    try {
                        writer.writeUTF(nickname + " se ha unido al chat.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Recibir y reenviar mensajes
                String mensaje;
                while ((mensaje = bufferedReader.readLine()) != null) {
                    for (DataOutputStream writer : clientes.values()) {
                        try {
                            writer.writeUTF(nickname + ": " + mensaje);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (nickname != null) {
                        usuariosConectados.remove(nickname);
                        clientes.remove(nickname);
                        for (DataOutputStream writer : clientes.values()) {
                            try {
                                writer.writeUTF(nickname + " ha salido del chat.");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
