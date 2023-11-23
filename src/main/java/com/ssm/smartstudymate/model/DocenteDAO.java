package com.ssm.smartstudymate.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DocenteDAO {

    public boolean isPrimoAccesso(String email){

        try(Connection connection = ConPool.getConnection()){
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT primo_accesso " +
                                                    "FROM docente " +
                                                    "WHERE email = ?;");

            preparedStatement.setString(1, email);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return resultSet.getBoolean("primo_accesso");
            }
            else
                return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void doSavePassword(Docente docente){
        try(Connection connection = ConPool.getConnection()){
            PreparedStatement preparedStatement =
                    connection.prepareStatement("UPDATE docente SET passwordHash = SHA1(?), primo_accesso = false WHERE email = ?;");

            preparedStatement.setString(1, docente.getPassword());
            preparedStatement.setString(2, docente.getEmail());

            if(preparedStatement.executeUpdate() != 1)
                throw new RuntimeException("UPDATE ADMIN USER error");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Docente doRetrieveByEmailPassword(Docente docente) {
        Docente doRetrieve = new Docente();

        try(Connection connection = ConPool.getConnection()){
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT * " +
                            "FROM docente " +
                            "WHERE email = ? AND passwordhash = SHA1(?);");


            preparedStatement.setString(1, docente.getEmail());
            preparedStatement.setString(2, docente.getPassword());

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                doRetrieve.setEmail(resultSet.getString("email"));
                doRetrieve.setNome(resultSet.getString("nome"));
                doRetrieve.setCognome(resultSet.getString("cognome"));
                doRetrieve.setPassword(resultSet.getString("passwordHash"));
            }
            else
                return null;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return doRetrieve;
    }
}
