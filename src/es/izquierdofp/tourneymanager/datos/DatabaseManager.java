package es.izquierdofp.tourneymanager.datos;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

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
            ResultSet resultado=st.executeQuery("SELECT COUNT(*) FROM juego");
            int cantidad=0;
            if (resultado.next()) {
                cantidad=resultado.getInt(1);
            }
            resultado.close();
            if (cantidad == 0) {
    st.executeUpdate("""
        INSERT INTO juego (nombre) VALUES
        ('League of Legends'),
        ('Valorant'),
        ('Rocket League'),
        ('EA Sports FC'),
        ('Fortnite')
        """);
}
            ResultSet resultadoRol = st.executeQuery("SELECT COUNT(*) FROM rol");

int cantidadRol = 0;

if (resultadoRol.next()) {
    cantidadRol = resultadoRol.getInt(1);
}

resultadoRol.close();

if (cantidadRol == 0) {
    st.executeUpdate("""
        INSERT INTO rol (nombre) VALUES
        ('Capitán'),
        ('Titular'),
        ('Suplente'),
        ('Entrenador')
        """);
}
ResultSet resultadoEquipo = st.executeQuery("SELECT COUNT(*) FROM equipo");

int cantidadEquipo = 0;

if (resultadoEquipo.next()) {
    cantidadEquipo = resultadoEquipo.getInt(1);
}

resultadoEquipo.close();

if (cantidadEquipo == 0) {
    st.executeUpdate("""
        INSERT INTO equipo (nombre, ciudad, fundacion) VALUES
        ('Zaragoza Gaming', 'Zaragoza', 2020),
        ('Madrid Wolves', 'Madrid', 2018),
        ('Barcelona Titans', 'Barcelona', 2019),
        ('Valencia Dragons', 'Valencia', 2021),
        ('Sevilla Kings', 'Sevilla', 2022),
        ('Bilbao Eagles', 'Bilbao', 2017),
        ('Malaga Phoenix', 'Malaga', 2023),
        ('Granada Lions', 'Granada', 2024)
        """);
}
ResultSet resultadoTorneo = st.executeQuery("SELECT COUNT(*) FROM torneo");

int cantidadTorneo = 0;

if (resultadoTorneo.next()) {
    cantidadTorneo = resultadoTorneo.getInt(1);
}

resultadoTorneo.close();

if (cantidadTorneo == 0) {
    st.executeUpdate("""
        INSERT INTO torneo (nombre, juego_id, fecha, premio, online) VALUES
        ('Copa Zaragoza', 1, date('now', '-20 days'), 500, 0),
        ('Valorant Cup', 2, date('now', '-10 days'), 800, 1),
        ('Rocket League Cup', 3, date('now', '+3 days'), 600, 1),
        ('FC Championship', 4, date('now', '+15 days'), 1000, 0),
        ('Fortnite Cup', 5, date('now', '+25 days'), 1500, 1),
        ('League Masters', 1, date('now', '+30 days'), 2000, 0)
        """);
}
ResultSet resultadoInscripcion = st.executeQuery("SELECT COUNT(*) FROM inscripcion");

int cantidadInscripcion = 0;

if (resultadoInscripcion.next()) {
    cantidadInscripcion = resultadoInscripcion.getInt(1);
}

resultadoInscripcion.close();

if (cantidadInscripcion == 0) {
    st.executeUpdate("""
        INSERT INTO inscripcion (equipo_id, torneo_id, posicion) VALUES
        (1, 1, 1),
        (2, 1, 2),
        (3, 1, 3),
        (4, 1, 4),

        (5, 2, 1),
        (6, 2, 2),
        (7, 2, 3),

        (1, 3, NULL),
        (8, 3, NULL),

        (2, 4, NULL),
        (3, 4, NULL),

        (4, 5, NULL),
        (5, 5, NULL),

        (6, 6, NULL),
        (7, 6, NULL)
        """);
}
            System.out.println("Base de datos creada correctamente");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}