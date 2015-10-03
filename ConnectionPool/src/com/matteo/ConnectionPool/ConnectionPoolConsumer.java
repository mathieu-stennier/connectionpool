package com.matteo.ConnectionPool;

import com.matteo.ConnectionPool.ConnectionNotAvailableException;
import com.matteo.ConnectionPool.ConnectionPool;
import com.matteo.ConnectionPool.PooledConnection;
import java.sql.SQLException;
import java.util.Random;

public class ConnectionPoolConsumer
implements Runnable {
    private ConnectionPool connectionPool;
    private String name = "";

    public ConnectionPoolConsumer(String name, ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
        this.name = name;
    }

    @Override
    public void run() {
        Random rand = new Random();
        long saveTime = System.currentTimeMillis();
        do {
            int randomnum = rand.nextInt(5);
            try {
                PooledConnection con = this.connectionPool.borrowConnection();
                this.connectionPool.shareWaitingTime((int)(System.currentTimeMillis() - saveTime));
                Thread.sleep(1000 * randomnum);
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ConnectionNotAvailableException e) {
                continue;
            }
            randomnum = rand.nextInt(10);
            try {
                Thread.sleep(1000 * randomnum);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            saveTime = System.currentTimeMillis();
        } while (true);
    }
}
