package Listener;

import Jobs.LimparReservasExpiradasJob;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Inicializador implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LimparReservasExpiradasJob.iniciar();
    }
}