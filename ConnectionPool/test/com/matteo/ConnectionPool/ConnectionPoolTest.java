package com.matteo.ConnectionPool;

import com.matteo.ConnectionPool.ConnectionNotAvailableException;
import com.matteo.ConnectionPool.ConnectionPool;
import com.matteo.ConnectionPool.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Assert;
import org.junit.Test;

public class ConnectionPoolTest {
    @Test(expected=ClassNotFoundException.class)
    public void wrongDriverName() throws ClassNotFoundException, IllegalArgumentException, SQLException {
        new com.matteo.ConnectionPool.ConnectionPool("toto", "localhost", "root", "root", 10, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void wrongInitArgumentGreaterThanMax() throws ClassNotFoundException, IllegalArgumentException, SQLException {
        new com.matteo.ConnectionPool.ConnectionPool("com.mysql.jdbc.Driver", "localhost", "root", "root", 10, 12);
    }

    @Test(expected=SQLException.class)
    public void wrongDbURLShouldRaiseException() throws ClassNotFoundException, IllegalArgumentException, SQLException {
        new com.matteo.ConnectionPool.ConnectionPool("com.mysql.jdbc.Driver", "testConnectionPool", "root", "wrongpassword", 10, 5);
    }

    @Test
    public void correctDbConfiguration() throws ClassNotFoundException, IllegalArgumentException, SQLException {
        ConnectionPool pool = new ConnectionPool("com.mysql.jdbc.Driver", "testConnectionPool", "root", "root", 10, 5);
        Assert.assertNotNull((Object)pool);
    }

    @Test
    public void numberOfInitialConnectionIsCorrect() throws ClassNotFoundException, IllegalArgumentException, SQLException {
        ConnectionPool pool = new ConnectionPool("com.mysql.jdbc.Driver", "testConnectionPool", "root", "root", 10, 5);
        Assert.assertEquals((long)pool.getNumberOfAvailableConnection(), (long)5);
    }

    @Test
    public void whenBorrowingMoreConnectionThanAvailableWeCreateMore() throws ClassNotFoundException, IllegalArgumentException, SQLException, InterruptedException, ConnectionNotAvailableException {
        ConnectionPool pool = new ConnectionPool("com.mysql.jdbc.Driver", "testConnectionPool", "root", "root", 10, 5);
        PooledConnection con1 = pool.borrowConnection();
        PooledConnection con2 = pool.borrowConnection();
        PooledConnection con3 = pool.borrowConnection();
        PooledConnection con4 = pool.borrowConnection();
        PooledConnection con5 = pool.borrowConnection();
        PooledConnection con6 = pool.borrowConnection();
        Assert.assertNotNull((String)"Connection 6 should not be null", (Object)con6);
        Assert.assertEquals((String)"Number of being used connection should be equal to 6", (long)pool.getNbOfBeingUsedConnection(), (long)6);
    }

    @Test(expected=ConnectionNotAvailableException.class)
    public void whenBorrowingMoreConnectionThanAvailableandAboveMaxWeDontCreateMore() throws ClassNotFoundException, IllegalArgumentException, SQLException, InterruptedException, ConnectionNotAvailableException {
        ConnectionPool pool = new ConnectionPool("com.mysql.jdbc.Driver", "testConnectionPool", "root", "root", 10, 5);
        PooledConnection con1 = pool.borrowConnection();
        PooledConnection con2 = pool.borrowConnection();
        PooledConnection con3 = pool.borrowConnection();
        PooledConnection con4 = pool.borrowConnection();
        PooledConnection con5 = pool.borrowConnection();
        PooledConnection con6 = pool.borrowConnection();
        PooledConnection con7 = pool.borrowConnection();
        PooledConnection con8 = pool.borrowConnection();
        PooledConnection con9 = pool.borrowConnection();
        PooledConnection con10 = pool.borrowConnection();
        PooledConnection con11 = pool.borrowConnection();
    }

    @Test
    public void whenBorrowingConnectionAndReleaseThemSateIsCorrect() throws Exception {
        ConnectionPool pool = new ConnectionPool("com.mysql.jdbc.Driver", "testConnectionPool", "root", "root", 10, 5);
        PooledConnection con1 = pool.borrowConnection();
        PooledConnection con2 = pool.borrowConnection();
        PooledConnection con3 = pool.borrowConnection();
        Assert.assertEquals((long)pool.getNbOfBeingUsedConnection(), (long)3);
        Assert.assertEquals((long)pool.getNumberOfAvailableConnection(), (long)2);
        pool.surrenderConnection((Connection)con3);
        Assert.assertEquals((long)pool.getNbOfBeingUsedConnection(), (long)2);
        Assert.assertEquals((long)pool.getNumberOfAvailableConnection(), (long)3);
        PooledConnection con4 = pool.borrowConnection();
        PooledConnection con5 = pool.borrowConnection();
        PooledConnection con6 = pool.borrowConnection();
        PooledConnection con7 = pool.borrowConnection();
        PooledConnection con8 = pool.borrowConnection();
        PooledConnection con9 = pool.borrowConnection();
        PooledConnection con10 = pool.borrowConnection();
        Assert.assertEquals((long)pool.getNbOfBeingUsedConnection(), (long)9);
        Assert.assertEquals((long)pool.getNumberOfAvailableConnection(), (long)0);
    }
}