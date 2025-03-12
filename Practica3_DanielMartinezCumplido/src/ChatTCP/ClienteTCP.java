package ChatTCP;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClienteTCP {
    private static String nickname;
    private static DataOutputStream dataOutputStream;
    private static BufferedReader bufferedReader;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket("localhost", 12345)) {
            // Crear flujos de entrada y salida
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            // Solicitar y enviar nickname
            System.out.print("Ingrese su nickname: ");
            nickname = scanner.nextLine();
            dataOutputStream.writeUTF(nickname);

            // Leer mensajes del servidor en un hilo separado
            Thread recibirMensajes = new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = bufferedReader.readLine()) != null) {
                        System.out.println(mensaje);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            recibirMensajes.start();

            // Enviar mensajes
            String mensaje;
            while ((mensaje = scanner.nextLine()) != null) {
                dataOutputStream.writeUTF(mensaje);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}