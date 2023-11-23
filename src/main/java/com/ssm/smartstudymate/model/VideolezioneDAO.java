package com.ssm.smartstudymate.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class VideolezioneDAO {
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
}

