package com.ssm.smartstudymate.model;

import java.sql.*;
import java.util.ArrayList;

public class VideolezioneDAO {

    public ArrayList<Videolezione> doRetrieveByDocente(Docente docente){
        ArrayList<Videolezione> videolezioni = new ArrayList<>();

        try (Connection connection = ConPool.getConnection()) {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT * FROM videolezione " +
                            "WHERE proprietario = ?;");

            preparedStatement.setString(1, docente.getEmail());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Videolezione videolezione = new Videolezione();

                videolezione.setTitolo(resultSet.getString("titolo"));
                videolezione.setDescrizione(resultSet.getString("descrizione"));
                videolezione.setUrlVideo(resultSet.getString("url"));
                videolezione.setUrlPhotoVideo(resultSet.getString("url_photo"));

                videolezioni.add(videolezione);
            }

        } catch (
                SQLException e) {
            e.printStackTrace();
        }

        return videolezioni;
    }

    public ArrayList<Videolezione> doRetrieveByAccessCode(String accessCode){
        ArrayList<Videolezione> videolezioni = new ArrayList<>();

        try (Connection connection = ConPool.getConnection()) {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT * FROM videolezione " +
                            "WHERE access_code = ?;");

            preparedStatement.setString(1, accessCode);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Videolezione videolezione = new Videolezione();

                videolezione.setTitolo(resultSet.getString("titolo"));
                videolezione.setDescrizione(resultSet.getString("descrizione"));
                videolezione.setUrlVideo(resultSet.getString("url"));
                videolezione.setUrlPhotoVideo(resultSet.getString("url_photo"));

                videolezioni.add(videolezione);
            }

        } catch (
                SQLException e) {
            e.printStackTrace();
        }

        return videolezioni;
    }

    public void doSave(Videolezione videolezione, Docente docente){

        try(Connection connection = ConPool.getConnection()) {
            PreparedStatement ps =
                    connection.prepareStatement("insert into videolezione (url, titolo, descrizione, url_photo, proprietario) " +
                            "VALUES(?,?,?,?,?);");
            ps.setString(1, videolezione.getUrlVideo());
            ps.setString(2, videolezione.getTitolo());
            ps.setString(3, videolezione.getDescrizione());
            ps.setString(4, videolezione.getUrlPhotoVideo());
            ps.setString(5, docente.getEmail());

            if(ps.executeUpdate() != 1)
                throw new RuntimeException("INSERT Error");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Videolezione doRetrieveByUrl(String url){
        Videolezione videolezione = null;

        try (Connection connection = ConPool.getConnection()) {
            PreparedStatement preparedStatement =
                    connection.prepareStatement("SELECT * FROM videolezione " +
                            "WHERE url = ?;");

            preparedStatement.setString(1, url);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                videolezione = new Videolezione();
                videolezione.setTitolo(resultSet.getString("titolo"));
                videolezione.setDescrizione(resultSet.getString("descrizione"));
                videolezione.setUrlVideo(resultSet.getString("url"));
                videolezione.setUrlPhotoVideo(resultSet.getString("url_photo"));
            }

        } catch (
                SQLException e) {
            e.printStackTrace();
        }

        return videolezione;
    }
}

