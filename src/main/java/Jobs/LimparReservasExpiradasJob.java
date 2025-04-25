package Jobs;

import Util.Conexao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LimparReservasExpiradasJob {

    public static void iniciar() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try (Connection conn = Conexao.getConnectionMySQL()) {

                StringBuilder sql = new StringBuilder("DELETE FROM estoque_reserva WHERE res_data < NOW() - INTERVAL 6 MINUTE");

                try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                    int removidos = ps.executeUpdate();
                    System.out.println("Reservas expiradas removidas: " + removidos);
                }

            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }, 0, 3, TimeUnit.MINUTES);
    }
}
