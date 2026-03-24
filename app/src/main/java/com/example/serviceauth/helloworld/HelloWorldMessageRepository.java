package com.example.serviceauth.helloworld;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HelloWorldMessageRepository extends JpaRepository<HelloWorldMessage, String> {
}
