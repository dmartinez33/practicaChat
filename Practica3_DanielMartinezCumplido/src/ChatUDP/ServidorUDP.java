package ChatUDP;

import java.net.*;
import java.util.*;

public class ServidorUDP {

    private static Set<String> usuariosConectados = new HashSet<>();
    private static final String MULTICAST_ADDRESS = "225.0.0.1";
    private static final int MULTICAST_PORT = 12345;

    public static void main(String[] args) {
        try (MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT)) {
            InetAddress grupoMulticast = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket.joinGroup(grupoMulticast);

            System.out.println("Servidor en espera de mensajes...");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

                // Recibir mensajes de los clientes
                multicastSocket.receive(datagramPacket);
                String mensaje = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                InetAddress clienteDireccion = datagramPacket.getAddress();
                int clientePuerto = datagramPacket.getPort();

                // Si el mensaje es un nuevo usuario, validamos su nickname
                if (mensaje.startsWith("NICKNAME:")) {
                    String nickname = mensaje.split(":")[1].trim();
                    // Verificar que no exista un nickname duplicado
                    if (usuariosConectados.contains(nickname)) {
                        // Enviar mensaje de error
                        String errorMsg = "El nickname " + nickname + " ya está en uso.";
                        DatagramPacket datagramError = new DatagramPacket(errorMsg.getBytes(), errorMsg.length(), clienteDireccion, clientePuerto);
                        multicastSocket.send(datagramError);
                    } else {
                        usuariosConectados.add(nickname);
                        // Notificar a todos los clientes sobre la llegada de un nuevo usuario
                        String bienvenida = "Nuevo usuario conectado: " + nickname;
                        DatagramPacket datagramMensaje = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupoMulticast, MULTICAST_PORT);
                        multicastSocket.send(datagramMensaje);
                    }
                } else {
                    // Reenviar el mensaje a todos los usuarios conectados mediante multicast
                    DatagramPacket paqueteMensaje = new DatagramPacket(mensaje.getBytes(), mensaje.length(), grupoMulticast, MULTICAST_PORT);
                    multicastSocket.send(paqueteMensaje);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}