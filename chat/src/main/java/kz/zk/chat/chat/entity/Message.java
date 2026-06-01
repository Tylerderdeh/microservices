package kz.zk.chat.chat.entity;

import jakarta.persistence.*;
import kz.zk.chat.chat.entity.enums.MessageStatus;
import kz.zk.chat.chat.entity.enums.MessageType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID chatId;

    @Column(nullable = false)
    private UUID senderId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    private MessageType type; // TEXT, IMAGE, FILE

    @Enumerated(EnumType.STRING)
    private MessageStatus status; // SENT, DELIVERED, READ, DELETED

    private Boolean edited;

    private Instant sentAt;

    private Instant editedAt;

    @PrePersist
    public void prePersist() {
        sentAt = Instant.now();
        status = MessageStatus.SENT;
        edited = false;
    }
}