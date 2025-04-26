package Listener;

import Jobs.LimparReservasExpiradasJob;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class Inicializador implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(Inicializador.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            LOGGER.info("Iniciando jobs do sistema...");
            LimparReservasExpiradasJob.iniciar();
            LOGGER.info("Jobs iniciados com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha ao iniciar jobs do sistema", e);
            throw new RuntimeException("Falha na inicialização do sistema", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            LOGGER.info("Parando jobs do sistema...");
            LimparReservasExpiradasJob.parar();
            LOGGER.info("Jobs parados com sucesso");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha ao parar jobs do sistema", e);
        }
    }
}