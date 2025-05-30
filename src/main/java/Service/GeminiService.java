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
            System.out.println("A chave fornecida foi: " + client.apiKey());

            // Chama o modelo com o prompt
            GenerateContentResponse response = client.models.generateContent(MODELO, prompt, null);

            // Extrai a resposta textual
            String texto = response.text();
            if (texto == null || texto.isBlank()) {
                return Resultado.erro("A IA não retornou nenhuma resposta.");
            }

            return Resultado.sucesso(texto.trim());

        } catch (Exception e) {
            return Resultado.erro("Erro ao consultar a IA: " + e.getMessage());
        }
    }
}