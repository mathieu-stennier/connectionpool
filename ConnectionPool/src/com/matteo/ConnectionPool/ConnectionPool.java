package com.matteo.ConnectionPool;

import com.matteo.ConnectionPool.ConnectionNotAvailableException;
import com.matteo.ConnectionPool.PooledConnection;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool
implements Runnable {
    private int maxNbConnections;
    private int initNbConnections;
    private int waitingTimeAverageSum = 0;
    private int waitingTimeAverageCase = 0;
    private String dbUrl;
    private String username;
    private String password;
    private LinkedBlockingQueue<PooledConnection> availableConnection;
    private LinkedBlockingQueue<PooledConnection> beingUsedConnection;

    public ConnectionPool(String dbDriver, String db, String user, String password, int max, int init) throws ClassNotFoundException, SQLException, IllegalArgumentException {
        Class.forName(dbDriver);
        this.availableConnection = new LinkedBlockingQueue(max);
        this.beingUsedConnection = new LinkedBlockingQueue(max);
        this.maxNbConnections = max;
        this.initNbConnections = init;
        this.dbUrl = "jdbc:mysql://localhost:8889/" + db;
        this.username = user;
        this.password = password;
        if (this.initNbConnections > this.maxNbConnections) {
            throw new IllegalArgumentException("The number of initial connection '" + this.initNbConnections + "' is greater than the maximum connection we can have (" + this.maxNbConnections + ")");
        }
        for (int i = 0; i < this.initNbConnections; ++i) {
            Connection newCon = DriverManager.getConnection(this.dbUrl, this.username, this.password);
            this.availableConnection.add(new PooledConnection(newCon, this));
        }
    }

    public synchronized void surrenderConnection(Connection pooledConnection) throws Exception {
        if (pooledConnection instanceof PooledConnection && pooledConnection.isValid(1000)) {
            if (!pooledConnection.getAutoCommit()) {
                pooledConnection.rollback();
            }
            pooledConnection.clearWarnings();
        } else {
            throw new Exception("The Connection is invalid and cannot be pooled back.");
        }
        this.beingUsedConnection.remove(pooledConnection);
        this.availableConnection.add((PooledConnection)pooledConnection);
    }

    public synchronized void shareWaitingTime(int milis) {
        ++this.waitingTimeAverageCase;
        this.waitingTimeAverageSum+=milis;
    }

    public synchronized int getAverageWaitingTime() {
        return this.waitingTimeAverageSum / this.waitingTimeAverageCase;
    }

    public synchronized int getNumberofWaitingTimeTestCases() {
        return this.waitingTimeAverageCase;
    }

    public synchronized PooledConnection borrowConnection() throws SQLException, InterruptedException, ConnectionNotAvailableException {
        if (!this.availableConnection.isEmpty()) {
            PooledConnection con = this.availableConnection.poll();
            this.beingUsedConnection.add(con);
            return con;
        }
        if (this.beingUsedConnection.size() < this.maxNbConnections) {
            Connection newCon = DriverManager.getConnection(this.dbUrl, this.username, this.password);
            PooledConnection newPooledCon = new PooledConnection(newCon, this);
            this.beingUsedConnection.add(newPooledCon);
            return newPooledCon;
        }
        if (this.availableConnection.peek() == null) {
            throw new ConnectionNotAvailableException();
        }
        return this.availableConnection.poll(0, TimeUnit.SECONDS);
    }

    public synchronized int getNumberOfAvailableConnection() {
        return this.availableConnection.size();
    }

    public synchronized int getNbOfBeingUsedConnection() {
        return this.beingUsedConnection.size();
    }

    @Override
    public void run() {
        do {
            System.out.println("Average waiting time: " + this.getAverageWaitingTime() + " (#=" + this.getNumberofWaitingTimeTestCases() + ")    /   av=" + this.availableConnection.size() + "  used=" + this.beingUsedConnection.size());
            try {
                Thread.sleep(1000);
                continue;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }
        } while (true);
    }
}
