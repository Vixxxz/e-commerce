package Service;

import Util.Resultado;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiService {

    private static final String MODELO = "gemini-2.0-flash";

    public static Resultado<String> consultarIA(String prompt) {
        try {
            // Inicializa o cliente da Gemini (usa GOOGLE_API_KEY da env)
            Client client = new Client();

            // Chama o modelo com o prompt
            GenerateContentResponse response = client.models.generateContent(MODELO, prompt, null);

            // Extrai a resposta textual
            String texto = response.text();
            if (texto == null || texto.isBlank()) {
                return Resultado.erro("A IA não retornou nenhuma resposta.");
            }

            // Limpa o texto de marcações Markdown e espaços extras
            String textoLimpo = limparFormatacao(texto.trim());

            return Resultado.sucesso(textoLimpo);

        } catch (Exception e) {
            // Para depuração, é útil imprimir o stack trace
            e.printStackTrace();
            return Resultado.erro("Erro ao consultar a IA: " + e.getMessage());
        }
    }

    private static String limparFormatacao(String texto) {
        if (texto == null) {
            return "";
        }

        // Remove blocos de código (incluindo a palavra da linguagem, como "java" ou "python")
        // Exemplo: ```java ...código... ``` vira apenas "...código..."
        String textoLimpo = texto.replaceAll("(?s)```[a-zA-Z]*\\n(.*?)\\n```", "$1");

        // Remove marcações de negrito (**texto** ou __texto__)
        textoLimpo = textoLimpo.replaceAll("[*]{2}(.*?)[*]{2}", "$1");
        textoLimpo = textoLimpo.replaceAll("[_]{2}(.*?)[_]{2}", "$1");

        // Remove marcações de itálico (*texto* ou _texto_)
        textoLimpo = textoLimpo.replaceAll("[*](.*?)[*]", "$1");
        textoLimpo = textoLimpo.replaceAll("[_](.*?)[_]", "$1");

        // Remove marcações de título (### Título)
        textoLimpo = textoLimpo.replaceAll("(?m)^#+\\s*(.*)", "$1");

        // Remove marcadores de lista (*, -, +) no início da linha
        textoLimpo = textoLimpo.replaceAll("(?m)^[\\*\\-\\+]\\s+", "");

        // Remove marcadores de lista numerada (1., 2., etc.)
        textoLimpo = textoLimpo.replaceAll("(?m)^\\d+\\.\\s+", "");

        // Remove linhas contendo apenas ```
        textoLimpo = textoLimpo.replaceAll("(?m)^```$", "");

        // Retorna o texto após todas as limpezas, aplicando um trim final
        return textoLimpo.trim();
    }
}