package br.com.washii.infra.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConfig {

    // [CORREÇÃO OBJECT STORAGE]: Carregamento resiliente do .env (procura em "." e ".."
    // e tolera chaves duplicadas sem lançar ExceptionInInitializerError)
    private static final Map<String, String> ENV = carregarVariaveisEnv();

    private static Map<String, String> carregarVariaveisEnv() {
        Map<String, String> mapa = new HashMap<>(System.getenv());
        Path caminhoEnv = Files.exists(Path.of(".env"))
                ? Path.of(".env")
                : (Files.exists(Path.of("..", ".env")) ? Path.of("..", ".env") : null);

        if (caminhoEnv != null) {
            try {
                String dir = caminhoEnv.getParent() != null ? caminhoEnv.getParent().toString() : ".";
                Dotenv dotenv = Dotenv.configure().directory(dir).ignoreIfMissing().load();
                dotenv.entries().forEach(e -> mapa.put(e.getKey(), e.getValue()));
            } catch (Exception ex) {
                // Fallback caso o Dotenv falhe por chave duplicada no .env
                try {
                    List<String> linhas = Files.readAllLines(caminhoEnv);
                    for (String linha : linhas) {
                        String limpa = linha.trim();
                        if (limpa.isEmpty() || limpa.startsWith("#") || !limpa.contains("=")) continue;
                        int idx = limpa.indexOf('=');
                        String chave = limpa.substring(0, idx).trim();
                        String valor = limpa.substring(idx + 1).trim();
                        mapa.put(chave, valor);
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return mapa;
    }

    public static String getEnv(String chave, String valorPadrao) {
        String val = ENV.get(chave);
        return (val != null && !val.isBlank()) ? val.trim() : valorPadrao;
    }

    // [CORREÇÃO OBJECT STORAGE]: Suporta tanto as variáveis DB_* quanto as variáveis
    // simplificadas USER (Project Ref do Supabase) e PASSWORD presentes no .env
    private static final String PROJECT_REF = getEnv("USER", "usxgpdppfsifsokwiqly");
    private static final String HOST = getEnv("DB_HOST", "aws-0-sa-east-1.pooler.supabase.com");
    private static final String PORT = getEnv("DB_PORT", "5432");
    private static final String DB_NAME = getEnv("DB_NAME", "postgres");
    private static final String USER = getEnv(
            "DB_USER",
            PROJECT_REF != null ? "postgres." + PROJECT_REF : "postgres"
    );
    private static final String PASSWORD = getEnv("DB_PASSWORD", getEnv("PASSWORD", ""));
    private static final String SSLMODE = getEnv("DB_SSLMODE", "require");
    private static final String SCHEMA = getEnv("DB_SCHEMA", "desktop");
    private static final String CHANNEL_BINDING = getEnv("DB_CHANNEL_BINDING", null);

    // [CORREÇÃO OBJECT STORAGE]: Monta a URL JDBC com currentSchema e só inclui channelBinding se definido
    private static final String URL = montarUrl();

    private static String montarUrl() {
        String base = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME
                + "?sslmode=" + SSLMODE
                + "&currentSchema=" + SCHEMA;
        if (CHANNEL_BINDING != null && !CHANNEL_BINDING.isBlank()) {
            base += "&channelBinding=" + CHANNEL_BINDING;
        }
        return base;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
