package com.example.application.ApplicationServer.Controller;
import com.example.application.ApplicationServer.Entity.RankRecord;
import org.springframework.stereotype.Repository;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


@Repository
public class DatabaseAccess {

    // 接続設定
    private Connection open() throws SQLException {
        String url = "jdbc:mysql://sql.yamazaki.se.shibaura-it.ac.jp:13308/db_group_b";
        String user = "group_b";
        String pass = "group_b";
        return DriverManager.getConnection(url, user, pass);
    }

    public boolean incrementRankCount(String username, int rankNum) {
        // カラム名を動的に指定（rank1, rank2...）
        String columnName = "rank" + rankNum;
        final String sql = "UPDATE account SET " + columnName + " = " + columnName + " + 1 WHERE username = ?";
        
        try (Connection con = open();
            PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            System.out.println("Incrementing " + columnName + " for user: " + username);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("リザルト更新（incrementRankCount）に失敗しました", e);
        }
    }

    public RankRecord getRankRecordByUsername(String username) {
        final String sql = "SELECT rank1, rank2, rank3, rank4 FROM account WHERE username = ?";
        
        try (Connection con = open();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new RankRecord(
                        rs.getInt("rank1"),
                        rs.getInt("rank2"),
                        rs.getInt("rank3"),
                        rs.getInt("rank4")
                    );
                } else {
                    return new RankRecord(0, 0, 0, 0); 
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("戦績取得（getRankRecordByUsername）に失敗しました", e);
        }
    }
}