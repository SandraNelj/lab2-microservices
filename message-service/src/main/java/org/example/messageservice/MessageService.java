package org.example.messageservice;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final UserGrpcClient userGrpcClient;

    public MessageService(MessageRepository messageRepository,
                          RabbitTemplate rabbitTemplate,
                          UserGrpcClient userGrpcClient) {
        this.messageRepository = messageRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.userGrpcClient = userGrpcClient;
    }
    public Message saveMessage(Message message) {
        String displayName = userGrpcClient.getUserDisplayName(message.getUsername());
        Message savedMessage = messageRepository.save(message);

        String messageContent = displayName + ": " + savedMessage.getContent();
        rabbitTemplate.convertAndSend("message.exchange", "message.published", messageContent);

        return savedMessage;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Message> getMessagesByUser(String username) {
        return messageRepository.findByUsername(username);
    }

}
