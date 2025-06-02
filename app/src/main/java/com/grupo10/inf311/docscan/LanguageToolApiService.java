package com.grupo10.inf311.docscan;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LanguageToolApiService {

    /**
     * Envia texto para verificação ortográfica e gramatical para a API LanguageTool.
     *
     * @param language O código do idioma para verificação (ex: "pt-BR", "en-US").
     * @param text O texto a ser verificado.
     * @return Um objeto Call que pode ser usado para executar a requisição de forma síncrona ou assíncrona.
     * O tipo de resposta esperado é LanguageToolResponse.
     */
    @FormUrlEncoded // Indica que os dados da requisição serão enviados como um formulário codificado na URL (application/x-www-form-urlencoded)
    @POST("api/v2/check") // Define o método HTTP como POST e o endpoint relativo à URL base.
    // A URL base será definida ao configurar o Retrofit (ex: "https://languagetool.org/")
    Call<LanguageToolResponse> checkText(
            @Field("language") String language, // Mapeia o parâmetro 'language' do método para o campo 'language' no corpo do formulário
            @Field("text") String text          // Mapeia o parâmetro 'text' do método para o campo 'text' no corpo do formulário
            // Outros parâmetros opcionais da API LanguageTool podem ser adicionados aqui, como:
            // @Field("motherTongue") String motherTongue,
            // @Field("preferredVariants") String preferredVariants,
            // @Field("enabledRules") String enabledRules,
            // @Field("disabledRules") String disabledRules,
            // @Field("enabledCategories") String enabledCategories,
            // @Field("disabledCategories") String disabledCategories,
            // @Field("enabledOnly") boolean enabledOnly
    );

    //  poderia adicionar outros métodos aqui para diferentes endpoints da API, se necessário.
}
