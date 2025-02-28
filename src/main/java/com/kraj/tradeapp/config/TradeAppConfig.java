package com.kraj.tradeapp.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.kraj.tradeapp.core.model.CommonUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeAppConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .addServersItem(new Server().url("http://localhost:8080"))
            .addServersItem(new Server().url("https://localhost:8080"))
            .addServersItem(new Server().url("https://trade-app-production.up.railway.app"));
    }

    @Bean
    public GoogleCredential googleSheetCredential(TradeAppConfigOptions tradeAppConfigOptions) throws IOException {
        String credentialJsonStr = CommonUtil.decodeBase64(tradeAppConfigOptions.getGoogleSheetServiceAccCredJson());
        ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(credentialJsonStr.getBytes(StandardCharsets.UTF_8));
        return GoogleCredential.fromStream(jsonInputStream).createScoped(Arrays.asList("https://www.googleapis.com/auth/spreadsheets"));
    }

    @Bean
    public Sheets googleSheets(GoogleCredential googleSheetCredential) throws IOException, GeneralSecurityException {
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), googleSheetCredential)
            .setApplicationName("tbd_health_backend")
            .build();
    }
}
