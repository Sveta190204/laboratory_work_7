package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet; // Добавьте этот импорт
import java.sql.SQLException;

public class WebScraper {

    // Данные для подключения к базе данных PostgreSQL
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/get_emails";
    private static final String DB_USER = "postgres"; 
    private static final String DB_PASSWORD = "1234";

    public static void main(String[] args) {
        String websiteUrl = "https://tsutmb.ru/";

        try {
            // Загружаем HTML-страницу
            Document document = Jsoup.connect(websiteUrl).get();

            // Проходимся по всем ссылкам на странице
            for (Element element : document.select("a[href]")) {
                String link = element.attr("abs:href");

                // Если ссылка содержит email
                if (link.startsWith("mailto:")) {
                    String email = link.substring(7); // Убираем "mailto:"
                    saveToDatabase(link, email, "email");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для сохранения данных в базу данных
    private static void saveToDatabase(String link, String subject, String type) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Проверка на наличие записи с такой же комбинацией link и subject в базе данных
            String checkSql = "SELECT COUNT(*) FROM email_data WHERE link = ? AND subject = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setString(1, link);
            checkStatement.setString(2, subject);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            int count = resultSet.getInt(1);

            // Если записи не существует, добавляем ее
            if (count == 0) {
                String sql = "INSERT INTO email_data (link, subject, type) VALUES (?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, link);
                preparedStatement.setString(2, subject);
                preparedStatement.setString(3, type);
                preparedStatement.executeUpdate();
                System.out.println("Сохранено: " + subject);
            } else {
                System.out.println("Email уже существует: " + subject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
