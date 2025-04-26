package Jobs;

import Util.Conexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LimparReservasExpiradasJob {
    private static final Logger LOGGER = Logger.getLogger(LimparReservasExpiradasJob.class.getName());
    private static final int INTERVALO_EXECUCAO_MINUTOS = 1;
    private static final int TEMPO_EXPIRACAO_MINUTOS = 3;
    private static ScheduledExecutorService scheduler;

    public static void iniciar() {
        if (scheduler != null && !scheduler.isShutdown()) {
            LOGGER.warning("O agendador já está em execução");
            return;
        }

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
                () -> {
                    LOGGER.info("Execução do job iniciada");
                    executarLimpeza();
                    LOGGER.info("Execução do job concluída");
                },
                0,
                INTERVALO_EXECUCAO_MINUTOS,
                TimeUnit.MINUTES
        );

        LOGGER.info("Job de limpeza de reservas expiradas iniciado (intervalo: "
                + INTERVALO_EXECUCAO_MINUTOS + " minutos)");
    }

    private static void executarLimpeza() {
        LOGGER.info("Preparando para executar limpeza de reservas expiradas...");

        try (Connection conn = Conexao.getConnectionMySQL();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE estoque_reserva SET res_status = 'EXPIRADO' WHERE res_data < NOW() - INTERVAL ? MINUTE AND res_status = 'ATIVO'")) {

            LOGGER.info("Conexão estabelecida, executando limpeza...");
            ps.setInt(1, TEMPO_EXPIRACAO_MINUTOS);
            int removidos = ps.executeUpdate();

            if (removidos > 0) {
                LOGGER.log(Level.INFO, "Reservas expiradas removidas: {0}", removidos);
            } else {
                LOGGER.info("Nenhuma reserva expirada encontrada para remoção");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Driver JDBC não encontrado", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erro ao acessar o banco de dados", e);
        }
    }

    public static void parar() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Interrupção durante o desligamento do scheduler", e);
            }
            LOGGER.info("Job de limpeza de reservas expiradas parado");
        }
    }
}