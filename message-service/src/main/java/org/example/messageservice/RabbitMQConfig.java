package org.example.messageservice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange messageExchange() {
        return new TopicExchange("message.exchange");
    }

    @Bean
    public Queue messageQueue() {
        return new Queue("message.queue");
    }

    @Bean
    public Binding messageBinding() {
        return BindingBuilder
                .bind(messageQueue())
                .to(messageExchange())
                .with("message.published");
    }

}
