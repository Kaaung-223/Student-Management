package com.example.student_management_system.Controller.DataBase;

import com.example.student_management_system.Controller.Model.ClassData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClassDAO {


    // =========================================================
    // GET ALL CLASSES
    // =========================================================

    public List<ClassData> getAllClasses() {

        List<ClassData> classes = new ArrayList<>();

        String sql =
                "SELECT class_id, class_name, academic_year, room_no " +
                        "FROM classes " +
                        "ORDER BY class_name";

        try (
                Connection con = DataBase_Connection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                ClassData data = new ClassData();

                data.setClassId(
                        rs.getInt("class_id")
                );

                data.setClassName(
                        rs.getString("class_name")
                );

                data.setAcademicYear(
                        rs.getString("academic_year")
                );

                data.setRoomNo(
                        rs.getString("room_no")
                );

                classes.add(data);
            }

        } catch (Exception e) {

            System.out.println("Failed to load classes.");
            e.printStackTrace();
        }

        return classes;
    }


    // =========================================================
    // GET CLASS BY ID
    // =========================================================

    public ClassData getClassById(int classId) {

        String sql =
                "SELECT class_id, class_name, academic_year, room_no " +
                        "FROM classes " +
                        "WHERE class_id = ?";

        try (
                Connection con = DataBase_Connection.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        ) {

            ps.setInt(1, classId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new ClassData(
                            rs.getInt("class_id"),
                            rs.getString("class_name"),
                            rs.getString("academic_year"),
                            rs.getString("room_no")
                    );
                }
            }

        } catch (Exception e) {

            System.out.println("Failed to find class.");
            e.printStackTrace();
        }

        return null;
    }
}