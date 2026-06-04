package org.example.bff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
public class GatewayRoutes {

    // Startsida
    @Bean
    public RouterFunction<ServerResponse> homeRoute() {
        return route()
                .GET("/", request -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .header("Content-Type", "text/html; charset=utf-8")
                        .body("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>Chat App</title>
                        <style>
                            body { font-family: Arial; margin: 40px; }
                            a { display: block; margin: 10px 0; }
                        </style>
                    </head>
                    <body>
                        <h1>Välkommen till Microservices Chat!</h1>
                        <p>Du är inloggad!</p>
                        <h3>Testa endpoints:</h3>
                        <ul>
                            <li><a href="/api/users">Hämta användare</a></li>
                            <li><a href="/api/messages">Hämta meddelanden</a></li>
                        </ul>
                    </body>
                    </html>
                    """))
                .build();
    }

    // Auth Service routes
    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return route()
                .POST("/api/auth/**", http())
                .before(uri("http://localhost:9000/"))
                .build();
    }

    // User Service routes
    @Bean
    public RouterFunction<ServerResponse> userRoutes() {
        return route()
                .GET("/api/users/**", http())
                .POST("/api/users", http())
                .PUT("/api/users/**", http())
                .DELETE("/api/users/**", http())
                .before(uri("http://user-service:8081/"))
                .build();
    }

    // Message Service routes
    @Bean
    public RouterFunction<ServerResponse> messageRoutes() {
        return route()
                .GET("/api/messages/**", http())
                .POST("/api/messages", http())
                .before(uri("http://message-service:8082/"))
                .build();
    }
}