package kz.zk.authservice.service;


public interface EmailService {
    void sendVerificationEmail(String to, String token, String locale);
}
