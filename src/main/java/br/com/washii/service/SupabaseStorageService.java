package br.com.washii.service;

import br.com.washii.infra.database.DatabaseConfig;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class SupabaseStorageService {

    // [CORREÇÃO OBJECT STORAGE]: Lê SUPABASE_URL do .env (ou monta a partir de USER do .env)
    private static final String SUPABASE_URL = obterSupabaseUrl();

    // [CORREÇÃO OBJECT STORAGE]: Lê SUPABASE_KEY ou SECRET_KEY diretamente do .env (sem chave hardcoded no código)
    private static final String SUPABASE_KEY = obterSupabaseKey();

    private static final String SUPABASE_BUCKET = DatabaseConfig.getEnv("SUPABASE_BUCKET", "fotos_perfil");

    private static String obterSupabaseUrl() {
        String url = DatabaseConfig.getEnv("SUPABASE_URL", null);
        if (url != null) {
            return url;
        }
        String projectRef = DatabaseConfig.getEnv("USER", null);
        if (projectRef != null) {
            return "https://" + projectRef + ".supabase.co";
        }
        throw new IllegalStateException("Variável SUPABASE_URL ou USER não encontrada no arquivo .env");
    }

    private static String obterSupabaseKey() {
        String key = DatabaseConfig.getEnv("SUPABASE_KEY", DatabaseConfig.getEnv("SECRET_KEY", null));
        if (key == null) {
            throw new IllegalStateException("Variável SUPABASE_KEY ou SECRET_KEY não encontrada no arquivo .env");
        }
        return key;
    }

    // [CORREÇÃO OBJECT STORAGE]: Reutilização da instância do HttpClient em vez de recriar a cada upload
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Faz o upload de um ficheiro local para o Supabase Storage.
     * @param caminhoFicheiroLocal Caminho do ficheiro no computador (ex: C:\fotos\perfil.jpg)
     * @param idUsuario O ID (Long ou UUID em String) do utilizador na base de dados
     * @return O URL público da imagem gerada no Supabase
     */
    public static String fazerUpload(Path caminhoFicheiroLocal, String idUsuario) throws Exception {
        String bucket = SUPABASE_BUCKET;
        String nomeFicheiroOriginal = caminhoFicheiroLocal.getFileName().toString();

        // [CORREÇÃO OBJECT STORAGE]: Codifica espaços e caracteres especiais no nome do arquivo
        // para evitar IllegalArgumentException no URI.create(urlDaApi)
        String nomeFicheiroCodificado = URLEncoder.encode(nomeFicheiroOriginal, StandardCharsets.UTF_8)
                .replace("+", "%20");

        // Caminho dentro do bucket: id_do_usuario/nome_do_ficheiro
        String caminhoNoSupabase = idUsuario + "/" + nomeFicheiroCodificado;

        String urlDaApi = SUPABASE_URL + "/storage/v1/object/" + bucket + "/" + caminhoNoSupabase;

        // Tenta detetar automaticamente se é PNG, JPG, etc.
        String mimeType = Files.probeContentType(caminhoFicheiroLocal);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        // Prepara a requisição POST
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlDaApi))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("Content-Type", mimeType)
                .header("x-upsert", "true") // Substitui o ficheiro se já existir um com o mesmo nome
                .POST(HttpRequest.BodyPublishers.ofFile(caminhoFicheiroLocal))
                .build();

        // Envia a imagem e aguarda a resposta
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        // Verifica se o upload foi um sucesso (Código 200)
        if (response.statusCode() == 200) {
            // [CORREÇÃO OBJECT STORAGE]: Retorna a URL pública (com timestamp para evitar cache de imagem antiga)
            return SUPABASE_URL + "/storage/v1/object/public/" + bucket + "/" + caminhoNoSupabase
                    + "?t=" + System.currentTimeMillis();
        } else {
            throw new RuntimeException("Erro no upload (Código " + response.statusCode() + "): " + response.body());
        }
    }
}
