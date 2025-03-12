package ChatUDP;

import java.net.*;
import java.util.*;
import java.io.*;

public class ClienteUDP {
    private static DatagramSocket socket;
    private static MulticastSocket multicastSocket;
    private static String nickname;
    private static final String MULTICAST_ADDRESS = "225.0.0.1";
    private static final int MULTICAST_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Crear un socket de datagrama normal para enviar mensajes al servidor
            socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName("localhost");

            // Crear un socket multicast para recibir mensajes
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            InetAddress grupoMulticast = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(grupoMulticast);

            // Solicitar el nickname del usuario
            System.out.print("Ingresa tu nickname: ");
            nickname = scanner.nextLine();

            // Enviar el nickname al servidor
            String mensajeNickname = "NICKNAME:" + nickname;
            DatagramPacket datagramNickname = new DatagramPacket(mensajeNickname.getBytes(), mensajeNickname.length(), serverAddress, 9876);
            socket.send(datagramNickname);

            // Hilo para recibir mensajes del grupo multicast
            Thread recibirMensajes = new Thread(() -> {
                while (true) {
                    try {
                        byte[] buffer = new byte[1024];
                        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                        multicastSocket.receive(datagramPacket);
                        String mensaje = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                        System.out.println(mensaje);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            recibirMensajes.start();

            // Hilo para enviar mensajes al servidor
            Thread enviarMensajes = new Thread(() -> {
                while (true) {
                    String mensaje = scanner.nextLine();
                    if (!mensaje.trim().isEmpty()) {
                        String mensajeCompleto = nickname + ": " + mensaje;
                        try {
                            DatagramPacket datagramPacket = new DatagramPacket(mensajeCompleto.getBytes(), mensajeCompleto.length(), serverAddress, 9876);
                            socket.send(datagramPacket);  // Enviar el mensaje al servidor
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            enviarMensajes.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
