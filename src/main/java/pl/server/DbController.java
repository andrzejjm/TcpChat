package pl.server;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class DbController {

    private Connection connection;

    public DbController() throws SQLException{
        connect();
    }

    private void connect() throws SQLException{
        connection = null;

        connection = DriverManager.getConnection("jdbc:sqlite:chatBase.db");

    }

    private void disconnect() throws SQLException{
        if(connection != null) {
            connection.close();
        }
    }

    public boolean register(String login, String password) throws SQLException {
        System.out.println("Rejestruję..");

        String SQLline = "INSERT INTO users (Login, Password) VALUES (?, ?);";

        PreparedStatement preparedStatement = connection.prepareStatement(SQLline);
        preparedStatement.setString(1, login);
        preparedStatement.setString(2, hashPassword(password));

        preparedStatement.executeUpdate();

        return true;
    }

    public boolean login(String login, String password) throws SQLException{
        String SQLline = "SELECT Password FROM users WHERE Login LIKE ?;";

        PreparedStatement preparedStatement = connection.prepareStatement(SQLline);
        preparedStatement.setString(1, login);

        ResultSet resultSet = preparedStatement.executeQuery();
        String hashPass = null;

        while(resultSet.next()) {
            hashPass = resultSet.getString("Password");
        }

        if(BCrypt.checkpw(password, hashPass)) {
            return true;
        } else {
            return false;
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
