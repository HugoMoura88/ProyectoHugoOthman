package es.izquierdofp.tourneymanager.datos;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static String rutaBD = System.getProperty("user.home")
            + "/.tourneymanager/tourneymanager.db";

    public static void setRutaBD(String ruta) {
        rutaBD = ruta;
    }

    public static Connection getConnection() throws SQLException {

        Connection conexion = DriverManager.getConnection(
                "jdbc:sqlite:" + rutaBD);

        try (Statement st = conexion.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conexion.close();
            throw e;
        }

        return conexion;
    }

    public static void inicializar() {

        File archivo = new File(rutaBD);
        File carpeta = archivo.getParentFile();

        if (carpeta != null) {
            carpeta.mkdirs();
        }

        try (Connection conexion = getConnection();
                Statement st = conexion.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS juego (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS rol (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS equipo (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE,
                    ciudad TEXT NOT NULL,
                    fundacion INTEGER NOT NULL
                        CHECK (fundacion BETWEEN 1990 AND 2100),
                    logo TEXT
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS torneo (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL,
                    juego_id INTEGER NOT NULL
                        REFERENCES juego(id) ON DELETE RESTRICT,
                    fecha TEXT NOT NULL,
                    premio REAL NOT NULL CHECK (premio >= 0),
                    online INTEGER NOT NULL DEFAULT 0
                        CHECK (online IN (0,1))
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS inscripcion (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    equipo_id INTEGER NOT NULL
                        REFERENCES equipo(id) ON DELETE CASCADE,
                    torneo_id INTEGER NOT NULL
                        REFERENCES torneo(id) ON DELETE CASCADE,
                    posicion INTEGER
                        CHECK (posicion IS NULL OR posicion >= 1),
                    UNIQUE (equipo_id, torneo_id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS jugador (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    equipo_id INTEGER NOT NULL
                        REFERENCES equipo(id) ON DELETE CASCADE,
                    rol_id INTEGER NOT NULL
                        REFERENCES rol(id) ON DELETE RESTRICT,
                    nick TEXT NOT NULL UNIQUE,
                    nombre TEXT NOT NULL,
                    edad INTEGER NOT NULL
                        CHECK (edad BETWEEN 14 AND 60)
                )
                """);

            System.out.println("Base de datos creada correctamente");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}